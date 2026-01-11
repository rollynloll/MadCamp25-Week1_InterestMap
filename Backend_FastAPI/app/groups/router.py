from __future__ import annotations

from datetime import datetime
import hashlib
import uuid

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import and_, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.deps import get_current_user
from app.db.session import get_db
from app.models.embedding import UserEmbedding
from app.models.group import Group, GroupMember
from app.models.message import GroupMessage
from app.models.photo import UserPhoto
from app.models.user import User
from app.schemas import (
    GroupCreateRequest,
    GroupListItem,
    GroupListResponse,
    GroupMemberItem,
    GroupMembersResponse,
    InterestMapEdge,
    InterestMapGroup,
    InterestMapLayout,
    InterestMapNode,
    InterestMapResponse,
    MessageContent,
    MessageCreateRequest,
    MessageItem,
    MessageListResponse,
    MessageSender,
    OkResponse,
)

router = APIRouter(prefix="/groups", tags=["groups"])


@router.post("", status_code=201)
async def create_group(
    payload: GroupCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    그룹 생성 및 creator를 자동으로 멤버로 추가
    """
    # 그룹 생성
    group = Group(
        name=payload.name,
        description=payload.description or "",
    )
    db.add(group)
    await db.flush()
    
    # Creator를 자동으로 멤버로 추가 (role: creator)
    member = GroupMember(
        group_id=group.id,
        user_id=current_user.id,
        role="creator"
    )
    db.add(member)
    await db.commit()
    await db.refresh(group)
    
    return {
        "id": str(group.id),
        "name": group.name,
        "description": group.description,
        "creator_id": str(current_user.id),
        "member_ids": [str(current_user.id)]
    }


def _coords_from_uuid(user_id: uuid.UUID) -> tuple[float, float]:
    digest = hashlib.sha256(user_id.bytes).digest()
    x = (int.from_bytes(digest[:4], "big") / 2**32) * 2 - 1
    y = (int.from_bytes(digest[4:8], "big") / 2**32) * 2 - 1
    return x, y


async def _get_primary_photo_map(db: AsyncSession, user_ids: list[uuid.UUID]):
    if not user_ids:
        return {}
    result = await db.execute(
        select(UserPhoto.user_id, UserPhoto.url).where(
            UserPhoto.user_id.in_(user_ids),
            UserPhoto.is_primary == True,  # noqa: E712
        )
    )
    return {row[0]: row[1] for row in result.all()}


async def _ensure_group(db: AsyncSession, group_id: uuid.UUID) -> Group:
    group = await db.get(Group, group_id)
    if not group:
        raise HTTPException(status_code=404, detail="Group not found")
    return group


async def _ensure_member(db: AsyncSession, group_id: uuid.UUID, user_id: uuid.UUID):
    member = await db.get(GroupMember, {"group_id": group_id, "user_id": user_id})
    if not member:
        raise HTTPException(status_code=403, detail="Not a group member")


@router.get("", response_model=GroupListResponse)
async def list_groups(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    membership_result = await db.execute(
        select(GroupMember.group_id).where(GroupMember.user_id == current_user.id)
    )
    member_group_ids = {row[0] for row in membership_result.all()}

    result = await db.execute(
        select(Group, func.count(GroupMember.user_id))
        .outerjoin(GroupMember, GroupMember.group_id == Group.id)
        .group_by(Group.id)
    )

    items = []
    for group, member_count in result.all():
        # group_profile에서 tags, region, image_url 추출
        profile = group.group_profile or {}
        tags = profile.get("tags", [])
        region = profile.get("region", "")
        image_url = profile.get("image_url", "")
        
        items.append(
            GroupListItem(
                id=str(group.id),
                name=group.name,
                description=group.description,
                member_count=member_count,
                is_member=group.id in member_group_ids,
                tags=tags,
                region=region,
                image_url=image_url,
            )
        )

    return GroupListResponse(items=items)


@router.post("/{group_id}/join", response_model=OkResponse)
async def join_group(
    group_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await _ensure_group(db, group_id)

    existing = await db.get(GroupMember, {"group_id": group_id, "user_id": current_user.id})
    if not existing:
        member = GroupMember(group_id=group_id, user_id=current_user.id, role="member")
        db.add(member)
        await db.commit()

    return OkResponse(ok=True)


@router.get("/{group_id}/members", response_model=GroupMembersResponse)
async def list_group_members(
    group_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await _ensure_group(db, group_id)

    result = await db.execute(
        select(User)
        .join(GroupMember, GroupMember.user_id == User.id)
        .where(GroupMember.group_id == group_id)
    )
    users = result.scalars().all()

    primary_photo_map = await _get_primary_photo_map(db, [user.id for user in users])

    items = [
        GroupMemberItem(
            user_id=str(user.id),
            nickname=user.nickname,
            primary_photo_url=primary_photo_map.get(user.id),
        )
        for user in users
    ]

    return GroupMembersResponse(items=items)


@router.get("/{group_id}/interest-map", response_model=InterestMapResponse)
async def group_interest_map(
    group_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    group = await _ensure_group(db, group_id)

    result = await db.execute(
        select(User)
        .join(GroupMember, GroupMember.user_id == User.id)
        .where(GroupMember.group_id == group_id)
    )
    users = result.scalars().all()
    user_ids = [user.id for user in users]

    embedding_result = await db.execute(
        select(UserEmbedding.user_id).where(
            UserEmbedding.user_id.in_(user_ids),
            UserEmbedding.is_active == True,  # noqa: E712
        )
    )
    embedding_user_ids = {row[0] for row in embedding_result.all()}

    primary_photo_map = await _get_primary_photo_map(db, user_ids)

    nodes = []
    for user in users:
        x, y = _coords_from_uuid(user.id)
        nodes.append(
            InterestMapNode(
                user_id=str(user.id),
                nickname=user.nickname,
                primary_photo_url=primary_photo_map.get(user.id),
                x=x,
                y=y,
                embedding_status="ready" if user.id in embedding_user_ids else "missing",
            )
        )

    return InterestMapResponse(
        group=InterestMapGroup(id=str(group.id), name=group.name),
        layout=InterestMapLayout(method="umap", version="v1", generated_at=datetime.now()),
        nodes=nodes,
        edges=[],
    )


@router.get("/{group_id}/messages", response_model=MessageListResponse)
async def list_group_messages(
    group_id: uuid.UUID,
    limit: int = Query(default=30, ge=1, le=100),
    before: str | None = Query(default=None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await _ensure_group(db, group_id)
    await _ensure_member(db, group_id, current_user.id)

    before_dt = None
    if before:
        try:
            before_dt = datetime.fromisoformat(before)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail="Invalid before parameter") from exc

    query = (
        select(GroupMessage, User, UserPhoto.url)
        .join(User, GroupMessage.sender_id == User.id)
        .outerjoin(
            UserPhoto,
            and_(
                UserPhoto.user_id == User.id,
                UserPhoto.is_primary == True,  # noqa: E712
            ),
        )
        .where(GroupMessage.group_id == group_id)
    )

    if before_dt:
        query = query.where(GroupMessage.created_at < before_dt)

    query = query.order_by(GroupMessage.created_at.desc()).limit(limit)
    rows = (await db.execute(query)).all()

    items = []
    for message, sender, primary_url in rows:
        items.append(
            MessageItem(
                id=str(message.id),
                group_id=str(message.group_id),
                sender=MessageSender(
                    user_id=str(sender.id),
                    nickname=sender.nickname,
                    primary_photo_url=primary_url,
                ),
                content=MessageContent(text=message.content.get("text", "")),
                created_at=message.created_at,
            )
        )

    next_before = items[-1].created_at if len(items) == limit else None

    return MessageListResponse(items=items, next_before=next_before)


@router.post("/{group_id}/messages", response_model=MessageItem, status_code=201)
async def create_group_message(
    group_id: uuid.UUID,
    payload: MessageCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    await _ensure_group(db, group_id)
    await _ensure_member(db, group_id, current_user.id)

    message = GroupMessage(
        group_id=group_id,
        sender_id=current_user.id,
        content={"text": payload.text},
    )
    db.add(message)
    await db.commit()
    await db.refresh(message)

    primary_url = await _get_primary_photo_map(db, [current_user.id])

    return MessageItem(
        id=str(message.id),
        group_id=str(message.group_id),
        sender=MessageSender(
            user_id=str(current_user.id),
            nickname=current_user.nickname,
            primary_photo_url=primary_url.get(current_user.id),
        ),
        content=MessageContent(text=payload.text),
        created_at=message.created_at,
    )
