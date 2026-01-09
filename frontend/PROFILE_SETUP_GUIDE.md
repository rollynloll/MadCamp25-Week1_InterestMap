# 프로필 설정 화면 구현 완료

## 📱 기능 설명

### 프로필 설정 화면 (ProfileSetupScreen)
로그인 후 사용자의 기초 정보를 입력받는 화면입니다.

#### 입력 항목
1. **닉네임** - 2자 이상 필수
2. **자기소개** - 500자 이내 (선택사항)
3. **프로필 사진** - 최소 1개, 최대 20개 선택

---

## 🗂️ 파일 구조

```
app/src/main/java/com/example/madclass01/
├── domain/
│   ├── model/
│   │   ├── Profile.kt              ✨ 새로 추가
│   │   └── ImageItem.kt            ✨ 새로 추가
│   └── usecase/
│       ├── AddImageUseCase.kt       ✨ 새로 추가
│       └── RemoveImageUseCase.kt    ✨ 새로 추가
│
├── presentation/
│   └── profile/                     ✨ 새로 추가
│       ├── screen/
│       │   └── ProfileSetupScreen.kt
│       ├── component/
│       │   ├── ImagePickerButton.kt
│       │   └── ImageGalleryGrid.kt
│       └── viewmodel/
│           └── ProfileSetupViewModel.kt
│
└── MainActivity.kt                  (수정됨)
```

---

## 🔑 주요 기능

### 1. 이미지 선택 기능
- **+ 버튼**: 갤러리에서 이미지 선택
- **최대 20개 제한**: 자동으로 중복 방지
- **이미지 미리보기**: 3x3 그리드로 표시
- **삭제 기능**: 각 이미지 오른쪽 상단의 X 버튼으로 삭제

### 2. 유효성 검사
```kotlin
// 닉네임 검증
- 필수 입력
- 최소 2자 이상

// 이미지 검증
- 최소 1개 필수
- 최대 20개 제한
- 중복 방지
```

### 3. 상태 관리 (StateFlow)
```kotlin
data class ProfileSetupUiState(
    val nickname: String = "",
    val bio: String = "",
    val images: List<ImageItem> = emptyList(),
    val imageCountText: String = "0/20",
    val nicknameError: String = "",
    val errorMessage: String = "",
    val isProfileComplete: Boolean = false
)
```

### 4. ViewModel 메서드
```kotlin
// 입력값 업데이트
fun updateNickname(newNickname: String)
fun updateBio(newBio: String)

// 이미지 관리
fun addImage(imageUri: String, imageName: String, imageSize: Long)
fun removeImage(imageUri: String)

// 프로필 완료
fun completeProfile()
```

---

## 🎨 UI 특징

### 레이아웃
- **헤더**: 제목 및 설명 (Padding 포함)
- **입력 필드**: Material Design 3 스타일
  - 닉네임: 단일 라인 입력
  - 자기소개: 다중 라인 입력 (120dp)
- **이미지 섹션**: 
  - 선택 가능 이미지 개수 표시 (0/20)
  - 3x3 그리드 레이아웃
  - 각 항목 100x100dp

### 색상 스킴
- **메인 색상**: #5C6BC0 (파란색)
- **배경색**: #FBEBEE (이미지 추가 버튼)
- **에러색**: #D32F2F (빨간색)
- **텍스트**: #1A1A1A (검정), #999999 (회색)

### 상호작용
- **+ 버튼 클릭**: 갤러리 열기
- **이미지 X 버튼**: 해당 이미지 삭제
- **프로필 완료 버튼**: 유효성 검사 후 완료

---

## 🔄 데이터 흐름

```
사용자 입력 (이미지 선택)
      ↓
ProfileSetupScreen
      ↓
ViewModel.addImage()
      ↓
AddImageUseCase (최대 20개 검증)
      ↓
ProfileSetupUiState 업데이트
      ↓
ImageGalleryGrid 리컴포지션
      ↓
선택된 이미지 화면에 표시
```

---

## 📸 이미지 처리

### 갤러리 접근
```kotlin
val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        val fileName = getFileName(context, it)
        viewModel.addImage(it.toString(), fileName)
    }
}
```

### Coil을 통한 이미지 로딩
```kotlin
AsyncImage(
    model = imageUri,
    contentDescription = "프로필 이미지",
    contentScale = ContentScale.Crop
)
```

---

## 🔐 권한 설정 (AndroidManifest.xml)

```xml
<!-- Android 13+ (API 33+) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Android 12 이하 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

---

## 🚀 네비게이션 흐름

```
LoginScreen (로그인)
     ↓
     ↓ (onLoginSuccess)
     ↓
ProfileSetupScreen (프로필 설정)
     ↓
     ↓ (onProfileComplete)
     ↓
HomeScreen (홈)
```

---

## 📦 새로운 의존성

```gradle
// Coil for image loading
implementation("io.coil-kt:coil-compose:2.4.0")

// Navigation Compose
implementation("androidx.navigation:navigation-compose:2.7.6")
```

---

## ✨ 특징 요약

✅ **MVVM 아키텍처** 유지  
✅ **클린 아키텍처** 원칙 준수  
✅ **최대 20개 이미지** 선택 제한  
✅ **갤러리 통합** (ActivityResultContracts)  
✅ **실시간 유효성 검사**  
✅ **자동 중복 방지**  
✅ **반응형 UI** (StateFlow)  
✅ **Material Design 3** 준수  

---

## 🔧 확장 방법

### API 연동
```kotlin
// 프로필 완료 시 서버로 전송
suspend fun saveProfile(
    token: String,
    nickname: String,
    bio: String,
    images: List<ImageItem>
): Result<Unit>
```

### 데이터베이스 저장
```kotlin
// Room을 이용한 로컬 저장
@Entity
data class ProfileEntity(
    @PrimaryKey val id: Int,
    val nickname: String,
    val bio: String,
    val imageUris: List<String>
)
```

### 이미지 압축
```kotlin
// 큰 이미지 자동 압축
fun compressImage(imageUri: Uri): Bitmap
```

완성되었습니다! 🎉
