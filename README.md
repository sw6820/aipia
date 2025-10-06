# 🏢 Member-Order-Payment System

> **Spring Boot 기반 이커머스 백엔드 시스템**  
> 클린 아키텍처, 도메인 주도 설계, 이벤트 기반 아키텍처를 적용한 프로덕션 준비 시스템

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [빠른 시작](#-빠른-시작)
- [API 문서](#-api-문서)
- [주요 기능](#-주요-기능)
- [프로덕션 배포](#-프로덕션-배포)
- [테스트](#-테스트)
- [모니터링](#-모니터링)
- [개발자 가이드](#-개발자-가이드)
- [문서 구조](#-문서-구조)

## 🎯 프로젝트 개요

Member-Order-Payment System은 현대적인 Spring Boot 기술을 활용하여 구축된 종합적인 이커머스 백엔드 시스템입니다. 이 프로젝트는 **클린 아키텍처**, **도메인 주도 설계(DDD)**, **이벤트 기반 아키텍처**를 적용하여 엔터프라이즈급 애플리케이션의 모범 사례를 보여줍니다.

### 핵심 도메인
- **회원 관리**: 회원 등록, 조회, 상태 관리
- **주문 관리**: 주문 생성, 확인, 취소, 완료
- **결제 관리**: 결제 처리, 환불, 상태 추적

### 주요 특징
- ✅ **클린 아키텍처**: 명확한 계층 분리와 의존성 역전
- ✅ **도메인 주도 설계**: 값 객체와 도메인 이벤트 활용
- ✅ **CQRS 패턴**: 명령과 쿼리 분리
- ✅ **이벤트 기반 아키텍처**: 느슨한 결합과 확장성
- ✅ **프로덕션 준비**: PostgreSQL 데이터베이스
- ✅ **모니터링**: Spring Boot Actuator 통합
- ✅ **컨테이너화**: Docker 및 Docker Compose 지원
- ✅ **API 문서화**: OpenAPI/Swagger 자동 생성
- ✅ **종합적 테스트**: 단위/통합 테스트 커버리지

## 🛠 기술 스택

### Backend
- **Java 17** - 최신 LTS 버전
- **Spring Boot 3.2.0** - 메인 프레임워크
- **Spring Data JPA** - 데이터 접근 계층
- **Spring Cache** - 성능 최적화
- **Spring Boot Actuator** - 모니터링 및 헬스 체크

### Database
- **PostgreSQL** - 프로덕션 준비 관계형 데이터베이스
- **H2 Database** - 개발 및 테스트용 인메모리/파일 데이터베이스
- **JPA/Hibernate** - ORM 매핑

### Testing
- **JUnit 5** - 단위 테스트
- **Spring Boot Test** - 통합 테스트
- **TestContainers** - 컨테이너 기반 테스트

### Documentation & Monitoring
- **OpenAPI/Swagger** - API 문서화
- **Spring Boot Actuator** - 애플리케이션 모니터링
- **Micrometer** - 메트릭 수집

### DevOps
- **Docker** - 컨테이너화
- **Docker Compose** - 로컬 개발 환경
- **Gradle** - 빌드 도구
- **Nginx** - 리버스 프록시

## 🏗 아키텍처

### 클린 아키텍처 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ MemberController│  │ OrderController │  │PaymentCtrl  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ MemberService    │  │ OrderService     │  │PaymentSvc   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ MemberRepository │  │ OrderRepository │  │PaymentRepo  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ Member           │  │ Order           │  │ Payment     │ │
│  │ Email            │  │ OrderItem       │  │             │ │
│  │ PhoneNumber      │  │                 │  │             │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 도메인 모델

#### 값 객체 (Value Objects)
```java
// 불변 값 객체 예시
public class Email {
    private final String value;
    
    public Email(String email) {
        validate(email);
        this.value = email.toLowerCase();
    }
}

public class PhoneNumber {
    private final String phoneNumber;
    private final String countryCode;
    
    // 한국 및 국제 전화번호 형식 검증
}
```

#### 도메인 이벤트
```java
// 이벤트 기반 아키텍처
public class MemberCreated implements DomainEvent {
    private final Long memberId;
    private final String email;
    private final LocalDateTime occurredOn;
}
```

## 🚀 빠른 시작

### 1. 사전 요구사항
- Java 17 이상
- Docker & Docker Compose (선택사항)
- Gradle 7.0 이상

### 2. 프로젝트 클론
```bash
git clone <repository-url>
cd member-order-payment-system
```

### 3. 애플리케이션 실행

#### 방법 1: H2 데이터베이스로 실행 (권장)
```bash
# 의존성 설치
./gradlew build

# H2 Embedded 모드로 실행 (인메모리)
./gradlew bootRun --args='--spring.profiles.active=h2-embedded'

# H2 Generic 모드로 실행 (파일 기반)
./gradlew bootRun --args='--spring.profiles.active=h2-generic'

# H2 Server 모드로 실행 (서버 모드)
./gradlew bootRun --args='--spring.profiles.active=h2-server'
```

#### 방법 2: PostgreSQL로 실행
```bash
# PostgreSQL 데이터베이스 실행 (Docker)
docker-compose up -d postgres

# 애플리케이션 실행
./gradlew bootRun
```

#### 방법 3: Docker Compose로 실행
```bash
# Docker Compose로 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 4. 애플리케이션 접근
- **애플리케이션**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator Health**: http://localhost:8080/actuator/health
- **PostgreSQL Database**: localhost:5432/member_order_payment_db (PostgreSQL 모드)

## 📚 API 문서

### Swagger UI
인터랙티브 API 문서는 다음 URL에서 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 주요 API 엔드포인트

#### 회원 관리 API
```bash
# 회원 생성
POST /api/members
Content-Type: application/json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "phoneNumber": "010-1234-5678"
}

# 회원 조회
GET /api/members/{id}

# 회원 목록 조회
GET /api/members?page=0&size=10

# 회원 이메일로 조회
GET /api/members/email/{email}
```

#### 주문 관리 API
```bash
# 주문 생성
POST /api/orders
Content-Type: application/json
{
  "memberId": 1,
  "items": [
    {
      "productName": "상품명",
      "quantity": 2,
      "price": 10000
    }
  ]
}

# 주문 조회
GET /api/orders/{id}

# 주문 상태 업데이트
PUT /api/orders/{id}/status
{
  "status": "CONFIRMED"
}
```

#### 결제 관리 API
```bash
# 결제 생성
POST /api/payments
Content-Type: application/json
{
  "orderId": 1,
  "amount": 20000,
  "paymentMethod": "CARD"
}

# 결제 조회
GET /api/payments/{id}

# 결제 상태 업데이트
PUT /api/payments/{id}/status
{
  "status": "COMPLETED"
}
```

## ⭐ 주요 기능

### 1. 클린 아키텍처 구현
- **계층 분리**: Presentation, Service, Repository, Domain 계층
- **의존성 역전**: 도메인이 인프라에 의존하지 않음
- **단일 책임 원칙**: 각 클래스가 하나의 책임만 가짐

### 2. 도메인 주도 설계 (DDD)
- **값 객체**: `Email`, `PhoneNumber` 등 불변 객체
- **엔티티**: `Member`, `Order`, `Payment` 도메인 모델
- **도메인 이벤트**: 비즈니스 로직의 중요한 변화를 이벤트로 발행

### 3. 이벤트 기반 아키텍처
```java
// 도메인 이벤트 발행 예시
@Component
public class DomainEventListener {
    @EventListener
    public void handleMemberCreated(MemberCreated event) {
        log.info("회원 생성됨: {}", event.getMemberId());
        // 추가 비즈니스 로직 처리
    }
}
```

### 4. 성능 최적화
- **Spring Cache**: 자주 조회되는 데이터 캐싱
- **쿼리 최적화**: JPA 쿼리 튜닝
- **연결 풀링**: HikariCP 설정

### 5. 모니터링 및 관찰 가능성
- **헬스 체크**: 애플리케이션 상태 모니터링
- **메트릭**: 성능 지표 수집
- **로깅**: 구조화된 로그 출력

## 🐳 프로덕션 배포

### Docker 배포
```bash
# 이미지 빌드
docker build -t member-order-payment-system .

# 컨테이너 실행
docker run -p 8080:8080 member-order-payment-system
```

### Docker Compose 배포
```bash
# 프로덕션 환경 실행
docker-compose -f docker-compose.yml up -d

# Nginx 리버스 프록시 포함
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 환경 변수 설정
```bash
# 데이터베이스 설정
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
export SPRING_DATASOURCE_USERNAME=myuser
export SPRING_DATASOURCE_PASSWORD=mypassword

# 캐시 설정
export SPRING_CACHE_TYPE=redis
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
```

## 🧪 테스트

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "*MemberTest"

# 통합 테스트 실행
./gradlew integrationTest

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 테스트 구조
```
src/test/java/
├── com/example/
│   ├── domain/           # 도메인 단위 테스트
│   ├── service/          # 서비스 계층 테스트
│   ├── repository/       # 저장소 통합 테스트
│   └── controller/       # 컨트롤러 통합 테스트
```

### 테스트 예시
```java
@Test
@DisplayName("유효한 이메일로 회원 생성 성공")
void createMemberWithValidEmail_Success() {
    // Given
    CreateMemberRequest request = CreateMemberRequest.builder()
        .name("홍길동")
        .email("hong@example.com")
        .phoneNumber("010-1234-5678")
        .build();
    
    // When
    Member member = memberService.createMember(request);
    
    // Then
    assertThat(member.getName()).isEqualTo("홍길동");
    assertThat(member.getEmail().getValue()).isEqualTo("hong@example.com");
}
```

## 📊 모니터링

### Actuator 엔드포인트
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Info**: `/actuator/info`
- **Environment**: `/actuator/env`
- **Beans**: `/actuator/beans`
- **Caches**: `/actuator/caches`

### 헬스 체크 예시
```bash
curl http://localhost:8080/actuator/health
```

응답:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 499963174912,
        "threshold": 10485760
      }
    }
  }
}
```

### 메트릭 모니터링
```bash
# JVM 메트릭
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP 요청 메트릭
curl http://localhost:8080/actuator/metrics/http.server.requests

# 캐시 메트릭
curl http://localhost:8080/actuator/metrics/cache.gets
```

## 👨‍💻 개발자 가이드

### 프로젝트 구조
```
src/main/java/com/example/
├── controller/           # REST API 컨트롤러
├── service/             # 비즈니스 로직 서비스
├── repository/          # 데이터 접근 계층
├── domain/              # 도메인 모델 및 값 객체
│   ├── valueobject/     # 값 객체 (Email, PhoneNumber)
│   └── event/           # 도메인 이벤트
└── infrastructure/      # 인프라 계층
    ├── config/          # 설정 클래스
    └── event/           # 이벤트 리스너
```

### 코딩 컨벤션
- **패키지 명명**: 소문자, 점(.)으로 구분
- **클래스 명명**: PascalCase
- **메서드 명명**: camelCase
- **상수 명명**: UPPER_SNAKE_CASE

### Git 워크플로우
```bash
# 기능 브랜치 생성
git checkout -b feature/member-management

# 커밋 메시지 컨벤션
git commit -m "feat: 회원 생성 API 구현

- MemberController에 POST /api/members 엔드포인트 추가
- CreateMemberRequest DTO 생성
- 회원 생성 비즈니스 로직 구현"
```

### 코드 리뷰 체크리스트
- [ ] 클린 아키텍처 원칙 준수
- [ ] 도메인 로직이 올바른 계층에 위치
- [ ] 테스트 커버리지 충족
- [ ] 예외 처리 적절성
- [ ] 로깅 및 모니터링 고려

## 🗄️ H2 데이터베이스 가이드

### H2 데이터베이스 모드

#### 1. Embedded 모드 (인메모리)
```bash
./gradlew bootRun --args='--spring.profiles.active=h2-embedded'
```
- **데이터 저장**: 메모리에만 저장
- **지속성**: 애플리케이션 종료 시 데이터 손실
- **용도**: 개발, 테스트, 데모
- **URL**: `jdbc:h2:mem:devdb`

#### 2. Generic 모드 (파일 기반)
```bash
./gradlew bootRun --args='--spring.profiles.active=h2-generic'
```
- **데이터 저장**: `./data/h2database.mv.db` 파일
- **지속성**: ✅ 애플리케이션 재시작 후에도 데이터 유지
- **용도**: 개발, 소규모 프로덕션
- **URL**: `jdbc:h2:file:./data/h2database`

#### 3. Server 모드 (서버 프로세스)
```bash
# H2 서버 시작
./start-h2-server.sh

# 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=h2-server'
```
- **데이터 저장**: 서버 프로세스에서 관리
- **지속성**: ✅ 애플리케이션 재시작 후에도 데이터 유지
- **용도**: 다중 애플리케이션, 프로덕션
- **URL**: `jdbc:h2:tcp://localhost:9092/./data/h2server`

### H2 Console 접근
모든 모드에서 동일한 콘솔 사용:
- **URL**: http://localhost:8080/h2-console
- **사용자명**: `sa`
- **비밀번호**: (비어있음)

### H2 테스트 스크립트
```bash
# H2 기능 테스트
./check-h2.sh

# 모든 H2 모드 데모
./h2-modes-demo.sh
```

## 🔧 설정

### H2 데이터베이스 설정
```yaml
# application-h2-embedded.yml
spring:
  datasource:
    url: jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console
```

### PostgreSQL 데이터베이스 설정
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/member_order_payment_db
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  cache:
    type: simple
    cache-names: members,orders,payments

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 로깅 설정
```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.web: DEBUG
    org.springframework.cache: DEBUG
```

## 📚 문서 구조

프로젝트의 모든 문서는 `docs/` 디렉토리에 체계적으로 정리되어 있습니다:

### 📖 **사용자 가이드** (`docs/guides/`)
- **[면접 가이드](docs/guides/면접_가이드.md)** - 한국어 면접 프레젠테이션 가이드
- **[Interview Guide](docs/guides/INTERVIEW_GUIDE.md)** - 영어 면접 가이드
- **[Test Guide](docs/guides/TEST_GUIDE.md)** - 종합적인 테스트 가이드

### 🔧 **기술 문서** (`docs/technical/`)
- **[Architecture Analysis](docs/technical/ARCHITECTURE_ANALYSIS_AND_IMPROVEMENTS.md)** - 아키텍처 분석 및 개선 계획
- **[Architecture Diagram](docs/technical/ARCHITECTURE_DIAGRAM.md)** - 시스템 아키텍처 다이어그램
- **[Clean Code Analysis](docs/technical/CLEAN_CODE_ARCHITECTURE_ANALYSIS.md)** - 클린 코드 및 아키텍처 평가
- **[Project Improvements](docs/technical/PROJECT_IMPROVEMENTS.md)** - 모든 개선사항 요약
- **[Deployment Guide](docs/technical/DEPLOYMENT.md)** - 프로덕션 배포 가이드

### 🌐 **API 문서** (`docs/api/`)
- **[API Documentation](docs/api/API_DOCUMENTATION.md)** - 완전한 API 참조 및 예제

### 📋 **문서 인덱스**
- **[Documentation Index](docs/README.md)** - 모든 문서의 체계적인 인덱스

## 🚀 향후 개선 계획

### 단기 계획
- [ ] JWT 기반 인증/인가 시스템
- [ ] API 버전 관리
- [ ] 속도 제한 (Rate Limiting)
- [ ] 데이터베이스 마이그레이션 (Flyway)

### 중기 계획
- [ ] 메시지 큐 통합 (RabbitMQ/Kafka)
- [ ] 분산 추적 (Zipkin/Jaeger)
- [ ] 로드 테스트 (JMeter/Gatling)
- [ ] CI/CD 파이프라인 구축

### 장기 계획
- [ ] 마이크로서비스 아키텍처 전환
- [ ] 쿠버네티스 배포
- [ ] 서비스 메시 (Istio) 도입
- [ ] 이벤트 소싱 구현

## 📞 지원 및 문의

### 기술적 질문
- **이슈 등록**: GitHub Issues를 통한 버그 리포트 및 기능 요청
- **문서**: 프로젝트 내 상세 문서 참조

### 면접 관련 문의
이 프로젝트는 기술 면접을 위해 개발되었습니다. 면접에서 이 프로젝트에 대해 질문이 있으시면 언제든 문의해 주세요.

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

**이 프로젝트는 현대적인 Spring Boot 개발의 모범 사례를 보여주며, 클린 아키텍처, CQRS 패턴, 도메인 주도 설계를 적용한 엔터프라이즈급 애플리케이션 개발에 필요한 모든 요소를 포함하고 있습니다.** 🚀