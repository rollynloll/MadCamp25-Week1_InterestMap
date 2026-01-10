# 👥 그룹 상세 화면 (Group Detail Screen) - 구현 가이드

## 📋 개요

**그룹 상세 화면**은 사용자가 접속한 그룹의 멤버들과의 관계를 시각화하는 화면입니다.
- **핵심 기능**: 관계 그래프 (Relationship Graph)
- **기술**: 코사인 유사도(Cosine Similarity) 기반 거리 계산
- **아키텍처**: MVVM + Clean Architecture

---

## 🎯 주요 기능

### 1. 관계 그래프 (Relationship Graph)
```
중앙: 현재 사용자 (나)
  ↓
주변: 그룹 내 다른 사용자들
  ↓
거리: 코사인 유사도 기반
  - 가까울수록 취향이 유사 (유사도 높음)
  - 멀어질수록 취향이 다름 (유사도 낮음)
```

### 2. 노드 배치 시스템
- **중앙 노드**: 현재 사용자 (72x72px, 그라데이션)
- **주변 노드**: 다른 사용자들 (크기: 40~56px, 색상: 유사도 기반)
- **배치 방식**: 원형 배치 (균등 분산)

### 3. 색상 시스템 (유사도 기반)
| 유사도 | 색상 | 의미 |
|--------|------|------|
| 0.7+ | #10B981 (초록색) | 매우 유사한 취향 |
| 0.5~0.7 | #10B981 (초록색) | 유사한 취향 |
| 0.3~0.5 | #F59E0B (주황색) | 보통 유사도 |
| <0.3 | #E5E7EB (회색) | 낮은 유사도 |

### 4. 채팅 기능
- **개인 채팅**: 노드 클릭 → 선택 사용자와 채팅
- **그룹 채팅**: 아무도 선택 안 됨 → 전체 그룹과 채팅

---

## 🏗️ 아키텍처 구조

### Domain Layer
```
domain/
├── model/
│   └── UserEmbedding.kt          # 사용자 임베딩 모델
│   └── GraphNodePosition.kt       # 그래프 노드 위치
│   └── RelationshipGraph.kt       # 관계 그래프 모델
├── repository/
│   └── GroupDetailRepository.kt   # Repository 인터페이스
└── usecase/
    ├── GetGroupDetailUseCase.kt
    └── GetRelationshipGraphUseCase.kt
```

### Data Layer
```
data/
├── remote/
│   └── dto/
│       └── GroupDetailDto.kt      # DTO (Request/Response)
├── repository/
│   └── GroupDetailRepositoryImpl.kt # Repository 구현
└── (ApiService.kt 확장)
```

### Presentation Layer
```
presentation/group/
├── screen/
│   └── GroupDetailScreen.kt       # 메인 스크린
├── viewmodel/
│   └── GroupDetailViewModel.kt    # 상태 관리
└── component/
    ├── RelationshipGraphComponent.kt # 그래프 렌더링
    ├── GroupDetailHeaderComponent.kt # 헤더
    └── ChatButtonComponent.kt        # 채팅 버튼
```

### Utilities
```
utils/
├── SimilarityCalculator.kt        # 코사인 유사도 계산
└── GraphLayoutCalculator.kt       # 노드 위치 계산
```

---

## 🔄 데이터 흐름

### 화면 로드 시 (초기화)
```
GroupDetailScreen
    ↓
LaunchedEffect: initializeWithGroup(groupId, currentUserId)
    ↓
GroupDetailViewModel
    ├─→ GetGroupDetailUseCase(groupId)
    │   └─→ ApiService.getGroupDetail()
    │       → GroupDetailResponse → Group (Domain Model)
    │
    └─→ GetRelationshipGraphUseCase(groupId, currentUserId)
        └─→ GroupDetailRepositoryImpl.getRelationshipGraph()
            ├─→ ApiService.getGroupUserEmbeddings()
            │   → GroupEmbeddingResponse
            │
            └─→ GraphLayoutCalculator.calculateNodePositions()
                ├─→ SimilarityCalculator.cosineSimilarity()
                │   (각 사용자 vs 현재 사용자)
                │
                └─→ 극좌표 → 직교좌표 변환
                    (원형 배치, 유사도 기반 거리)
```

### 사용자 노드 클릭
```
OtherUserNodeComponent.clickable()
    ↓
onNodeClick(userId)
    ↓
viewModel.selectUser(userId)
    ↓
uiState.selectedUserId = userId
    ↓
ChatButtonComponent 업데이트
    ("사용자명과 채팅" 표시)
```

### 채팅 버튼 클릭
```
ChatButtonComponent.clickable()
    ↓
viewModel.startChatWithSelectedUser() or startGroupChat()
    ↓
uiState.chatRoomId = 생성된 ID
    ↓
LaunchedEffect: onChatRoomCreated(chatRoomId)
    ↓
네비게이션: ChatScreen으로 이동
```

---

## 📊 코사인 유사도 계산 상세

### 수식
```
Cosine Similarity = (A · B) / (||A|| × ||B||)

A · B = a₁b₁ + a₂b₂ + ... + aₙbₙ (내적)
||A|| = √(a₁² + a₂² + ... + aₙ²) (크기)
||B|| = √(b₁² + b₂² + ... + bₙ²) (크기)

범위: 0 (완전히 다름) ~ 1 (동일)
```

### 구현 예시
```kotlin
fun cosineSimilarity(vector1: List<Float>, vector2: List<Float>): Float {
    // 1. 내적 계산
    val dotProduct = vector1.zip(vector2).sumOf { (a, b) -> (a * b).toDouble() }
    
    // 2. 크기 계산
    val magnitude1 = sqrt(vector1.sumOf { (it * it).toDouble() })
    val magnitude2 = sqrt(vector2.sumOf { (it * it).toDouble() })
    
    // 3. 코사인 유사도
    return (dotProduct / (magnitude1 * magnitude2)).toFloat()
}
```

### 유사도 → 거리 변환
```kotlin
fun similarityToPixelDistance(similarity: Float, maxDistance: Float = 150f): Float {
    // 유사도가 높을수록 (1에 가까울수록) 거리가 짧음
    // 유사도가 0.5이면 maxDistance의 절반
    return (1f - similarity) * maxDistance
}

예시:
- 유사도 0.9 → 거리 15px (매우 가깝다)
- 유사도 0.5 → 거리 75px (중간)
- 유사도 0.1 → 거리 135px (멀다)
```

---

## 🎨 UI 구조

### 화면 레이아웃
```
┌──────────────────────────────┐
│ ← Header    QR Code ⋮        │  ← 200px (그라데이션)
│    👥  그룹명                 │
│    24명 · 오늘 활동           │
├──────────────────────────────┤
│                              │
│         나                    │  ← 중앙 노드 (72x72)
│       🟢 🟢 🟢               │  ← 주변 노드들 (40~56px)
│         🟡 🟡 🟡             │
│         🟤 🟤 🟤             │
│                              │
│      520px (Graph Canvas)    │
├──────────────────────────────┤
│   [💬 사용자명과 채팅]         │  ← 채팅 버튼 (53px)
└──────────────────────────────┘
```

### 헤더 구성
- **배경**: 그라데이션 (#10B981 → #059669)
- **왼쪽**: 뒤로가기 버튼 (←)
- **중앙**: 그룹 아이콘 (80x80, 흰색 배경), 그룹명, 멤버 수, 활동 상태
- **오른쪽**: QR 코드 아이콘 (⌨), 더보기 아이콘 (⋮)

### 그래프 캔버스
- **배경**: #FAFBFC (밝은 회색)
- **중앙 노드**: 72x72px, 그라데이션, "나" 텍스트
- **주변 노드들**: 크기 및 색상은 유사도 기반
  - 크기: 40~56px (유사도 0~1)
  - 색상: 초록/주황/회색 (유사도 3단계)

---

## 📡 API 엔드포인트

### 1. 그룹 상세 정보 조회
```http
GET /api/groups/{groupId}/detail

Response (200 OK):
{
    "id": "group123",
    "name": "서울 러너스",
    "description": "달리기를 좋아하는 사람들의 모임",
    "iconType": "people",
    "memberCount": 24,
    "isPublic": true,
    "createdByUserId": "user123",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-10T12:00:00Z",
    "profileImageUrl": null,
    "activityStatus": "오늘 활동"
}
```

### 2. 그룹 사용자 임베딩 조회
```http
GET /api/groups/{groupId}/embeddings

Response (200 OK):
{
    "groupId": "group123",
    "currentUserId": "user123",
    "currentUserEmbedding": {
        "userId": "user123",
        "userName": "현재사용자",
        "profileImageUrl": "https://...",
        "embeddingVector": [0.1, 0.2, ..., 0.5],  // N차원 벡터
        "activityStatus": "활동중"
    },
    "otherUserEmbeddings": [
        {
            "userId": "user456",
            "userName": "김OO",
            "profileImageUrl": "https://...",
            "embeddingVector": [0.12, 0.19, ..., 0.48],
            "activityStatus": "활동중"
        },
        // ... 더 많은 사용자들
    ]
}
```

### 3. 개별 사용자 임베딩 조회
```http
GET /api/users/{userId}/embedding

Response (200 OK):
{
    "userId": "user123",
    "userName": "사용자명",
    "profileImageUrl": "https://...",
    "embeddingVector": [0.1, 0.2, ..., 0.5],
    "activityStatus": "활동중"
}
```

---

## 🚀 사용 방법

### 1. Navigation에 추가
```kotlin
composable("groupDetail/{groupId}") { backStackEntry ->
    val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
    
    GroupDetailScreen(
        groupId = groupId,
        currentUserId = getCurrentUserId(),  // 현재 로그인한 사용자
        onBackPress = { navController.popBackStack() },
        onQRCodeClick = { navController.navigate("qrInvite/$groupId") },
        onChatRoomCreated = { chatRoomId ->
            navController.navigate("chat/$chatRoomId")
        }
    )
}
```

### 2. 그룹 목록에서 호출
```kotlin
Button(onClick = {
    navController.navigate("groupDetail/${group.id}")
}) {
    Text("그룹 상세 보기")
}
```

---

## 🔧 설정 및 의존성

### 의존성 (이미 추가됨)
```gradle
// Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Hilt
implementation("com.google.dagger:hilt-android")
kapt("com.google.dagger:hilt-compiler")
implementation("androidx.hilt:hilt-navigation-compose")

// Retrofit
implementation("com.squareup.retrofit2:retrofit")
implementation("com.squareup.okhttp3:okhttp")
```

### ViewModel 초기화
```kotlin
val viewModel: GroupDetailViewModel = hiltViewModel()
```

---

## 📊 상태 관리

### GroupDetailUiState
```kotlin
data class GroupDetailUiState(
    val group: Group? = null,                    // 그룹 정보
    val relationshipGraph: RelationshipGraph? = null,  // 관계 그래프
    val isLoading: Boolean = false,              // 로딩 중
    val errorMessage: String = "",               // 오류 메시지
    val selectedUserId: String? = null,          // 선택된 사용자
    val chatRoomId: String? = null              // 생성된 채팅 룸
)
```

### ViewModel 메서드
```kotlin
fun initializeWithGroup(groupId: String, currentUserId: String)
fun selectUser(userId: String)
fun deselectUser()
fun startChatWithSelectedUser(currentUserId: String)
fun startGroupChat(groupId: String)
fun resetChatState()
fun clearErrorMessage()
```

---

## 🎓 기술 포인트

### 1. 코사인 유사도
- **목적**: 사용자 간의 취향/관심사 유사도 측정
- **입력**: 사용자의 임베딩 벡터 (N차원)
- **출력**: 0~1 범위의 유사도 값
- **활용**: 노드 간 거리 결정

### 2. 원형 배치 (Circular Layout)
- **목적**: 균등하고 보기 좋은 노드 배치
- **방식**: 극좌표 사용 (각도, 거리)
- **공식**: 
  ```
  각도 = 2π × (index / 전체_노드_수)
  x = 중심X + 거리 × cos(각도)
  y = 중심Y + 거리 × sin(각도)
  ```

### 3. 동적 노드 크기
- **작은 노드** (40px): 유사도 낮음
- **큰 노드** (56px): 유사도 높음
- **공식**: `40 + (유사도 × 16)`

### 4. 채팅 룸 ID 생성
```kotlin
// 개인 채팅
val userIds = listOf(user1, user2).sorted()
val chatRoomId = "${userIds[0]}_${userIds[1]}"

// 그룹 채팅
val chatRoomId = "group_${groupId}"
```

---

## 📝 BackEnd 구현 체크리스트

- [ ] GET /api/groups/{groupId}/detail 구현
- [ ] GET /api/groups/{groupId}/embeddings 구현
- [ ] GET /api/users/{userId}/embedding 구현
- [ ] 임베딩 벡터 생성 로직
  - [ ] 사용자 행동 데이터 수집 (좋아요, 클릭, 보기 시간 등)
  - [ ] Embedding 모델 학습 (Word2Vec, Doc2Vec 등)
  - [ ] 사용자별 임베딩 벡터 저장
- [ ] 데이터베이스 스키마
  - [ ] Users 테이블에 embedding_vector 컬럼 추가
  - [ ] Groups 테이블 기본 구조

---

## 🧪 테스트 체크리스트

- [ ] 그룹 정보 로드 테스트
- [ ] 임베딩 데이터 로드 테스트
- [ ] 코사인 유사도 계산 정확도 테스트
- [ ] 노드 위치 계산 테스트 (화면 경계 내)
- [ ] 노드 클릭 반응성 테스트
- [ ] 채팅 버튼 상태 변화 테스트
- [ ] 에러 처리 테스트
- [ ] 로딩 상태 표시 테스트

---

## 🔐 보안 고려사항

1. **임베딩 벡터 노출**: 개인 취향 정보이므로 신중하게 처리
2. **권한 검증**: 그룹 멤버만 다른 사용자의 정보 볼 수 있도록
3. **네트워크 보안**: HTTPS 사용, API 요청 검증
4. **캐싱**: 임베딩 데이터는 민감하므로 캐싱 최소화

---

## 📈 성능 최적화

1. **데이터 로드**
   - 임베딩 벡터는 한 번만 로드
   - 필요시 메모리 캐싱 고려

2. **UI 렌더링**
   - LazyColumn으로 노드 렌더링 (노드가 많을 경우)
   - 그래프 다시 계산 최소화

3. **계산 최적화**
   - 코사인 유사도 사전 계산 (백엔드)
   - GraphLayout 계산은 필요할 때만

---

**상태**: ✅ 구현 완료  
**아키텍처**: MVVM + Clean Architecture  
**핵심 기술**: 코사인 유사도, 원형 배치, Compose
