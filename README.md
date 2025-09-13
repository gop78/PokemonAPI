# Pokemon API 프로젝트

Spring Boot를 사용하여 구축된 Pokemon API 서비스입니다. 외부 PokeAPI와 연동하여 포켓몬 정보를 조회하고, 로컬 데이터베이스에 캐싱하는 RESTful API를 제공합니다.

## 🚀 주요 기능

- **포켓몬 개별 조회**: 이름 또는 ID로 특정 포켓몬 정보 검색
- **포켓몬 목록 조회**: 페이징을 지원하는 포켓몬 리스트 제공
- **캐싱 시스템**: Redis를 통한 성능 최적화
- **데이터 영속성**: H2 데이터베이스를 통한 로컬 데이터 저장
- **초기 데이터 로딩**: 애플리케이션 시작 시 1세대 포켓몬(151마리) 자동 로드
- **포괄적인 예외 처리**: GlobalExceptionHandler를 통한 통합 오류 관리

## 🛠 기술 스택

- **Framework**: Spring Boot 3.3.4
- **Java Version**: 17
- **Database**: H2 Database (개발용)
- **Cache**: Redis
- **HTTP Client**: OpenFeign
- **Build Tool**: Gradle
- **External API**: [PokeAPI](https://pokeapi.co/)

## 📋 사전 요구사항

- Java 17 이상
- Redis 서버 (localhost:6379)
- Gradle 8.14.3 이상

## 📚학습 내용
- ResponseEntity
- GlobalExceptionHandler
- ...

## ⚙️ 설치 및 실행

### 1. Redis 서버 시작
```bash
# macOS (Homebrew)
brew services start redis

# Docker
docker run -d -p 6379:6379 redis:alpine
```

### 2. 애플리케이션 실행
```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/pokemonApi-0.0.1-SNAPSHOT.jar
```

## 🌐 API 엔드포인트

### 기본 URL: `http://localhost:80/api/pokemon`

#### 개별 포켓몬 조회
```bash
# 이름으로 조회
curl http://localhost:80/api/pokemon/pikachu

# ID로 조회  
curl http://localhost:80/api/pokemon/25
```

#### 포켓몬 목록 조회
```bash
curl "http://localhost:80/api/pokemon?page=0&size=10"
```

#### 헬스체크
```bash
curl http://localhost:80/api/pokemon/health
```

## 💾 데이터베이스

### H2 Console 접근
- URL: http://localhost:80/h2-console
- JDBC URL: `jdbc:h2:file:./data/pokemondb`
- Username: `sa`, Password: (공백)

## 🏗 프로젝트 구조

```
src/main/java/com/pokeapi/
├── client/          # 외부 API 클라이언트
├── config/          # 설정 클래스들
├── controller/      # REST 컨트롤러
├── entity/          # JPA 엔티티
├── exception/       # 사용자 정의 예외 클래스
├── model/           # DTO 및 모델 클래스
├── repository/      # JPA 레포지토리
├── service/         # 비즈니스 로직
└── util/           # 유틸리티 클래스
```

## ⚡ 성능 최적화

- **Redis 캐싱**: 자주 조회되는 포켓몬 데이터 캐싱
- **JPA 2차 캐시**: 데이터베이스 쿼리 최적화  
- **배치 로딩**: 초기 데이터 로드 시 효율적인 배치 처리

## 🔧 설정 커스터마이징

`application.yml`에서 주요 설정 조정 가능:

```yaml
app:
  data:
    initial-load-enabled: true    # 초기 데이터 로드 활성화
    initial-load-count: 151       # 로드할 포켓몬 수
    batch-size: 10                # 배치 크기
```

## 🧪 테스트

```bash
./gradlew test
```

## ⚡ Quick Start

```bash
# 1. Redis 시작
brew services start redis

# 2. 애플리케이션 실행  
./gradlew bootRun

# 3. API 테스트
curl http://localhost:80/api/pokemon/pikachu
```

