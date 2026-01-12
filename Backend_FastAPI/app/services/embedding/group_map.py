from __future__ import annotations

from collections import OrderedDict
from dataclasses import dataclass
from datetime import datetime
import hashlib
import math
from typing import Iterable


CANVAS_WIDTH = 390.0
CANVAS_HEIGHT = 520.0
PADDING = 48.0


@dataclass(frozen=True)
class GroupMapInput:
    user_id: str
    embedding: list[float] | None
    updated_at: datetime | None


class GroupMapCache:
    def __init__(self, max_items: int = 128) -> None:
        self._items: OrderedDict[str, dict[str, tuple[float, float]]] = OrderedDict()
        self._max_items = max_items

    def get(self, key: str) -> dict[str, tuple[float, float]] | None:
        if key not in self._items:
            return None
        self._items.move_to_end(key)
        return self._items[key]

    def set(self, key: str, value: dict[str, tuple[float, float]]) -> None:
        self._items[key] = value
        self._items.move_to_end(key)
        if len(self._items) > self._max_items:
            self._items.popitem(last=False)


_CACHE = GroupMapCache()


def build_group_map_positions(
    group_id: str,
    members: Iterable[GroupMapInput],
) -> dict[str, tuple[float, float]]:
    member_list = list(members)
    signature = _build_signature(member_list)
    cache_key = f"{group_id}:{signature}"
    cached = _CACHE.get(cache_key)
    if cached is not None:
        return cached

    user_ids = [member.user_id for member in member_list]
    embeddings = [member.embedding for member in member_list]
    positions = _compute_positions(user_ids, embeddings)
    _CACHE.set(cache_key, positions)
    return positions


def _build_signature(members: Iterable[GroupMapInput]) -> str:
    parts = []
    for member in sorted(members, key=lambda item: item.user_id):
        if member.updated_at is None:
            stamp = "none"
        else:
            stamp = member.updated_at.isoformat()
        parts.append(f"{member.user_id}:{stamp}")
    raw = "|".join(parts).encode("utf-8")
    return hashlib.sha1(raw).hexdigest()


def _compute_positions(
    user_ids: list[str],
    embeddings: list[list[float] | None],
) -> dict[str, tuple[float, float]]:
    if not user_ids:
        return {}

    dim = 0
    for emb in embeddings:
        if emb:
            dim = len(emb)
            break
    if dim == 0:
        return _circle_layout(user_ids)

    vectors: list[list[float]] = []
    for emb in embeddings:
        if emb and len(emb) == dim:
            vectors.append([float(value) for value in emb])
        else:
            vectors.append([0.0] * dim)

    coords = _pca_2d(vectors)
    if coords is None:
        return _circle_layout(user_ids)

    return {
        user_id: (coords[index][0], coords[index][1])
        for index, user_id in enumerate(user_ids)
    }


def _pca_2d(vectors: list[list[float]]) -> list[tuple[float, float]] | None:
    count = len(vectors)
    if count == 0:
        return None
    dim = len(vectors[0])
    if dim == 0:
        return None

    mean = [0.0] * dim
    for vec in vectors:
        for i, value in enumerate(vec):
            mean[i] += value
    mean = [value / count for value in mean]

    centered = []
    for vec in vectors:
        centered.append([value - mean[i] for i, value in enumerate(vec)])

    if _all_zero(centered):
        return None

    axis1 = _power_iteration(centered)
    if axis1 is None:
        return None
    axis2 = _power_iteration(centered, orthogonal_to=axis1)
    if axis2 is None:
        axis2 = _orthogonal_basis(axis1)

    coords = []
    for vec in centered:
        x = _dot(vec, axis1)
        y = _dot(vec, axis2)
        coords.append((x, y))

    return _scale_coords(coords)


def _scale_coords(coords: list[tuple[float, float]]) -> list[tuple[float, float]]:
    count = len(coords)
    if count == 0:
        return []

    median_x = _median([coord[0] for coord in coords])
    median_y = _median([coord[1] for coord in coords])
    distances = [math.hypot(coord[0] - median_x, coord[1] - median_y) for coord in coords]
    max_distance = max(distances) if distances else 0.0
    if max_distance < 1e-6:
        return _circle_coords(count)

    target_radius = min(CANVAS_WIDTH, CANVAS_HEIGHT) / 2 - PADDING
    radius_ref = _trimmed_max(distances, trim_count=2)
    if radius_ref < 1e-6:
        radius_ref = max_distance

    scale = target_radius / radius_ref

    scaled = []
    for (x, y), distance in zip(coords, distances):
        if distance > 0:
            clamped = min(distance, radius_ref)
            ratio = (clamped / distance) * scale
            x = (x - median_x) * ratio + CANVAS_WIDTH / 2
            y = (y - median_y) * ratio + CANVAS_HEIGHT / 2
        else:
            x = CANVAS_WIDTH / 2
            y = CANVAS_HEIGHT / 2
        scaled.append((x, y))
    return scaled


def _circle_coords(count: int) -> list[tuple[float, float]]:
    if count == 1:
        return [(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2)]
    radius = min(CANVAS_WIDTH, CANVAS_HEIGHT) * 0.35
    center_x = CANVAS_WIDTH / 2
    center_y = CANVAS_HEIGHT / 2
    coords = []
    for index in range(count):
        angle = 2 * math.pi * index / count
        coords.append(
            (
                center_x + math.cos(angle) * radius,
                center_y + math.sin(angle) * radius,
            )
        )
    return coords


def _circle_layout(user_ids: list[str]) -> dict[str, tuple[float, float]]:
    coords = _circle_coords(len(user_ids))
    return {user_id: coords[index] for index, user_id in enumerate(user_ids)}


def _all_zero(vectors: list[list[float]]) -> bool:
    for vec in vectors:
        for value in vec:
            if abs(value) > 1e-12:
                return False
    return True


def _percentile(values: list[float], quantile: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    index = (len(ordered) - 1) * quantile
    low = int(math.floor(index))
    high = int(math.ceil(index))
    if low == high:
        return ordered[low]
    weight = index - low
    return ordered[low] * (1 - weight) + ordered[high] * weight


def _median(values: list[float]) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    mid = len(ordered) // 2
    if len(ordered) % 2 == 1:
        return ordered[mid]
    return (ordered[mid - 1] + ordered[mid]) / 2


def _trimmed_max(values: list[float], trim_count: int) -> float:
    if not values:
        return 0.0
    if len(values) <= trim_count:
        return max(values)
    ordered = sorted(values)
    trimmed = ordered[:-trim_count]
    return max(trimmed) if trimmed else max(values)


def _dot(a: list[float], b: list[float]) -> float:
    return sum(x * y for x, y in zip(a, b))


def _normalize(vec: list[float]) -> list[float] | None:
    norm = math.sqrt(sum(value * value for value in vec))
    if norm < 1e-12:
        return None
    return [value / norm for value in vec]


def _power_iteration(
    centered: list[list[float]],
    orthogonal_to: list[float] | None = None,
    iterations: int = 12,
) -> list[float] | None:
    dim = len(centered[0])
    initial = [0.0] * dim
    for vec in centered:
        for i, value in enumerate(vec):
            initial[i] += value

    if orthogonal_to is not None:
        projection = _dot(initial, orthogonal_to)
        initial = [initial[i] - projection * orthogonal_to[i] for i in range(dim)]

    vector = _normalize(initial)
    if vector is None:
        vector = _normalize(centered[0])
    if vector is None:
        return None

    for _ in range(iterations):
        vector = _matvec(centered, vector)
        if orthogonal_to is not None:
            projection = _dot(vector, orthogonal_to)
            vector = [vector[i] - projection * orthogonal_to[i] for i in range(dim)]
        normalized = _normalize(vector)
        if normalized is None:
            return None
        vector = normalized
    return vector


def _matvec(centered: list[list[float]], vec: list[float]) -> list[float]:
    dim = len(vec)
    result = [0.0] * dim
    for row in centered:
        dot = _dot(row, vec)
        if dot == 0.0:
            continue
        for i in range(dim):
            result[i] += row[i] * dot
    scale = 1.0 / max(len(centered), 1)
    return [value * scale for value in result]


def _orthogonal_basis(axis: list[float]) -> list[float]:
    dim = len(axis)
    min_index = 0
    min_value = abs(axis[0])
    for i in range(1, dim):
        value = abs(axis[i])
        if value < min_value:
            min_index = i
            min_value = value
    basis = [0.0] * dim
    basis[min_index] = 1.0
    projection = _dot(basis, axis)
    basis = [basis[i] - projection * axis[i] for i in range(dim)]
    normalized = _normalize(basis)
    return normalized if normalized is not None else axis
