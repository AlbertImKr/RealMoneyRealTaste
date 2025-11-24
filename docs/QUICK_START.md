# 🚀 빠른 시작 가이드

## 📋 사전 요구사항

- **Java 17+**
- **Docker** & **Docker Compose**
- **Gradle 8+**

## 🛠 설치 및 실행

### 1. 프로젝트 클론

```bash
git clone https://github.com/AlbertImKr/RealMoneyRealTaste.git
cd RealMoneyRealTaste
```

### 2. 환경 설정

```bash
# 환경 변수 파일 복사
cp .env.example .env

# 필요한 경우 .env 파일 수정
vim .env
```

### 3. 데이터베이스 시작

```bash
# Testcontainers를 사용하면 자동으로 시작됩니다
# 또는 직접 MySQL을 실행할 경우:
docker-compose up -d mysql
```

### 4. 애플리케이션 실행

```bash
# 개발 모드
./gradlew bootRun

# 테스트 실행
./gradlew test

# 빌드
./gradlew build
```

### 5. 접속

애플리케이션이 시작되면 다음 주소로 접속하세요:

- **메인 페이지**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html (준비중)

## 🧪 테스트

### 통합 테스트 실행

```bash
# 전체 테스트
./gradlew test

# API 테스트만 실행
./gradlew test --tests "*ApiTest*"

# WebView 테스트만 실행
./gradlew test --tests "*ViewTest*"

# 애플리케이션 로직 테스트만 실행
./gradlew test --tests "*application*"

# 특정 테스트 클래스
./gradlew test --tests "*CollectionDeleteApiTest*"

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

### 테스트 구조

프로젝트는 **통합 테스트 중심**의 테스트 전략을 사용합니다:

- **IntegrationTestBase**: 모든 통합 테스트의 기본 클래스
- **@WithMockMember**: 인증된 사용자 시뮬레이션
- **TestMemberHelper**: 테스트 멤버 생성 유틸리티
- **TestPostHelper**: 테스트 포스트 생성 유틸리티
- **CSRF 적용**: 모든 API 요청에 보호 적용

### Testcontainers

프로젝트는 Testcontainers를 사용하여 테스트용 데이터베이스를 자동으로 관리합니다:

- **MySQL 8.0** 컨테이너 자동 시작
- **CI 환경** 최적화 설정
- **성능 튜닝** 적용 (메모리 64MB, 시작 타임아웃 5분)

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── kotlin/          # Kotlin 소스 코드
│   └── resources/       # 설정 파일 및 템플릿
└── test/
    ├── kotlin/          # 테스트 코드
    └── resources/       # 테스트 설정
```

## 🔧 개발 환경 설정

### IntelliJ IDEA

1. **프로젝트 열기**: File → Open → 프로젝트 폴더 선택
2. **Kotlin 플러그인**: 자동으로 설치됨
3. **Gradle 설정**: Use Gradle from 'gradle-wrapper.properties'
4. **코드 스타일**: 프로젝트의 .editorconfig 적용

### VS Code

1. **Extension Pack for Java** 설치
2. **Kotlin Language** 확장 프로그램 설치
3. **Gradle for Java** 확장 프로그램 설치

## 🐛 문제 해결

### 일반적인 문제들

**Q: 포트가 이미 사용 중입니다**

```bash
# 포트 확인 및 종료
lsof -ti:8080 | xargs kill -9
```

**Q: Testcontainers 시작 실패**

```bash
# Docker 데몬 확인
docker ps

# 권한 문제 해결
sudo usermod -aG docker $USER
```

**Q: 메모리 부족**

```bash
# Gradle 메모리 증가
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
```

## 📞 지원

문제가 발생하면 다음을 확인하세요:

1. [Issues](https://github.com/AlbertImKr/RealMoneyRealTaste/issues) 검색
2. 새 이슈 생성 시 상세 정보 포함
3. [아키텍처 문서](ARCHITECTURE.md) 참고

## 🔄 다음 단계

- [API 문서](API_DOCUMENTATION.md) 확인
- [도메인 모델](DOMAIN_MODEL.md) 이해
- [테스트 가이드](TESTING_GUIDE.md) 참고

---

**Happy Coding! 🎉**
