# RMRT (Real Money Real Taste)

> 진짜 내돈내산 푸디들의 소셜 플랫폼

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)

---

## 🌟 핵심 가치

**광고성 리뷰는 NO! 순수 내돈내산 리뷰만 YES!** 🙅‍♂️💰

- **Real Money First**: 내돈내산 리뷰 최우선
- **Transparent Disclosure**: 광고성/내돈내산 명확 구분
- **Trust-Based Ranking**: 신뢰도 기반 랭킹 시스템

## ✨ 주요 기능

🎯 **내돈내산 인증** | 👥 **소셜 네트워킹** | 📍 **위치 기반 검색** | 🏆 **신뢰도 시스템**

## 🛠 기술 스택

**Backend**: Kotlin, Spring Boot, JPA, MySQL \
**Cloud**: AWS (S3, ECS, RDS, Route53, ALB) \
**Testing**: JUnit5, MockK, Testcontainers, LocalStack \
**DevOps**: Docker, GitHub Actions, SonarCloud, Flyway

## 🌐 배포

- **프로덕션**: https://rmrt.albert-im.com/
- **인프라**: AWS 기반 컨테이너 오케스트레이션 (ECS, RDS, ALB)
- **CI/CD**: GitHub Actions 기반 자동화 파이프라인

### ☁️ 클라우드 아키텍처

```
┌─────────────────┐    ┌────────────────────┐    ┌────────────────────┐
│   GitHub Actions│───▶│     AWS ECR        │───▶│   Amazon ECS       │
│ (CI/CD Pipeline)│    │(Container Registry)│    │ (Container Service)│
└─────────┬───────┘    └────────────────────┘    └────────┬───────────┘
          │                                               │
          │             ┌─────────────────┐               │
          │             │  Amazon Route 53│ ◀─────────────┘
          │             │ (DNS Management)│
          │             └────────┬────────┘
          │                      │
          ▼                      ▼
┌─────────────────┐     ┌─────────────────┐       ┌─────────────────┐
│    SonarCloud   │     │   Application   │       │   Amazon RDS    │
│  (Code Quality) │     │   Load Balancer │       │   (MySQL 8.0)   │
└─────────────────┘     │      (ALB)      │       │   (Multi-AZ)    │
                        └────────┬────────┘       └─────────────────┘
                                 │                         │
                                 ▼                         │
                        ┌─────────────────┐                │
                        │   ECS Cluster   │   ◀────────────┘
                        │  (rmrt-cluster) │
                        └────────┬────────┘
                                 │
                        ┌────────┴────────┐
                        │    ECS Task     │
                        │   (rmrt-task)   │
                        └─────────────────┘
                                 ▲
                                 │
                        ┌────────┴────────┐
                        ▼                 ▼
                ┌─────────────┐   ┌─────────────────┐
                │  Amazon S3  │   │   CloudWatch    │
                │   (Image    │   │  (Monitoring)   │
                │   Storage)  │   └─────────────────┘
                └─────────────┘
```

### 📊 데이터베이스 구조

![ERD 다이어그램](docs/erd.png)

## 📚 문서

- [📋 도메인 요구사항](docs/DOMAIN_REQUIREMENTS.md)
- [🏗 도메인 모델](docs/DOMAIN_MODEL.md)
- [🏛 아키텍처](docs/ARCHITECTURE.md)
- [🚀 빠른 시작](docs/QUICK_START.md)
- [📖 API 문서](docs/API_DOCUMENTATION.md)
-
    - [📷 이미지 관리 시스템](docs/IMAGE_MANAGEMENT.md)
- [🧪 테스트 가이드](docs/TESTING_GUIDE.md)
- [✅ TODO 리스트](docs/TODO.md)

## 🚀 빠른 시작

```bash
git clone https://github.com/AlbertImKr/RealMoneyRealTaste.git
cd RealMoneyRealTaste
./gradlew bootRun
```

자세한 설치 및 실행 방법은 [빠른 시작 가이드](docs/QUICK_START.md)를 참고하세요.

## 📄 라이선스

MIT License
