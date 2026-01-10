# 🎟️ QR 초대 화면 (QR Invite Screen) - 구현 가이드

## 📋 개요
디자인 스펙에 맞춘 "QR 초대" 화면을 **MVVM + Clean Architecture** 기반으로 구현했습니다.  
초대 링크 생성, QR 코드 생성, 공유 기능을 포함하고 있습니다.

---

## 🏗️ 아키텍처 구조

### 1. **Presentation Layer** (UI)
```
presentation/group/
├── screen/
│   └── QRInviteScreen.kt              # 메인 UI 컴포넌트
├── viewmodel/
│   └── QRInviteViewModel.kt           # UI 상태 관리
└── component/
    ├── GroupInfoCard.kt               # 그룹 정보 카드
    ├── QRCodeContainer.kt             # QR 코드 표시
    └── ShareButtonsComponent.kt       # 공유 버튼들
```

### 2. **Domain Layer** (비즈니스 로직)
```
domain/
├── usecase/
│   ├── GenerateInviteLinkUseCase.kt   # 초대 링크 생성
│   └── JoinGroupByInviteLinkUseCase.kt # 링크로 그룹 가입
├── repository/
│   └── InviteRepository.kt            # Repository 인터페이스
└── model/
    └── InviteLink.kt                  # 초대 링크 모델
```

### 3. **Data Layer** (데이터 접근)
```
data/
├── repository/
│   └── InviteRepositoryImpl.kt         # Repository 구현체
├── remote/
│   ├── ApiService.kt                  # (수정) API 인터페이스
│   └── dto/
│       └── InviteDto.kt               # DTO 정의
```

### 4. **Utilities**
```
utils/
└── QRCodeGenerator.kt                  # QR 코드 생성 유틸리티
```

---

## 📱 UI 구성

### QRInviteScreen 레이아웃
```
┌─────────────────────────────┐
│ ←  그룹 초대         ↗       │  <- Header
├─────────────────────────────┤
│                             │
│  ┌───────────────────────┐  │
│  │   그룹 정보 카드      │  │  <- 그룹명, 멤버수
│  │  그룹명, 멤버 수     │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │   QR 코드 생성        │  │
│  │  ┌─────────────────┐  │  │
│  │  │                 │  │  │
│  │  │   [QR CODE]     │  │  │  <- ZXing 라이브러리로 생성
│  │  │                 │  │  │
│  │  └─────────────────┘  │  │
│  │  설명 텍스트           │  │
│  │  ⏱ 24시간 후 만료    │  │
│  └───────────────────────┘  │
│                             │
│  ┌─────────────────────────┐│
│  │ 🔗 초대 링크 복사      ││
│  └─────────────────────────┘│
│                             │
│  [💬 카카오] [📸 인스타] [⋯더보기]
│                             │
│  ✓ 초대 링크 복사됨 (Toast)│
└─────────────────────────────┘
```

---

## 🔄 상태 관리 (ViewModel)

### QRInviteUiState
```kotlin
data class QRInviteUiState(
    val group: Group? = null,                    // 현재 그룹
    val inviteLink: InviteLink? = null,          // 생성된 초대 링크
    val qrCodeBitmap: String? = null,            // QR 코드 데이터
    val expiryTime: String = "24시간 후 만료",  // 만료 시간
    val isLoading: Boolean = false,              // 로딩 중
    val errorMessage: String = "",               // 오류 메시지
    val copySuccess: Boolean = false,            // 복사 성공
    val joinSuccess: Boolean = false,            // 가입 성공
    val joinGroupId: String? = null              // 가입한 그룹 ID
)
```

### ViewModel 메서드
```kotlin
fun initializeWithGroup(group: Group)                    // 그룹 정보 초기화
fun generateInviteLink(groupId: String, userId: String) // 초대 링크 생성
fun copyInviteLink(): Boolean                            // 링크 복사
fun shareToKakao(inviteLink: String)                     // 카카오톡 공유
fun shareToInstagram(inviteLink: String)                 // 인스타그램 공유
fun shareMore(inviteLink: String)                        // 기본 공유
fun joinGroupByLink(inviteUrl: String, userId: String)  // 링크로 가입
fun resetCopySuccess()                                   // 복사 상태 초기화
fun resetJoinSuccess()                                   // 가입 상태 초기화
```

---

## 🎯 주요 기능

### 1. 초대 링크 생성
- 백엔드 API 호출: `POST /api/invites/generate`
- 고유한 초대 URL 생성
- 24시간 유효 기간 설정

### 2. QR 코드 생성
- **라이브러리**: `com.google.zxing:core:3.5.2`
- 초대 링크를 QR 코드로 변환
- Bitmap으로 화면에 표시
- 512x512px 해상도

### 3. 링크 복사
- 클립보드에 초대 URL 복사
- Toast 메시지로 복사 확인
- 2초 후 자동 사라짐

### 4. 소셜 공유
- **카카오톡**: `Intent.ACTION_SEND`
- **인스타그램**: Direct Message로 전달
- **더보기**: Android 기본 공유 메뉴

### 5. 그룹 가입
- 초대 링크로 그룹 자동 가입
- 유효성 검사 (만료 여부)
- 중복 가입 방지

---

## 📡 백엔드 연동

### API 엔드포인트

#### 1. 초대 링크 생성
```
POST /api/invites/generate

Request:
{
    "groupId": "group123",
    "createdByUserId": "user123"
}

Response:
{
    "id": "invite123",
    "groupId": "group123",
    "inviteUrl": "https://madclass.com/invite/abc123def456",
    "qrCodeData": "...",  // QR 코드 데이터 (선택)
    "expiresAt": "2024-01-11T12:00:00Z",
    "createdAt": "2024-01-10T12:00:00Z",
    "maxUses": 100,
    "currentUses": 0
}
```

#### 2. 초대 링크 조회
```
GET /api/invites/group/{groupId}

Response:
{
    "id": "invite123",
    "groupId": "group123",
    ...
}
```

#### 3. 초대 링크로 그룹 가입
```
POST /api/invites/join

Request:
{
    "inviteUrl": "https://madclass.com/invite/abc123def456",
    "userId": "newuser123"
}

Response:
{
    "success": true,
    "groupId": "group123",
    "message": "성공적으로 그룹에 가입했습니다"
}
```

---

## 🔧 의존성 추가

### build.gradle.kts
```gradle
// QR Code Generation
implementation("com.google.zxing:core:3.5.2")
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
```

---

## 🚀 사용 방법

### 1. Navigation에 등록
```kotlin
composable("qrInvite/{groupId}") { backStackEntry ->
    val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
    val group = getCurrentGroup(groupId)  // 그룹 정보 조회
    
    QRInviteScreen(
        group = group,
        userId = currentUserId,
        onBackPress = {
            navController.popBackStack()
        },
        onJoinSuccess = {
            navController.navigate("groupDetail/$groupId")
        }
    )
}
```

### 2. 그룹 상세에서 호출
```kotlin
Button(onClick = {
    navController.navigate("qrInvite/${group.id}")
}) {
    Text("그룹 초대하기")
}
```

### 3. 초대 링크로 가입 (Deep Link)
```kotlin
// AndroidManifest.xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="madclass.com"
        android:pathPrefix="/invite/" />
</intent-filter>

// QRInviteViewModel에서 처리
viewModel.joinGroupByLink(
    inviteUrl = "https://madclass.com/invite/abc123def456",
    userId = currentUserId
)
```

---

## 🎨 색상 팔레트

| 용도 | 색상코드 | 예시 |
|------|---------|------|
| Primary | #667EEA | 버튼, 강조 |
| Success (Group Icon) | #10B981 | 그룹 아이콘 배경 |
| Background | #F9FAFB | 입력 필드, 카드 |
| Text Primary | #111827 | 제목, 라벨 |
| Text Secondary | #6B7280 | 설명, 서브텍스트 |
| Border | #E5E7EB | 기본 보더 |
| Expiry Badge | #FEF2F2 | 만료 알림 배경 |
| Warning Text | #991B1B | 만료 텍스트 |

---

## 📦 파일 구조 요약

```
✅ Presentation Layer (4개)
  - QRInviteScreen.kt
  - QRInviteViewModel.kt
  - GroupInfoCard.kt
  - QRCodeContainer.kt
  - ShareButtonsComponent.kt

✅ Domain Layer (3개)
  - GenerateInviteLinkUseCase.kt
  - JoinGroupByInviteLinkUseCase.kt
  - InviteRepository.kt
  - InviteLink.kt

✅ Data Layer (2개)
  - InviteRepositoryImpl.kt
  - InviteDto.kt

✅ Utilities (1개)
  - QRCodeGenerator.kt

✅ DI Configuration (1개)
  - RepositoryModule.kt (수정)
```

---

## ⚙️ QR 코드 생성 상세

### QRCodeGenerator 활용
```kotlin
val bitmap = QRCodeGenerator.generateQRCode(
    text = "https://madclass.com/invite/abc123def456",
    width = 512,  // 512x512px
    height = 512
)
```

### 특징
- ZXing 라이브러리 기반
- Error Correction Level: Q (25% recovery)
- 최적화된 크기: 512x512px
- Black & White 컬러

---

## 🔐 보안 고려사항

1. **초대 링크 암호화**: 랜덤한 토큰 기반
2. **만료 시간**: 24시간 제한
3. **사용 횟수 제한**: maxUses 설정
4. **검증**: 백엔드에서 유효성 확인
5. **로그 기록**: 가입 사용자 추적

---

## 📊 테스트 체크리스트

- [ ] 초대 링크 생성 테스트
- [ ] QR 코드 생성 및 표시 테스트
- [ ] 링크 복사 기능 테스트
- [ ] 카카오톡 공유 테스트
- [ ] 인스타그램 공유 테스트
- [ ] 기본 공유 테스트
- [ ] 링크로 그룹 가입 테스트
- [ ] 만료된 링크 테스트
- [ ] 중복 가입 방지 테스트
- [ ] 에러 메시지 표시 테스트

---

## 🔄 향후 개선사항

1. **QR 코드 고급 옵션**
   - 로고 삽입
   - 컬러 QR 코드
   - 디자인 패턴 추가

2. **공유 기능 확장**
   - Facebook 공유
   - Twitter 공유
   - WhatsApp 공유

3. **초대 관리**
   - 초대 링크 비활성화
   - 초대 통계 (사용 횟수, 가입자 조회)
   - 초대 링크 재생성

4. **사용자 경험**
   - 복사 후 자동 공유 제안
   - QR 코드 다운로드
   - 초대 히스토리 조회

---

## 📚 참고 자료

- [ZXing QR Code Library](https://github.com/zxing/zxing)
- [Android Intent Documentation](https://developer.android.com/guide/components/intents)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/)

---

**상태**: ✅ 구현 완료 - 백엔드 API 연동 준비 완료  
**라이브러리**: ZXing (QR 코드 생성)  
**아키텍처**: MVVM + Clean Architecture
