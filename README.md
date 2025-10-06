# 🛒 Member-Order-Payment System

**Spring Boot 기반 전자상거래 백엔드 시스템**

Clean Architecture와 Domain-Driven Design을 적용한 확장 가능하고 유지보수가 용이한 전자상거래 시스템입니다.

## 🎯 프로젝트 소개

### 핵심 기능
- **회원 관리**: 회원 등록, 조회, 상태 관리
- **주문 관리**: 주문 생성, 상태 변경, 조회  
- **결제 관리**: 결제 생성, 처리, 환불, 조회
- **REST API**: 완전한 RESTful API 구현

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL (Production) + H2 (Development/Testing)
- **ORM**: Spring Data JPA, Hibernate
- **Testing**: JUnit 5, Spring Boot Test, Mockito
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker, Docker Compose

## 🏗️ 아키텍처 구조

```
src/main/java/com/example/
├── domain/                    # 도메인 계층
│   ├── event/                # 도메인 이벤트
│   ├── service/              # 도메인 서비스
│   ├── specification/        # 비즈니스 규칙 명세
│   └── valueobject/          # 값 객체
├── application/              # 애플리케이션 계층
│   ├── command/              # 명령 처리
│   ├── query/                # 쿼리 처리
│   ├── service/              # 애플리케이션 서비스
│   ├── usecase/              # 유스케이스
│   └── exception/            # 예외 처리
├── infrastructure/           # 인프라 계층
│   ├── persistence/          # 데이터 영속성
│   ├── web/                  # 웹 컨트롤러
│   ├── event/                # 이벤트 발행
│   └── validation/           # 입력 검증
└── dto/                      # 데이터 전송 객체
```

## 실행 방법

### 1. PostgreSQL 사용 (권장)
```bash
docker-compose up -d
./gradlew bootRun
```

### 2. H2 데이터베이스 사용
```bash
# H2 Embedded (인메모리)
./gradlew bootRun --args='--spring.profiles.active=h2-embedded'

# H2 Generic (파일 기반)
./gradlew bootRun --args='--spring.profiles.active=h2-generic'

# H2 Server (서버 모드)
./start-h2-server.sh
./gradlew bootRun --args='--spring.profiles.active=h2-server'
```

## 접근 정보

- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`
- **H2 Console**: `http://localhost:8080/h2-console`

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# E2E 테스트 실행
./e2e-test.sh

# H2 데이터베이스 테스트
./check-h2.sh
```

## 주요 API

### 회원 관리
- `POST /api/members` - 회원 생성
- `GET /api/members` - 전체 회원 조회
- `GET /api/members/{id}` - 회원 상세 조회

### 주문 관리
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 주문 목록 조회
- `POST /api/orders/{id}/confirm` - 주문 확인
- `POST /api/orders/{id}/complete` - 주문 완료
- `POST /api/orders/{id}/cancel` - 주문 취소

### 결제 관리
- `POST /api/payments` - 결제 생성
- `GET /api/payments` - 결제 목록 조회
- `POST /api/payments/{id}/process` - 결제 처리
- `POST /api/payments/{id}/refund` - 결제 환불


## 상세 문서

- [API 문서](docs/api/API_DOCUMENTATION.md)
- [아키텍처 분석](docs/technical/ARCHITECTURE_ANALYSIS_AND_IMPROVEMENTS.md)
- [H2 데이터베이스 가이드](docs/technical/H2_DATABASE_GUIDE.md)
- [면접 가이드](docs/guides/면접_가이드.md)

## 프로젝트 설정

### 환경 변수
- `SPRING_PROFILES_ACTIVE`: 활성 프로필 (default, h2-embedded, h2-generic, h2-server)
- `DB_HOST`: 데이터베이스 호스트 (기본값: localhost)
- `DB_PORT`: 데이터베이스 포트 (기본값: 5432)

### H2 데이터베이스 모드
- **Embedded**: 인메모리, 개발/테스트용
- **Generic**: 파일 기반, 데이터 지속성
- **Server**: 서버 모드, 공유 환경

## 🎉 프로젝트 하이라이트

- ✅ **완전한 테스트 커버리지**: 단위/통합/E2E 테스트
- ✅ **다중 데이터베이스 지원**: PostgreSQL + H2 (3모드)
- ✅ **완전한 문서화**: API 문서, 아키텍처 가이드
- ✅ **자동화 스크립트**: 테스트/데모 스크립트 제공
- ✅ **프로덕션 준비**: Docker, 모니터링, 로깅

---

**GitHub**: [https://github.com/sw6820/aipia](https://github.com/sw6820/aipia)
