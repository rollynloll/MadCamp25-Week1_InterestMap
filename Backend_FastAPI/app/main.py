from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional, Dict, Any
import uuid
from datetime import datetime
from app.schemas import (
    UserCreateRequest, UserUpdateRequest, UserResponse,
    GroupCreateRequest, GroupResponse, AddMemberRequest,
    PhotoUploadResponse, TagAnalysisRequest, TagAnalysisResponse,
    ImageAnalysisRequest, ImageAnalysisResponse, ImageAnalysisResult, ImageKeyword,
    GenerateEmbeddingRequest, EmbeddingResponse
)

app = FastAPI(title="InterestMap API", version="1.0.0")

# CORS 설정 (Android 앱에서 접근 가능하도록)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 중에는 모든 origin 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory 데이터베이스 (실제로는 PostgreSQL 사용)
users_db: Dict[str, dict] = {}
photos_db: Dict[str, dict] = {}
groups_db: Dict[str, dict] = {}
user_provider_map: Dict[str, str] = {}  # (provider, provider_user_id) -> user_id

@app.get("/")
def read_root():
    return {"message": "Hello FastAPI"}

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "InterestMap Backend",
        "version": "1.0.0"
    }

# ==================== User APIs ====================

@app.post("/api/users", response_model=UserResponse)
async def create_user(request: UserCreateRequest):
    """카카오 로그인 후 사용자 생성 또는 조회"""
    # 이미 존재하는 사용자인지 확인
    user_key = f"{request.provider}:{request.provider_user_id}"
    if user_key in user_provider_map:
        # 기존 사용자 반환
        user_id = user_provider_map[user_key]
        return users_db[user_id]
    
    # 새 사용자 생성
    user_id = str(uuid.uuid4())
    now = datetime.now().isoformat()
    
    user_data = {
        "id": user_id,
        "provider": request.provider,
        "provider_user_id": request.provider_user_id,
        "nickname": request.nickname,
        "profile_image_url": request.profile_image_url,
        "profile_data": request.profile_data or {},
        "created_at": now,
        "updated_at": now
    }
    
    users_db[user_id] = user_data
    user_provider_map[user_key] = user_id
    
    return user_data

@app.get("/api/users/{user_id}", response_model=UserResponse)
async def get_user(user_id: str):
    """사용자 정보 조회"""
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    return users_db[user_id]

@app.put("/api/users/{user_id}", response_model=UserResponse)
async def update_user(user_id: str, request: UserUpdateRequest):
    """사용자 프로필 업데이트"""
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    user = users_db[user_id]
    
    if request.nickname is not None:
        user["nickname"] = request.nickname
    if request.profile_image_url is not None:
        user["profile_image_url"] = request.profile_image_url
    if request.profile_data is not None:
        user["profile_data"].update(request.profile_data)
    
    user["updated_at"] = datetime.now().isoformat()
    
    return user

# ==================== Photo APIs ====================

@app.post("/api/photos", response_model=PhotoUploadResponse)
async def upload_photo(
    user_id: str = Form(...),
    file: UploadFile = File(...)
):
    """사진 업로드"""
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    photo_id = str(uuid.uuid4())
    file_path = f"/uploads/{user_id}/{photo_id}_{file.filename}"
    file_url = f"http://localhost:8000{file_path}"
    
    photo_data = {
        "id": photo_id,
        "user_id": user_id,
        "file_path": file_path,
        "file_url": file_url,
        "uploaded_at": datetime.now().isoformat()
    }
    
    photos_db[photo_id] = photo_data
    
    return photo_data

@app.get("/api/photos/user/{user_id}", response_model=List[PhotoUploadResponse])
async def get_user_photos(user_id: str):
    """사용자의 사진 목록 조회"""
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_photos = [photo for photo in photos_db.values() if photo["user_id"] == user_id]
    return user_photos

# ==================== Group APIs ====================

@app.post("/api/groups", response_model=GroupResponse)
async def create_group(request: GroupCreateRequest):
    """그룹 생성"""
    if request.creator_id not in users_db:
        raise HTTPException(status_code=404, detail="Creator not found")
    
    group_id = str(uuid.uuid4())
    now = datetime.now().isoformat()
    
    group_data = {
        "id": group_id,
        "name": request.name,
        "creator_id": request.creator_id,
        "description": request.description,
        "member_ids": [request.creator_id],  # 생성자는 자동으로 멤버
        "created_at": now
    }
    
    groups_db[group_id] = group_data
    
    return group_data

@app.get("/api/groups/{group_id}", response_model=GroupResponse)
async def get_group(group_id: str):
    """그룹 정보 조회"""
    if group_id not in groups_db:
        raise HTTPException(status_code=404, detail="Group not found")
    return groups_db[group_id]

@app.get("/api/groups/user/{user_id}", response_model=List[GroupResponse])
async def get_user_groups(user_id: str):
    """사용자가 속한 그룹 목록 조회"""
    if user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_groups = [
        group for group in groups_db.values() 
        if user_id in group["member_ids"]
    ]
    return user_groups

@app.post("/api/groups/{group_id}/members", response_model=GroupResponse)
async def add_group_member(group_id: str, request: AddMemberRequest):
    """그룹에 멤버 추가"""
    if group_id not in groups_db:
        raise HTTPException(status_code=404, detail="Group not found")
    if request.user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    group = groups_db[group_id]
    
    if request.user_id not in group["member_ids"]:
        group["member_ids"].append(request.user_id)
    
    return group

@app.delete("/api/groups/{group_id}/members/{user_id}")
async def remove_group_member(group_id: str, user_id: str):
    """그룹에서 멤버 제거"""
    if group_id not in groups_db:
        raise HTTPException(status_code=404, detail="Group not found")
    
    group = groups_db[group_id]
    
    if user_id in group["member_ids"]:
        group["member_ids"].remove(user_id)
    
    return {"message": "Member removed successfully"}

# ==================== Tag/Analysis APIs ====================

@app.post("/api/analyze/tags", response_model=TagAnalysisResponse)
async def analyze_images_for_tags(request: TagAnalysisRequest):
    """이미지를 분석하여 태그 생성 (Mock 구현)"""
    if request.user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Mock 태그 반환 (실제로는 AI 모델 사용)
    mock_tags = ["여행", "음식", "카페", "운동", "독서", "영화", "음악", "게임"]
    mock_categories = ["라이프스타일", "취미", "문화"]
    mock_interests = ["아웃도어", "실내활동", "예술"]
    
    return {
        "tags": mock_tags[:5],
        "categories": mock_categories,
        "interests": mock_interests[:3]
    }

@app.post("/api/analyze/images", response_model=ImageAnalysisResponse)
async def analyze_images(request: ImageAnalysisRequest):
    """
    여러 이미지를 분석하여 캡셔닝 + 키워드 추출
    실제로는 BLIP, CLIP 등의 모델 사용
    """
    if request.user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Mock 이미지 분석 결과
    mock_captions = [
        "사람이 카페에서 커피를 마시고 있다",
        "아름다운 산 풍경과 하이킹하는 사람들",
        "맛있는 파스타 요리가 테이블에 놓여있다",
        "해변에서 일몰을 감상하는 모습",
        "책을 읽으며 편안하게 휴식하는 장면"
    ]
    
    mock_keywords_pool = [
        ["카페", "커피", "실내", "휴식", "음료"],
        ["등산", "자연", "산", "아웃도어", "운동"],
        ["음식", "파스타", "맛집", "이탈리안", "요리"],
        ["여행", "바다", "일몰", "휴가", "해변"],
        ["독서", "책", "힐링", "인테리어", "취미"]
    ]
    
    import random
    results = []
    all_keywords_dict = {}
    
    for i, image_url in enumerate(request.image_urls):
        caption_idx = i % len(mock_captions)
        keywords_idx = i % len(mock_keywords_pool)
        
        keywords = []
        for keyword in mock_keywords_pool[keywords_idx]:
            confidence = random.uniform(0.7, 0.95)
            keywords.append(ImageKeyword(
                keyword=keyword,
                confidence=confidence,
                category=random.choice(["hobby", "interest", "lifestyle", "food", "travel"])
            ))
            
            # 키워드 빈도수 카운트
            if keyword not in all_keywords_dict:
                all_keywords_dict[keyword] = {"count": 0, "total_confidence": 0.0}
            all_keywords_dict[keyword]["count"] += 1
            all_keywords_dict[keyword]["total_confidence"] += confidence
        
        results.append(ImageAnalysisResult(
            image_url=image_url,
            caption=mock_captions[caption_idx],
            keywords=keywords
        ))
    
    # TOP-5 추천 태그 계산 (빈도수 * 평균 confidence)
    keyword_scores = []
    for keyword, data in all_keywords_dict.items():
        avg_confidence = data["total_confidence"] / data["count"]
        score = data["count"] * avg_confidence
        keyword_scores.append((keyword, score, avg_confidence))
    
    keyword_scores.sort(key=lambda x: x[1], reverse=True)
    recommended_tags = [kw[0] for kw in keyword_scores[:5]]
    
    all_keywords = [
        ImageKeyword(keyword=kw[0], confidence=kw[2], category="recommended")
        for kw in keyword_scores[:10]
    ]
    
    return ImageAnalysisResponse(
        user_id=request.user_id,
        results=results,
        recommended_tags=recommended_tags,
        all_keywords=all_keywords
    )

@app.post("/api/generate-embedding", response_model=EmbeddingResponse)
async def generate_embedding(request: GenerateEmbeddingRequest):
    """
    사용자 프로필 전체를 임베딩 벡터로 변환
    실제로는 Sentence-BERT, OpenAI Embeddings 등 사용
    """
    if request.user_id not in users_db:
        raise HTTPException(status_code=404, detail="User not found")
    
    import numpy as np
    
    # Mock 임베딩 생성 (128차원)
    # 실제로는 텍스트 + 이미지 특징을 결합한 임베딩
    np.random.seed(hash(request.user_id) % (2**32))  # 같은 user_id는 같은 임베딩
    embedding = np.random.randn(128).tolist()
    
    # 2D 맵 위치 계산 (t-SNE, UMAP 등으로 차원 축소)
    # Mock으로 -1.0 ~ 1.0 범위로 정규화
    map_x = np.tanh(embedding[0] + embedding[1])
    map_y = np.tanh(embedding[2] + embedding[3])
    
    # 사용자 프로필에 임베딩 저장
    user = users_db[request.user_id]
    user["profile_data"]["embedding"] = embedding
    user["profile_data"]["map_position"] = {"x": float(map_x), "y": float(map_y)}
    user["profile_data"]["tags"] = request.tags
    user["profile_data"]["image_keywords"] = request.image_keywords
    user["updated_at"] = datetime.now().isoformat()
    
    return EmbeddingResponse(
        user_id=request.user_id,
        embedding=embedding,
        map_position={"x": float(map_x), "y": float(map_y)}
    )

# ==================== Test APIs (개발용) ====================

@app.post("/api/users/test")
async def create_test_user(user_data: dict):
    return {
        "id": "test-user-123",
        "provider": user_data.get("provider", "kakao"),
        "provider_user_id": user_data.get("provider_user_id", "12345"),
        "nickname": user_data.get("nickname", "Test User"),
        "profile_image_url": user_data.get("profile_image_url"),
        "profile_data": {},
        "created_at": "2026-01-09T14:00:00Z",
        "updated_at": "2026-01-09T14:00:00Z"
    }

@app.get("/api/users/test/{user_id}")
async def get_test_user(user_id: str):
    return {
        "id": user_id,
        "provider": "kakao",
        "provider_user_id": "12345",
        "nickname": "Test User",
        "profile_image_url": None,
        "profile_data": {},
        "created_at": "2026-01-09T14:00:00Z",
        "updated_at": "2026-01-09T14:00:00Z"
    }
