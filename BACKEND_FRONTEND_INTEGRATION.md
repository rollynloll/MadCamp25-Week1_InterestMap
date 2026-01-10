# Backend-Frontend 연동 가이드

## 📋 개요
Android 앱과 FastAPI 백엔드가 연결되었습니다!

## 🔧 설정 완료 사항

### 1. 의존성 추가 ✅
- Retrofit 2.9.0
- OkHttp 4.12.0
- Gson Converter

### 2. 네트워크 권한 추가 ✅
- INTERNET 권한
- ACCESS_NETWORK_STATE 권한

### 3. 생성된 파일들
```
frontend/app/src/main/java/com/example/madclass01/
├── data/
│   ├── remote/
│   │   ├── ApiService.kt          # API 엔드포인트 정의
│   │   └── dto/
│   │       └── ApiDtos.kt         # 데이터 모델
│   └── repository/
│       └── BackendRepository.kt   # API 호출 로직
└── di/
    └── NetworkModule.kt           # Retrofit & DI 설정
```

## 🚀 사용 방법

### 1. BASE_URL 설정
**NetworkModule.kt**에서 백엔드 서버 주소를 설정하세요:

```kotlin
// 로컬 개발 (Android 에뮬레이터)
private const val BASE_URL = "http://10.0.2.2:8000/"

// 실제 서버
private const val BASE_URL = "https://your-server.com/"

// 같은 WiFi의 실제 기기
private const val BASE_URL = "http://192.168.x.x:8000/"
```

### 2. ViewModel에서 사용 예시

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    
    // Health Check
    fun checkServerHealth() {
        viewModelScope.launch {
            when (val result = backendRepository.healthCheck()) {
                is ApiResult.Success -> {
                    println("Server is running: ${result.data.message}")
                }
                is ApiResult.Error -> {
                    println("Error: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // Show loading
                }
            }
        }
    }
    
    // 사용자 생성 (카카오 로그인 후)
    fun createUserFromKakao(kakaoUserId: String, nickname: String) {
        viewModelScope.launch {
            when (val result = backendRepository.createUser(
                provider = "kakao",
                providerUserId = kakaoUserId,
                nickname = nickname
            )) {
                is ApiResult.Success -> {
                    val user = result.data
                    println("User created: ${user.id}")
                }
                is ApiResult.Error -> {
                    println("Error: ${result.message}")
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    // 사진 업로드
    fun uploadPhoto(userId: String, file: File) {
        viewModelScope.launch {
            when (val result = backendRepository.uploadPhoto(userId, file)) {
                is ApiResult.Success -> {
                    val photo = result.data
                    println("Photo uploaded: ${photo.fileUrl}")
                }
                is ApiResult.Error -> {
                    println("Error: ${result.message}")
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    // 그룹 생성
    fun createGroup(name: String, creatorId: String) {
        viewModelScope.launch {
            when (val result = backendRepository.createGroup(
                name = name,
                creatorId = creatorId,
                description = "Interest group"
            )) {
                is ApiResult.Success -> {
                    val group = result.data
                    println("Group created: ${group.id}")
                }
                is ApiResult.Error -> {
                    println("Error: ${result.message}")
                }
                is ApiResult.Loading -> {}
            }
        }
    }
}
```

### 3. 기존 LoginViewModel 통합 예시

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val backendRepository: BackendRepository  // 추가!
) : ViewModel() {
    
    fun handleKakaoLoginSuccess(kakaoUser: com.kakao.sdk.user.model.User) {
        viewModelScope.launch {
            // 1. 백엔드에 사용자 생성/조회
            when (val result = backendRepository.createUser(
                provider = "kakao",
                providerUserId = kakaoUser.id.toString(),
                nickname = kakaoUser.kakaoAccount?.profile?.nickname,
                profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl
            )) {
                is ApiResult.Success -> {
                    val backendUser = result.data
                    // 2. 백엔드 userId로 앱 상태 업데이트
                    _loginState.value = LoginState.Success(
                        userId = backendUser.id,
                        nickname = backendUser.nickname ?: ""
                    )
                }
                is ApiResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }
}
```

## 🔍 API 엔드포인트

### User APIs
- `POST /api/users` - 사용자 생성
- `GET /api/users/{userId}` - 사용자 조회
- `PUT /api/users/{userId}` - 사용자 업데이트

### Photo APIs
- `POST /api/photos` - 사진 업로드
- `GET /api/photos/user/{userId}` - 사용자 사진 목록

### Group APIs
- `POST /api/groups` - 그룹 생성
- `GET /api/groups/{groupId}` - 그룹 조회
- `GET /api/groups/user/{userId}` - 사용자 그룹 목록
- `POST /api/groups/{groupId}/members` - 그룹 멤버 추가

## 🐛 디버깅

### 로그 확인
Logcat에서 `OkHttp` 태그로 필터링하면 모든 HTTP 요청/응답을 볼 수 있습니다.

### 일반적인 이슈

**1. 연결 실패 (Connection refused)**
- BASE_URL 확인
- 백엔드 서버가 실행 중인지 확인
- 에뮬레이터: `10.0.2.2` 사용
- 실제 기기: PC IP 주소 사용

**2. CLEARTEXT 에러 (Android 9+)**
AndroidManifest.xml에 추가:
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

**3. 타임아웃**
- NetworkModule에서 타임아웃 시간 조정
- 백엔드 응답 시간 확인

## 📝 다음 단계

1. **백엔드 API 완성**: FastAPI에 실제 엔드포인트 구현
2. **테스트**: Postman으로 API 테스트
3. **통합**: 기존 ViewModel들에 BackendRepository 주입
4. **에러 처리**: 네트워크 에러, 서버 에러 UI 처리
5. **로딩 상태**: API 호출 중 로딩 인디케이터 표시

## 🎯 체크리스트

- [ ] BASE_URL 설정
- [ ] 백엔드 서버 실행
- [ ] Health Check API 테스트
- [ ] 카카오 로그인 후 사용자 생성 테스트
- [ ] 사진 업로드 테스트
- [ ] 그룹 생성/조회 테스트
- [ ] 에러 처리 구현
- [ ] 로딩 상태 UI 구현
