# SayToReserve

자연어 기반 AI 장소 추천 & 실시간 예약 시스템


---

## 프로젝트 개요

- 프로젝트명: SayToReserve
- 개발 기간: 2025년 6월 10일 ~
- 플랫폼: 웹

---

## 목표

메인 페이지 입력창에 사용자의 자연어 질문을 입력하면,
AI가 문장을 분석하여 병원, 미용실, 식당 등 적절한 장소를 추천하고
예약 가능한 시간대를 확인 후 즉시 예약 가능한 웹 기반 예약 시스템을 제공합니다.

> 예시:
> "내일 저녁에 고기 먹으러 가고 싶어" →
> 위치 기반 맛집 추천 + 실시간 예약 가능 시간 안내

### 주요 기능

| 기능 | 설명 |
|------|------|
| 자연어 검색 | 사용자가 일상 언어로 원하는 장소/시간을 입력 |
| AI 장소 추천 | 입력된 문장을 분석하여 적합한 장소 추천 |
| 실시간 예약 | 추천된 장소의 예약 가능 시간 확인 및 즉시 예약 |
| 위치 기반 서비스 | 사용자 위치 기반 주변 장소 탐색 |
| 소셜 로그인 | 카카오, 구글 OAuth 2.0 로그인 지원 |
| 회원 관리 | 일반 회원가입/로그인, 예약 내역 관리 |

### 지원 예약 카테고리

- 식당/카페
- 병원/의원
- 미용실/네일샵
- 기타 예약 가능 업종 (확장 예정)

---

## 기술 스택

### Backend

| 기술 | 버전 | 설명 |
|------|------|------|
| Java | 17 | 메인 언어 |
| Spring Boot | 3.5.0 | 백엔드 프레임워크 |
| Spring Security | 6.x | 인증/인가 |
| Spring Data JPA | 3.5.0 | ORM |
| Spring Data Redis | 3.5.0 | Redis 연동 |
| JWT (jjwt) | 0.11.5 | 토큰 기반 인증 |
| Lombok | - | 보일러플레이트 코드 감소 |
| MySQL | 8.0 | 관계형 데이터베이스 |
| Redis | 7-alpine | 토큰 저장소 (Refresh Token, Blacklist) |

### Frontend

| 기술 | 버전 | 설명 |
|------|------|------|
| React | 19.1.0 | UI 라이브러리 |
| React Router DOM | 7.6.2 | 라우팅 |
| Axios | 1.9.0 | HTTP 클라이언트 |
| Vite | 6.3.5 | 빌드 도구 |
| Tailwind CSS | 3.4.17 | CSS 프레임워크 |
| ESLint | 9.25.0 | 코드 린터 |
| Prettier | 3.5.3 | 코드 포맷터 |

### Infrastructure

| 기술 | 버전 | 설명 |
|------|------|------|
| Docker | - | 컨테이너화 |
| Docker Compose | 3.8 | 멀티 컨테이너 오케스트레이션 |

### CI/CD

| 기술 | 버전 | 설명 |
|------|------|------|
| SpotBugs | 5.1.3 | 정적 코드 분석 |

---

## 인증 시스템

| 방식 | 설명 |
|------|------|
| JWT | Access Token (15분) / Refresh Token (7일) |
| OAuth 2.0 | 카카오, 구글 소셜 로그인 |
| Redis | Refresh Token 저장, Access Token 블랙리스트 관리 |

---

## 프로젝트 구조

```
SayToReserve/
├── backend/
│   └── src/main/java/org/example/saytoreverse/
│       ├── config/           # 설정 (Security, Redis, JWT)
│       ├── controller/       # API 컨트롤러
│       ├── domain/           # 엔티티
│       ├── dto/              # 데이터 전송 객체
│       ├── repository/       # JPA 레포지토리
│       └── service/          # 비즈니스 로직
├── frontend/
│   └── src/
│       ├── components/       # React 컴포넌트
│       ├── pages/            # 페이지
│       └── api/              # API 호출
└── docker-compose.yml
```

---

## 실행 방법

### 사전 요구사항
- Java 17
- Node.js
- Docker & Docker Desktop

### Docker로 실행
```bash
# 전체 서비스 실행
docker-compose up -d

# 개별 서비스 실행
docker-compose up -d redis    # Redis만
docker-compose up -d db       # MySQL만
```

### 로컬 개발 환경
```bash
# Backend (IntelliJ에서 실행 또는)
cd backend
./gradlew bootRun

# Frontend
cd frontend
npm install
npm run dev
```

### 포트 정보
| 서비스 | 포트 |
|--------|------|
| Frontend | 5173 |
| Backend | 8080 |
| MySQL | 3307 (외부) → 3306 (내부) |
| Redis | 6379 |

---

## 프로젝트 규칙

### Branch Strategy
> - main / dev / feature 브랜치 기본 생성

### Git Convention
> 1. 적절한 커밋 접두사 작성
> 2. 커밋 메시지 내용 작성
> 3. 내용 뒤에 이슈 (#이슈 번호)와 같이 작성하여 이슈 연결

### Pull Request
> ### Title
> * 제목은 '[Feat] 홈 페이지 구현'과 같이 작성합니다.

> ### PR Type
  > - [ ] FEAT: 새로운 기능 구현
  > - [ ] FIX: 버그 수정
  > - [ ] DOCS: 문서 수정
  > - [ ] STYLE: 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
  > - [ ] REFACTOR: 코드 리펙토링
  > - [ ] CHORE: 빌드 업무 수정, 패키지 매니저 수정

### Code Convention
> BE
> - 패키지명 전체 소문자
> - 클래스명, CamelCase
> - 클래스 이름 명사 사용

> FE
> - 클래스명, CamelCase
> - Event handler 사용 (ex. handle ~)
> - export방식 (ex. export default ~)
> - 화살표 함수 사용

### Communication Rules
> - Notion 활용

---

## 문서

| 문서 | 설명 |
|------|------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 시스템 아키텍처 및 개발 계획 |
