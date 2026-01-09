# 🧪 Backend-Frontend 연결 테스트 가이드

## 📝 준비 사항 체크리스트

- [ ] Python 3.11+ 설치됨
- [ ] Android Studio 설치됨
- [ ] Android 에뮬레이터 또는 실제 기기

## 🚀 테스트 시작하기

### Step 1: 백엔드 서버 실행

```powershell
# 1. Backend 폴더로 이동
cd Backend_FastAPI

# 2. 가상환경 생성 (처음 한 번만)
python -m venv venv

# 3. 가상환경 활성화
.\venv\Scripts\activate

# 4. 의존성 설치 (처음 한 번만)
pip install -r requirements.txt

# 5. 서버 실행
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**서버 실행 성공 확인:**
```
INFO:     Uvicorn running on http://0.0.0.0:8000
INFO:     Application startup complete.
```

브라우저에서 확인: http://localhost:8000
응답: `{"message": "Hello FastAPI"}`

### Step 2: Android 앱 실행

```bash
# 1. Frontend 폴더로 이동
cd frontend

# 2. Gradle 동기화
./gradlew sync

# 3. 앱 빌드 및 실행
./gradlew installDebug
```

**또는 Android Studio에서:**
1. `frontend` 폴더를 Android Studio로 열기
2. Sync Now 클릭 (Gradle 동기화)
3. ▶️ Run 버튼 클릭

### Step 3: 테스트 실행

앱이 실행되면 **API Test 화면**이 자동으로 나타납니다!

#### 테스트 순서:
1. **Health Check** 버튼 클릭
   - ✅ 성공: "Success: Hello FastAPI" 또는 "healthy" 메시지
   - ❌ 실패: 연결 오류 메시지 확인

2. **Create User** 버튼 클릭
   - ✅ 성공: User ID, Nickname 등 정보 표시
   - ❌ 실패: 에러 메시지 확인

3. **Get User** 버튼 클릭
   - ✅ 성공: 사용자 정보 조회됨
   - ❌ 실패: 에러 메시지 확인

## 🔍 문제 해결

### 1. "Failed to connect" / "Connection refused"

**원인:** 백엔드 서버가 실행 중이 아니거나 주소가 잘못됨

**해결:**
- 백엔드 서버가 실행 중인지 확인
- 서버 로그에서 에러 확인
- 방화벽 설정 확인

**에뮬레이터 주소 확인:**
- ✅ 에뮬레이터: `http://10.0.2.2:8000`
- ❌ localhost: `http://localhost:8000` (작동 안 함!)

**실제 기기 주소 확인:**
```powershell
# PC IP 주소 확인
ipconfig

# WiFi 어댑터의 IPv4 주소 찾기
# 예: 192.168.0.10
```
[NetworkModule.kt](frontend/app/src/main/java/com/example/madclass01/di/NetworkModule.kt)에서:
```kotlin
private const val BASE_URL = "http://192.168.0.10:8000/"
```

### 2. CLEARTEXT 에러

**에러 메시지:** "Cleartext HTTP traffic not permitted"

**해결:** 이미 AndroidManifest.xml에 `usesCleartextTraffic="true"` 추가됨!

### 3. 백엔드 서버 실행 실패

```powershell
# Python 버전 확인
python --version  # 3.11+ 필요

# 가상환경이 활성화되었는지 확인
# 프롬프트에 (venv)가 표시되어야 함

# 의존성 재설치
pip install --upgrade -r requirements.txt
```

### 4. 앱 빌드 실패

**Gradle 동기화 에러:**
```bash
# Gradle wrapper 권한 부여 (Linux/Mac)
chmod +x gradlew

# Gradle 캐시 삭제
./gradlew clean
```

**Android Studio에서:**
- File → Invalidate Caches → Invalidate and Restart

## 📊 로그 확인

### Backend 로그
터미널에서 실시간으로 모든 요청/응답 확인:
```
INFO:     127.0.0.1:xxxxx - "GET /health HTTP/1.1" 200 OK
INFO:     127.0.0.1:xxxxx - "POST /api/users/test HTTP/1.1" 200 OK
```

### Android 로그
Android Studio Logcat에서 `OkHttp` 태그로 필터:
```
D/OkHttp: --> GET http://10.0.2.2:8000/health
D/OkHttp: <-- 200 OK http://10.0.2.2:8000/health (123ms)
D/OkHttp: {"status":"healthy","service":"InterestMap Backend"}
```

## ✅ 테스트 완료 확인

모든 버튼이 ✅ 성공 메시지를 표시하면 연결 성공!

```
✅ Health Check Success
✅ Create User Success
✅ Get User Success
```

## 🎯 다음 단계

테스트가 성공하면:

1. **테스트 모드 끄기**
   [MainActivity.kt](frontend/app/src/main/java/com/example/madclass01/MainActivity.kt):
   ```kotlin
   val isTestMode = false  // true → false로 변경
   ```

2. **실제 API 구현**
   - Backend에 실제 데이터베이스 연결
   - User, Photo, Group API 완성

3. **프로덕션 준비**
   - BASE_URL을 실제 서버 주소로 변경
   - 에러 처리 강화
   - 로딩 상태 UI 추가

## 🆘 도움이 필요하면

1. 백엔드 로그 확인
2. Android Logcat 확인
3. 네트워크 설정 확인
4. 방화벽 설정 확인

**테스트 성공을 기원합니다! 🎉**
