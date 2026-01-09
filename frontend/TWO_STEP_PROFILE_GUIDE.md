# 2단계 프로필 설정 및 취향 분석 시스템 완료

## 🎯 전체 흐름

```
로그인 → Step 1: 프로필 설정 → 로딩 중 → Step 2: 취향 분석 결과 → 홈
```

---

## 📱 Step 1: 프로필 설정 화면

### 입력 항목
1. **닉네임** (필수) - 2자 이상
2. **나이** (필수) - 숫자만 입력
3. **지역** (필수) - 자유 입력
4. **취미** (선택) - 태그칩 형태로 추가/삭제 가능
5. **흥미** (선택) - 태그칩 형태로 추가/삭제 가능
6. **프로필 사진** (필수) - 최소 1개, 최대 20개

### 색상 스킴
- **포인트**: #FF9945 (주황색)
- **배경**: 흰색
- **텍스트**: 검정 (#1A1A1A)
- **보조**: 회색 (#999999)

### 태그칩 특징
- **추가**: TagInputField로 입력 후 + 버튼 클릭
- **삭제**: 각 태그의 X 버튼 클릭
- **디자인**: 선택되지 않으면 회색 배경, 선택되면 주황색 배경

---

## ⏳ 로딩 화면

### 애니메이션
- 원형 프로그레스바 (주황색)
- 로딩 닷 애니메이션
- 단계별 로딩 메시지:
  1. "이미지 분석 중입니다" (1초)
  2. "AI가 취향을 학습하고 있습니다" (1.5초)
  3. "핵심 키워드를 추출 중입니다" (1.5초)

### 특징
- 전체 소요시간: 약 4초
- 사용자에게 진행 상황 시각화
- 로딩 완료 후 자동으로 Step 2로 이동

---

## 🏷️ Step 2: 취향 분석 결과 및 태그 선택

### 헤더
- **배경색**: 보라색 (#6B5BE2)
- **제목**: "취향 분석 결과"
- **설명**: "AI가 분석한 취향이에요, 수정하거나 추가할 수 있어요"
- **뒤로가기 버튼**: Step 1로 돌아갈 수 있음

### 섹션 구성

#### 1. 자동 추출된 태그
- AI가 분석한 사진에서 추출한 태그
- 3개씩 행으로 배치
- 선택/해제 토글 가능
- 체크 표시로 선택 상태 표시

예시 태그:
- 감성 카페
- 러닝
- 필름 카메라
- 전시회
- 베이킹

#### 2. 직접 추가하기
- TagInputField로 새로운 태그 입력
- 최대 20자 제한
- 중복 방지

#### 3. 추천 태그 (사진 기반)
- AI가 추천하는 추가 태그들
- 사용자가 선택 여부 결정
- 마찬가지로 선택/해제 토글

예시 태그:
- 흩한 카페
- 등산
- 댕댕이

#### 4. 내가 추가한 태그
- 사용자가 직접 추가한 커스텀 태그
- 삭제 가능 (X 버튼)
- 항상 선택된 상태로 표시

---

## 🎨 태그칩 컴포넌트

### 상태 표시
```kotlin
// 선택 안 됨
배경: #F5F5F5 (회색)
텍스트: #333333 (검정)

// 선택됨
배경: #FF9945 (주황색)
텍스트: 흰색
좌측에 체크 아이콘 표시
```

### 상호작용
- **클릭**: 선택/해제 토글
- **X 버튼**: 태그 삭제 (커스텀 태그만 해당)

---

## 📊 아키텍처

### Domain Layer
```
model/
  ├── Tag.kt (id, name, category, isSelected)
  ├── TagAnalysisResult.kt (extractedTags, recommendedTags)
  └── UserProfile.kt (종합 프로필 정보)

usecase/
  ├── AnalyzeImagesUseCase.kt
  ├── AddTagUseCase.kt
  ├── RemoveTagUseCase.kt
  └── ToggleTagUseCase.kt
```

### Presentation Layer
```
profile/
  ├── screen/
  │   ├── ProfileSetupScreen.kt (Step 1)
  │   ├── LoadingScreen.kt
  │   └── TagSelectionScreen.kt (Step 2)
  ├── viewmodel/
  │   ├── ProfileSetupViewModel.kt
  │   └── TagSelectionViewModel.kt
  └── component/
      ├── ImageGalleryGrid.kt
      └── ImagePickerButton.kt

common/
  └── component/
      ├── TagChip.kt
      └── TagInputField.kt
```

---

## 🔄 데이터 플로우

### Step 1 → Loading → Step 2

```
ProfileSetupViewModel (Step 1)
  ↓ (proceedToNextStep)
  ↓ (프로필 데이터 저장)
  ↓
LoadingScreen
  ↓ (4초 로딩)
  ↓
TagSelectionViewModel (Step 2)
  ├── analyzeImages() 호출
  ├── AnalyzeImagesUseCase 실행
  └── TagAnalysisResult 수신
      ├── extractedTags (자동 추출)
      └── recommendedTags (추천)
```

### Step 2 상태 관리

```
사용자 상호작용
  ↓
ViewModel 메서드 호출
  ├── toggleExtractedTag()
  ├── toggleRecommendedTag()
  ├── addCustomTag()
  └── removeCustomTag()
  ↓
StateFlow 업데이트
  ↓
UI 자동 리컴포지션
```

---

## 🎯 주요 기능

### Step 1
- ✅ 기본 정보 입력 (닉네임, 나이, 지역)
- ✅ 취미/흥미 태그 추가/삭제
- ✅ 프로필 사진 선택 (최대 20개)
- ✅ 실시간 유효성 검사
- ✅ 다음 버튼으로 진행

### Loading
- ✅ AI 분석 시뮬레이션
- ✅ 단계별 로딩 메시지
- ✅ 애니메이션 (프로그레스바, 닷)

### Step 2
- ✅ 자동 추출 태그 선택/해제
- ✅ 추천 태그 선택/해제
- ✅ 커스텀 태그 추가/삭제
- ✅ 선택된 태그 개수 추적
- ✅ 완료하고 시작하기

---

## 💡 확장 방법

### AI 이미지 분석 통합
```kotlin
// AnalyzeImagesUseCase.kt 수정
suspend operator fun invoke(images: List<ImageItem>): TagAnalysisResult {
    // 실제 이미지 분석 API 호출
    val response = imageAnalysisService.analyze(images)
    return response.toTagAnalysisResult()
}
```

### 서버 동기화
```kotlin
// 완료 시 서버로 전송
fun completeProfile(
    nickname: String,
    age: Int,
    region: String,
    selectedTags: List<Tag>
): Result<Unit>
```

### 데이터베이스 저장
```kotlin
// Room을 이용한 로컬 저장
@Entity
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val age: Int,
    val region: String,
    val selectedTags: String // JSON 저장
)
```

---

## 📝 주의사항

1. **Coil 라이브러리**: 이미지 로딩에 필수
2. **Hilt**: 의존성 주입 프레임워크 필요
3. **Coroutines**: 비동기 작업 처리
4. **Material Design 3**: UI 컴포넌트 기반

---

## 🎨 색상 정리

| 용도 | 색상코드 | 설명 |
|------|---------|------|
| 포인트 | #FF9945 | 주황색 (태그, 버튼) |
| 헤더 (Step2) | #6B5BE2 | 보라색 |
| 배경 | #FFFFFF | 흰색 |
| 텍스트 | #1A1A1A | 검정 |
| 보조 텍스트 | #999999 | 회색 |
| 에러 | #D32F2F | 빨강 |
| 입력필드 테두리 | #DDDDDD | 라이트 그레이 |

---

## 📦 파일 구조 요약

```
app/src/main/java/com/example/madclass01/
├── domain/
│   ├── model/ (Tag, TagAnalysisResult, UserProfile)
│   └── usecase/ (Analyze, AddTag, RemoveTag, ToggleTag)
├── data/
│   └── repository/
├── presentation/
│   ├── login/
│   ├── profile/
│   │   ├── screen/ (ProfileSetup, Loading, TagSelection)
│   │   ├── component/ (ImageGallery, ImagePicker)
│   │   └── viewmodel/ (ProfileSetup, TagSelection)
│   └── common/
│       └── component/ (TagChip, TagInputField)
└── MainActivity.kt
```

---

완성되었습니다! 🎉

모든 UI가 주황색 포인트컬러 (#FF9945)로 통일되어 있으며, 
흰색 배경과 검정 텍스트로 깔끔하게 디자인되었습니다.
