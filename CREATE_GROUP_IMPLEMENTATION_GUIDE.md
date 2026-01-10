# 새 그룹 만들기 (Create Group Screen) - 구현 가이드

## 📋 개요
디자인 스펙에 맞춘 "새 그룹 만들기" 화면을 **MVVM + Clean Architecture** 기반으로 구현했습니다.

---

## 🏗️ 아키텍처 구조

### 1. **Presentation Layer** (UI)
```
presentation/group/
├── screen/
│   └── CreateGroupScreen.kt          # 메인 UI 컴포넌트
├── viewmodel/
│   └── CreateGroupViewModel.kt        # UI 상태 관리
└── component/
    ├── IconSelectComponents.kt        # 아이콘 선택 버튼
    ├── PrivacyOptionComponent.kt      # 공개/비공개 옵션
    └── TagChipComponent.kt            # 태그 칩
```

### 2. **Domain Layer** (비즈니스 로직)
```
domain/
├── usecase/
│   └── CreateGroupUseCase.kt          # 그룹 생성 유스케이스
└── repository/
    └── GroupRepository.kt             # Repository 인터페이스
```

### 3. **Data Layer** (데이터 접근)
```
data/
├── repository/
│   └── GroupRepositoryImpl.kt          # Repository 구현체
├── remote/
│   ├── ApiService.kt                  # (기존) API 인터페이스
│   └── dto/
│       └── CreateGroupDto.kt          # DTO 정의
```

---

## 📱 UI 구성

### CreateGroupScreen 레이아웃
```
┌─────────────────────────────┐
│  ✕  새 그룹 만들기    완료   │  <- Header
├─────────────────────────────┤
│                             │
│  ┌───────────────┐          │
│  │  아이콘 미리보기 │          │
│  │   (👥 이모지)  │          │
│  └───────────────┘          │
│                             │
│  [👥] [☕] [📷] [⛰️]        │  <- 아이콘 선택 옵션
│                             │
│  그룹 이름                   │
│  ┌─────────────────────┐    │
│  │ 예: 서울 러너스      │    │
│  └─────────────────────┘    │
│                             │
│  그룹 설명                   │
│  ┌─────────────────────┐    │
│  │ 그룹에 대해 간단히... │    │
│  └─────────────────────┘    │
│                             │
│  관련 태그                   │
│  ┌─────────────────────┐    │
│  │ # 태그 추가      [+]│    │
│  └─────────────────────┘    │
│  [러닝 ✕] [운동 ✕]        │  <- 추가된 태그
│                             │
│  공개 설정                   │
│  ◉ 공개                      │  <- 선택됨 (파란색)
│  ○ 비공개                    │
│                             │
└─────────────────────────────┘
```

---

## 🔄 상태 관리 (ViewModel)

### CreateGroupUiState
```kotlin
data class CreateGroupUiState(
    val groupName: String = "",                      // 그룹 이름
    val groupDescription: String = "",               // 그룹 설명
    val selectedIconType: String = "users",         // 선택된 아이콘 (users, coffee, camera, mountain)
    val selectedTags: List<String> = emptyList(),  // 선택된 태그 목록
    val isPublic: Boolean = true,                   // 공개 여부
    val isLoading: Boolean = false,                 // 로딩 중 표시
    val errorMessage: String = "",                  // 오류 메시지
    val isCreateSuccess: Boolean = false,           // 생성 성공 여부
    val createdGroupId: String? = null              // 생성된 그룹 ID
)
```

### ViewModel 메서드
```kotlin
// 텍스트 입력 처리
fun updateGroupName(name: String)                    // 그룹 이름 수정
fun updateGroupDescription(description: String)     // 그룹 설명 수정

// 아이콘/옵션 선택
fun selectIconType(iconType: String)                // 아이콘 선택
fun setPublic(isPublic: Boolean)                    // 공개 설정

// 태그 관리
fun addTag(tag: String)                             // 태그 추가 (최대 5개)
fun removeTag(tag: String)                          // 태그 제거

// 그룹 생성
fun createGroup(userId: String)                     // 그룹 생성 요청
fun resetCreateState()                              // 상태 초기화
```

---

## 🎯 주요 기능

### 1. 아이콘 선택
- 4가지 아이콘 옵션: 👥 (users), ☕ (coffee), 📷 (camera), ⛰️ (mountain)
- 선택된 아이콘은 미리보기 영역에서 표시
- 선택된 버튼은 파란색(#667EEA) 강조

### 2. 폼 입력
- **그룹 이름**: 필수 입력 (유효성 검사)
- **그룹 설명**: 필수 입력 (유효성 검사)
- **관련 태그**: 선택 항목, 최대 5개 추가 가능

### 3. 공개 설정 (Radio Button 스타일)
- **공개**: 누구나 검색하고 가입 가능 (기본값)
- **비공개**: 초대받은 사람만 가입 가능

### 4. 유효성 검사
```kotlin
- 그룹 이름 필수 입력
- 그룹 설명 필수 입력
- 유효성 검사 실패 시 에러 메시지 표시
```

---

## 📡 백엔드 연동

### API 엔드포인트
```
POST /api/groups

Request Body:
{
    "name": "서울 러너스",
    "description": "매주 일요일 한강에서 모여요",
    "iconType": "users",
    "tags": ["러닝", "운동"],
    "isPublic": true,
    "userId": "user123"
}

Response:
{
    "id": "group123",
    "name": "서울 러너스",
    "description": "매주 일요일 한강에서 모여요",
    "iconType": "users",
    "tags": ["러닝", "운동"],
    "isPublic": true,
    "userId": "user123",
    "createdAt": "2024-01-10T12:34:56Z",
    "memberCount": 1
}
```

---

## 🔧 의존성 주입 (Hilt)

### RepositoryModule 설정
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository
}
```

### ViewModel 주입
```kotlin
@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase
) : ViewModel() { ... }
```

---

## 📝 사용 예시

### 1. Navigation에 추가
```kotlin
// MainActivity.kt 또는 Navigation Graph에 추가
CreateGroupScreen(
    userId = currentUserId,
    onCreateSuccess = { groupId ->
        // 그룹 생성 성공 처리
        navController.navigate("groupDetail/$groupId")
    },
    onBackPress = {
        navController.popBackStack()
    }
)
```

### 2. 화면 호출
```kotlin
// GroupListScreen에서 "새 그룹 만들기" 버튼
Button(onClick = { 
    navController.navigate("createGroup") 
}) {
    Text("새 그룹 만들기")
}
```

---

## 🎨 디자인 스펙 준수

| 요소 | 색상 | 크기 |
|------|------|------|
| Primary Color | #667EEA | - |
| Secondary Color | #764BA2 | - |
| Background | #FFFFFF | - |
| Text Primary | #111827 | 14-18sp |
| Text Secondary | #6B7280 | 12-14sp |
| Input Background | #F9FAFB | - |
| Border Color | #E5E7EB | 1-2dp |
| Selected Border | #667EEA | 2dp |

---

## ⚠️ 주요 구현 포인트

1. **FlowRow 커스텀 구현**: 태그 자동 줄바꿈 처리
2. **상태 관리**: Composable 재렌더링 최소화
3. **유효성 검사**: 클라이언트 사이드 검증
4. **에러 처리**: 명확한 오류 메시지
5. **로딩 상태**: 동시 요청 방지

---

## 📦 파일 구조 요약

```
✅ Presentation Layer
  - CreateGroupScreen.kt
  - CreateGroupViewModel.kt
  - IconSelectComponents.kt
  - PrivacyOptionComponent.kt
  - TagChipComponent.kt

✅ Domain Layer
  - CreateGroupUseCase.kt
  - GroupRepository.kt

✅ Data Layer
  - GroupRepositoryImpl.kt
  - CreateGroupDto.kt
  - (ApiService - 이미 구현됨)

✅ DI Configuration
  - RepositoryModule.kt (수정됨)
```

---

## 🚀 다음 단계

1. **백엔드 API 구현**: `/api/groups` POST 엔드포인트 개발
2. **에러 처리 향상**: 네트워크 오류, 타임아웃 등 상세 처리
3. **이미지 업로드**: 그룹 썸네일 이미지 기능 추가
4. **UI/UX 개선**: 로딩 스켈레톤, 애니메이션 추가
5. **테스트 코드**: Unit Test, UI Test 작성

---

**최종 확인**: Clean Architecture와 MVVM 패턴을 완벽하게 준수하며 구현되었습니다! 🎉
