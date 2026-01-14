# <img src="frontend/app/src/main/res/drawable/omo.png" align="center" width="50" height="50"> 오늘모임 (OmO)

> **취향으로 연결되는 우리 반 소모임** - AI가 분석한 관심사로 가장 잘 어울리는 멤버들을 모아드려요.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.110+-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

---


## 📖 프로젝트 소개

> **"낯선 사람들이 모인 40명의 반, 어떻게 하면 어색함을 깨고 빨리 친해질 수 있을까?"**

**오늘모임(OmO)** 은 사용자가 업로드한 사진과 소개를 AI로 분석하여 관심사를 추출하고, **대규모 모임 내에서 가장 잘 맞는 소그룹을 자동으로 추천**해주는 서비스입니다.

### 🎯 기획 의도

처음 만나는 많은 사람들 속에서 누구와 이야기를 시작해야 할지 막막했던 경험이 있나요? **오늘모임**은 단순한 그룹 생성을 넘어, **대규모 그룹 내 인원들을 관심사 유사도에 따라 N개의 소그룹으로 균등하게(Uniform) 나누어주는 기능**을 제공합니다. 이를 통해 사용자는 성향이 맞는 사람들과 자연스럽게 대화를 시작하고 빠르게 친해질 수 있습니다.

### ✨ 주요 기능

| 기능 | 설명 |
|------|------|
|**소그룹 자동 클러스터링** | 대형 그룹 멤버들을 관심사 기반으로 N개의 소그룹으로 자동 분배 & 매칭 |
|**카카오 로그인** | 카카오 계정으로 간편하게 로그인 |
|**사진 업로드 & AI 분석** | 업로드된 사진을 BLIP 모델로 캡셔닝하고 관심사 태그 추출 |
|**임베딩 기반 매칭** | OpenAI 임베딩을 활용한 유사도 분석 및 추천 알고리즘 |
|**그룹 생성 & 참여** | QR 코드 또는 검색을 통한 간편한 그룹 참여 |
|**그룹 채팅** | 실시간 그룹 채팅 (텍스트 + 이미지) |
|**Interest Map 시각화** | 그룹 멤버들의 관심사 유사도를 2D 맵으로 시각화하여 한눈에 파악 |

---

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Android App (Frontend)                    │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐     │
│  │  Login    │  │  Profile  │  │  Groups   │  │   Chat    │     │
│  │  Screen   │  │  Screen   │  │  Screen   │  │  Screen   │     │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘     │
│        └──────────────┴───────────────┴──────────────┘          │
│                              │                                   │
│                    ┌─────────▼─────────┐                        │
│                    │    ViewModel      │  (MVVM Architecture)   │
│                    │    + Repository   │                        │
│                    └─────────┬─────────┘                        │
│                              │ Retrofit2 + OkHttp                │
└──────────────────────────────┼───────────────────────────────────┘
                               │ HTTP/JSON
┌──────────────────────────────▼───────────────────────────────────┐
│                     FastAPI Backend (Python)                     │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐     │
│  │  /auth/*  │  │   /me/*   │  │ /groups/* │  │  /api/*   │     │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘     │
│        └──────────────┴───────────────┴──────────────┘          │
│                              │                                   │
│         ┌────────────────────┼────────────────────┐             │
│         │                    │                    │             │
│  ┌──────▼──────┐  ┌──────────▼──────────┐  ┌─────▼─────┐       │
│  │ PostgreSQL  │  │   AI Services        │  │  Storage  │       │
│  │  (SQLAlchemy)│  │ (BLIP + OpenAI)     │  │ (uploads/)│       │
│  └─────────────┘  └─────────────────────┘  └───────────┘       │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 기술 스택

### Frontend (Android)

| 구분 | 기술 |
|------|------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (Dagger) |
| **Networking** | Retrofit2 + OkHttp3 + Gson |
| **Image Loading** | Coil |
| **Navigation** | Navigation Compose |
| **Auth** | Kakao SDK |
| **QR Code** | ZXing |

### Backend (Server)

| 구분 | 기술 |
|------|------|
| **Framework** | FastAPI (Python 3.11+) |
| **ASGI Server** | Uvicorn |
| **Database** | PostgreSQL |
| **ORM** | SQLAlchemy 2.0 (Async) |
| **Migration** | Alembic |
| **AI/ML** | BLIP (Image Captioning), OpenAI Embeddings |
| **Auth** | JWT (python-jose) |

---



---

## 🚀 실행 방법

### 1. 환경 변수 설정 (.env)

백엔드 서버 실행을 위해 `Backend_FastAPI/.env` 파일을 생성하고 다음 변수들을 설정해야 합니다.

```ini
# --- Database (PostgreSQL) ---
POSTGRES_DB=interestmap
POSTGRES_USER=myuser
POSTGRES_PASSWORD=mypassword
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
# 또는 아래와 같이 전체 URL로 설정 가능
# DATABASE_URL=postgresql+asyncpg://user:password@localhost:5432/interestmap

# --- Kakao Login ---
KAKAO_REST_API_KEY=your_kakao_rest_api_key
KAKAO_REDIRECT_URI=http://localhost:8000/auth/kakao/callback

# --- JWT Auth ---
JWT_SECRET=your_super_secret_key_for_jwt_signing
JWT_ALG=HS256
JWT_EXPIRE_MINUTES=43200  # 30일

# --- OpenAI (Embedding & Translation) ---
OPENAI_API_KEY=sk-your_openai_api_key
OPENAI_TRANSLATION_MODEL=gpt-4o-mini
OPENAI_EMBED_MODEL=text-embedding-3-small

# --- Frontend Redirect (Optional) ---
FRONTEND_REDIRECT_URL=http://localhost:5173/auth/callback
```

### 2. DB Setup (PostgreSQL)

1. **PostgreSQL DB 생성**
   ```sql
   CREATE DATABASE interestmap;
   ```

2. **Alembic 마이그레이션 적용**
   ```bash
   # 가상환경 활성화 상태에서 실행
   cd Backend_FastAPI
   
   # 초기 테이블 생성 (또는 변경사항 적용)
   alembic upgrade head
   ```

### 3. Backend Server 실행

```bash
cd Backend_FastAPI

# 가상환경 생성 및 활성화
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 서버 실행
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 4. Frontend App 실행

1. Android Studio에서 `frontend/` 폴더 열기
2. `local.properties`에 SDK 경로 설정
3. 에뮬레이터 또는 실제 디바이스에서 실행

> **Note:** 백엔드 서버 주소는 `data/remote/` 폴더 내 설정에서 변경 가능

---

## 📡 API 문서

서버 실행 후 아래 URL에서 자동 생성된 API 문서를 확인할 수 있습니다:

- **Swagger UI:** `http://localhost:8000/docs`
- **ReDoc:** `http://localhost:8000/redoc`

### 주요 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/auth/kakao` | 카카오 로그인 |
| `GET` | `/me` | 내 정보 조회 |
| `PATCH` | `/me` | 내 정보 수정 |
| `GET` | `/groups` | 그룹 목록 조회 |
| `POST` | `/groups` | 그룹 생성 |
| `GET` | `/groups/{id}/interest-map` | Interest Map 데이터 |
| `POST` | `/api/photos/batch` | 다중 사진 업로드 & AI 분석 |
| `POST` | `/api/generate-embedding` | 사용자 임베딩 생성 |


---
<p align="center">
  <b>at MadCamp 2025 Week 1</b>
</p>
