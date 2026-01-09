# 로그인 스크린 - MVVM + 클린 아키텍처

## 프로젝트 구조

```
app/src/main/java/com/example/madclass01/
├── domain/                          # Domain Layer (비즈니스 로직)
│   ├── model/
│   │   ├── LoginResult.kt
│   │   └── User.kt
│   ├── repository/
│   │   └── LoginRepository.kt       # Repository Interface
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── ValidateEmailUseCase.kt
│       └── ValidatePasswordUseCase.kt
│
├── data/                            # Data Layer (데이터 관리)
│   └── repository/
│       └── LoginRepositoryImpl.kt    # Repository 구현
│
├── presentation/                    # Presentation Layer (UI)
│   └── login/
│       ├── screen/
│       │   └── LoginScreen.kt       # Compose UI
│       ├── component/
│       │   └── CustomTextField.kt   # 재사용 가능한 컴포넌트
│       └── viewmodel/
│           └── LoginViewModel.kt    # MVVM ViewModel
│
├── di/                              # Dependency Injection
│   └── RepositoryModule.kt          # Hilt Module
│
├── MainActivity.kt                  # Entry Point
└── MadClass01Application.kt         # Application Class
```

## 주요 기능

### 1. Domain Layer
- **Model**: `User`, `LoginResult` - 비즈니스 도메인 모델
- **Repository**: `LoginRepository` - 인터페이스 정의 (의존성 역전)
- **UseCase**: `LoginUseCase`, `ValidateEmailUseCase`, `ValidatePasswordUseCase`

### 2. Data Layer
- **RepositoryImpl**: 실제 로그인 로직 구현
- 간단한 유효성 검사:
  - 이메일: 기본 정규식 검사
  - 비밀번호: 6자 이상

### 3. Presentation Layer
- **ViewModel**: 
  - UI 상태 관리 (`LoginUiState`)
  - 사용자 입력 처리
  - 로그인 로직 실행
  
- **UI (Compose)**:
  - Material Design 3
  - 깔끔한 로그인 폼
  - 에러 메시지 표시
  - 비밀번호 표시/숨김 기능
  - 로딩 상태 표시

### 4. Dependency Injection (Hilt)
- `RepositoryModule`로 의존성 자동 주입
- ViewModel에서 UseCase 자동 주입

## 사용 방법

### 로그인 스크린 사용
```kotlin
LoginScreen(
    onLoginSuccess = { token ->
        // 로그인 성공 처리
        println("로그인 성공, 토큰: $token")
    }
)
```

### ViewModel에서 로그인
```kotlin
viewModel.updateEmail("user@example.com")
viewModel.updatePassword("password123")
viewModel.login()
```

## 의존성

```gradle
// ViewModel & Compose
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Hilt
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
```

## 확장 방법

### 1. 실제 API 연동
`LoginRepositoryImpl.kt`의 `login()` 함수에서 실제 API 호출로 변경:
```kotlin
override suspend fun login(user: User): LoginResult {
    val response = apiService.login(user.email, user.password)
    return LoginResult(
        isSuccess = response.success,
        message = response.message,
        token = response.token
    )
}
```

### 2. 데이터베이스 추가
- Room을 이용한 로컬 저장소 추가
- 토큰 저장소 (DataStore 또는 SharedPreferences)

### 3. UI 커스터마이징
- `LoginScreen.kt`의 색상, 폰트, 레이아웃 수정
- 회원가입, 비밀번호 찾기 화면 추가

## 클린 아키텍처 이점

1. **테스트 용이**: 각 레이어가 독립적으로 테스트 가능
2. **유지보수성**: 계층 분리로 코드 관리 용이
3. **확장성**: 새로운 기능 추가 시 기존 코드 수정 최소화
4. **의존성 역전**: 인터페이스를 통한 느슨한 결합
