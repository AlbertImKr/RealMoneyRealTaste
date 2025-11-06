# RealMoneyRealTaste 아키텍처 문서

> 본 문서는 RealMoneyRealTaste 시스템의 아키텍처 설계와 도메인 모델링에 대해 설명합니다.

## 목차

- [1. 아키텍처 원칙](#1-아키텍처-원칙)
- [2. 레이어별 구현 예시](#2-레이어별-구현-예시)
- [3. 프로젝트 구조](#3-프로젝트-구조)

---

## 1. 아키텍처 원칙

### 1.1 Clean Architecture + DDD (Domain-Driven Design)

RMRT는 **클린 아키텍처**와 **도메인 주도 설계**를 기반으로 구축되었습니다.

**핵심 원칙**

- **의존성 역전**: 고수준 모듈이 저수준 모듈에 의존하지 않음
- **관심사 분리**: 비즈니스 로직과 기술적 구현의 명확한 분리
- **테스트 가능성**: 모든 레이어의 독립적 테스트 지원
- **확장성**: 새로운 요구사항에 유연하게 대응

### 1.2 헥사고날 아키텍처 (Ports and Adapters)

```
┌──────────────────────────────────────────────────────────┐
│                    Adapter Layer                         │
│  ┌─────────────────┐         ┌─────────────────────────┐ │
│  │   Web Adapter   │         │ Infrastructure Adapter  │ │
│  │  Controllers    │         │  Repositories, Email    │ │
│  │  Views, Forms   │         │  External Services      │ │
│  │  Exception      │         │  Configuration          │ │
│  │  Handlers       │         │                         │ │
│  └─────────────────┘         └─────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│                  Application Layer                       │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              Use Case Services                      │ │
│  │   Registration, Authentication, Post Management     │ │
│  │                                                     │ │
│  │  ┌─────────────┐           ┌─────────────────────┐  │ │
│  │  │  Provided   │           │     Required        │  │ │
│  │  │   Ports     │◄─────────►│      Ports          │  │ │
│  │  │ (Interfaces │           │   (Interfaces)      │  │ │
│  │  │ for Inbound)│           │  for Outbound)      │  │ │
│  │  └─────────────┘           └─────────────────────┘  │ │
│  └─────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│  ┌─────────────────────────────────────────────────────┐ │
│  │               Business Logic                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │ │
│  │  │   Member    │  │    Post     │  │   Token     │  │ │
│  │  │ Aggregate   │  │ Aggregate   │  │ Aggregates  │  │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │ │
│  │                                                     │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │ │
│  │  │   Value     │  │   Domain    │  │   Domain    │  │ │
│  │  │  Objects    │  │   Events    │  │  Services   │  │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │ │
│  └─────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

**의존성 방향**: Adapter → Application → Domain (단방향)

---

## 2. 레이어별 구현 예시

### 2.1 Domain Layer (도메인 레이어)

**책임**: 핵심 비즈니스 로직과 규칙 구현

```kotlin
// 엔티티 예시 - Member
@Entity
class Member protected constructor(
    email: Email,
    nickname: Nickname,
    passwordHash: PasswordHash,
    // ...
) : BaseEntity() {

    fun activate() {
        if (status != MemberStatus.PENDING) {
            throw InvalidMemberStatusException.NotPending("등록 대기 상태에서만 등록 완료가 가능합니다")
        }
        status = MemberStatus.ACTIVE
        detail.activate()
        updatedAt = LocalDateTime.now()
    }

    companion object {
        fun register(email: Email, nickname: Nickname, password: PasswordHash): Member {
            return Member(
                email = email,
                nickname = nickname,
                passwordHash = password,
                detail = MemberDetail.register(),
                trustScore = TrustScore.create(),
                status = MemberStatus.PENDING,
                roles = Roles.ofUser(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}
```

**구성 요소**

- **Aggregates**: Member, Post, ActivationToken, PasswordResetToken
- **Value Objects**: Email, Nickname, PasswordHash, PostContent
- **Domain Services**: PasswordEncoder 인터페이스
- **Domain Events**: MemberRegisteredEvent, PostCreatedEvent

### 2.2 Application Layer (애플리케이션 레이어)

**책임**: 유스케이스 조율 및 트랜잭션 관리

```kotlin
@Service
@Transactional
class MemberRegistrationService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : MemberRegister {

    override fun register(request: MemberRegisterRequest): Member {
        validateEmailNotDuplicated(request)
        val passwordHash = PasswordHash.of(request.password, passwordEncoder)
        val member = Member.register(request.email, request.nickname, passwordHash)
        val savedMember = memberRepository.save(member)
        publishMemberRegisteredEvent(savedMember)
        return savedMember
    }
}
```

**포트 (Ports)**

- **Provided Ports (인바운드)**: 외부에서 호출하는 인터페이스
    - `MemberRegister`, `PostCreator`, `MemberVerify`
- **Required Ports (아웃바운드)**: 외부 의존성 인터페이스
    - `MemberRepository`, `EmailSender`, `ActivationTokenGenerator`

### 2.3 Adapter Layer (어댑터 레이어)

**Web Adapter (인바운드 어댑터)**

```kotlin
@Controller
class MemberView(
    private val memberRegister: MemberRegister,
    private val memberVerify: MemberVerify,
) {

    @PostMapping("/signup")
    fun signup(@Valid form: SignupForm): String {
        val request = MemberRegisterRequest(form.email, form.nickname, form.password)
        memberRegister.register(request)
        return "redirect:/"
    }
}
```

**Infrastructure Adapter (아웃바운드 어댑터)**

```kotlin
@Repository
class JpaMemberRepository(
    private val jpaRepository: SpringDataMemberRepository
) : MemberRepository {

    override fun save(member: Member): Member = jpaRepository.save(member)
    override fun findByEmail(email: Email): Member? = jpaRepository.findByEmail(email.address)
    override fun findById(id: Long): Member? = jpaRepository.findById(id).orElse(null)
}
```

## 3. 프로젝트 구조

```
src/main/kotlin/com/albert/realmoneyrealtaste/
├── adapter/                          # 어댑터 레이어
│   ├── web/                          # 웹 어댑터
│   │   ├── member/                   # 회원 관련 컨트롤러
│   │   │   ├── MemberView.kt
│   │   │   ├── MemberExceptionHandler.kt
│   │   │   └── AuthView.kt
│   │   └── post/                     # 게시글 관련 컨트롤러
│   │       └── PostView.kt
│   ├── infrastructure/               # 인프라 어댑터
│   │   ├── persistence/              # 데이터베이스 어댑터
│   │   │   ├── member/
│   │   │   └── post/
│   │   ├── email/                    # 이메일 어댑터
│   │   │   └── EmailSenderImpl.kt
│   │   └── security/                 # 보안 설정
│   │       ├── SecurityConfig.kt
│   │       └── CustomAuthenticationProvider.kt
│   └── configuration/                # 설정 클래스
├── application/                      # 애플리케이션 레이어
│   ├── member/                       # 회원 유스케이스
│   │   ├── service/                  # 애플리케이션 서비스
│   │   │   ├── MemberRegistrationService.kt
│   │   │   ├── MemberActivationService.kt
│   │   │   └── MemberUpdateService.kt
│   │   ├── dto/                      # 데이터 전송 객체
│   │   │   ├── MemberRegisterRequest.kt
│   │   │   └── AccountUpdateRequest.kt
│   │   ├── event/                    # 도메인 이벤트
│   │   │   ├── MemberRegisteredEvent.kt
│   │   │   └── MemberEventListener.kt
│   │   ├── provided/                 # 제공 포트 (인바운드)
│   │   │   ├── MemberRegister.kt
│   │   │   ├── MemberVerify.kt
│   │   │   └── MemberReader.kt
│   │   └── required/                 # 요구 포트 (아웃바운드)
│   │       ├── MemberRepository.kt
│   │       ├── ActivationTokenRepository.kt
│   │       └── EmailSender.kt
│   └── post/                         # 게시글 유스케이스
│       ├── service/
│       ├── dto/
│       ├── provided/
│       └── required/
└── domain/                           # 도메인 레이어
    ├── member/                       # 회원 도메인
    │   ├── Member.kt                 # 회원 애그리게이트 루트
    │   ├── MemberDetail.kt
    │   ├── TrustScore.kt
    │   ├── ActivationToken.kt
    │   ├── value/                    # 값 객체
    │   │   ├── Email.kt
    │   │   ├── Nickname.kt
    │   │   ├── PasswordHash.kt
    │   │   └── Roles.kt
    │   ├── service/                  # 도메인 서비스
    │   │   └── PasswordEncoder.kt
    │   └── exceptions/               # 도메인 예외
    │       ├── EmailValidationException.kt
    │       └── InvalidMemberStatusException.kt
    ├── post/                         # 게시글 도메인
    │   ├── Post.kt
    │   ├── PostHeart.kt
    │   └── value/
    │       ├── Author.kt
    │       ├── Restaurant.kt
    │       ├── PostContent.kt
    │       └── PostImages.kt
    └── common/                       # 공통 도메인 요소
        └── BaseEntity.kt
```

