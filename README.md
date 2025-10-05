# RMRT (Real Money Real Taste) - 진짜 내돈내산 푸디 소셜 플랫폼

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=AlbertImKr_RealMoneyRealTaste&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=AlbertImKr_RealMoneyRealTaste)

## 목차

- [1. 도메인 개요](#1-도메인-개요)
    - [1.1 서비스 목적](#11-서비스-목적)
    - [1.2 핵심 컨셉](#12-핵심-컨셉)
- [2. 도메인 요구사항 정의](#2-도메인-요구사항-정의)
    - [2.1 회원 관리 (Member Management)](#21-회원-관리-member-management)
    - [2.2 푸디 인플루언서 (Foodie Influencer)](#22-푸디-인플루언서-foodie-influencer)
    - [2.3 리뷰 작성 및 관리 (Review Management)](#23-리뷰-작성-및-관리-review-management)
    - [2.4 식당 정보 관리 (Restaurant Management)](#24-식당-정보-관리-restaurant-management)
    - [2.5 소셜 네트워킹 (Social Networking)](#25-소셜-네트워킹-social-networking)
    - [2.6 리뷰 유형 및 신뢰도 시스템 (Review Type & Trust System)](#26-리뷰-유형-및-신뢰도-시스템-review-type--trust-system)
    - [2.7 커뮤니티 신뢰도 (Community Trust)](#27-커뮤니티-신뢰도-community-trust)
    - [2.8 피드 및 추천 시스템 (Feed & Recommendation System)](#28-피드-및-추천-시스템-feed--recommendation-system)
- [3. RMRT 도메인 모델](#3-rmrt-도메인-모델)
  - [3.1 회원 애그리거트 (Member Aggregate)](#31-회원-애그리거트-member-aggregate)
    - [3.1.1 회원 (Member)](#311-회원-member)
    - [3.1.2 회원 상세(MemberDetail)](#312-회원-상세memberdetail)
    - [3.1.3 회원 상태(MemberStatus)](#313-회원-상태memberstatus)
    - [3.1.4 신뢰도 점수(TrustScore)](#314-신뢰도-점수trustscore)
    - [3.1.5 신뢰도 레벨(TrustLevel)](#315-신뢰도-레벨trustlevel)

## 1. 도메인 개요

### 1.1 서비스 목적

"진짜 내돈내산 푸디들의 소셜 플랫폼"

### 1.2 핵심 컨셉:

- Real Money First (내돈내산 우선): 내돈내산 리뷰를 가장 높은 가치로 평가
- Transparent Disclosure (투명한 표시): 광고성/내돈내산 구분을 명확히 표시
- Trust-Based Ranking (신뢰도 기반 랭킹): 내돈내산 리뷰에 더 높은 신뢰도 부여
- Community Driven (커뮤니티 주도): 내돈내산 중심의 순수한 커뮤니티 문화 추구

## 2. 도메인 요구사항 정의

### 2.1 회원 관리 (Member Management)

- RMRT에서 활동하기 위해서는 회원으로 등록해야 한다.
    - 다만, 회원이 되기 전에도 RMRT에 대한 소개와 공개된 리뷰를 살펴볼 수 있다.
    - 리뷰를 작성하고 소셜 기능을 이용하기 위해서는 등록을 완료하고 활동 가능한 회원이 되어야 한다.
    - 등록 신청을 한 뒤 이메일 인증 등 정해진 요건을 충족하면 등록이 완료된다.
    - 등록을 완료한 경우에 닉네임, 프로필 사진, 자기소개를 등록하거나 수정할 수 있다.
        - 닉네임은 한글, 영문, 숫자로 구성된 20자 이내의 중복되지 않은 값
        - 탈퇴한 회원의 닉네임과 프로필 정보는 수정할 수 없다.
    - 가입 시간, 등록 완료 시간, 탈퇴 시간을 저장한다.

### 2.2 푸디 인플루언서 (Foodie Influencer)

- 원하는 회원은 자신의 맛집 전문성을 바탕으로 다른 회원에게 신뢰할 수 있는 추천을 제공하는 푸디 인플루언서가 될 수도 있다.
    - 회원이 최초로 푸디 인플루언서가 되려면 일정 조건(리뷰 수, 신뢰도 점수 등)을 충족해야 한다.
    - 인플루언서는 특별한 리뷰 권한과 큐레이션 기능을 사용할 수 있다.
  - 인플루언서의 추천은 일반 회원보다 높은 가중치로 피드에 노출된다.

### 2.3 리뷰 작성 및 관리 (Review Management)

- 리뷰 작성은 실제 방문 경험을 기록하고 공유하는 것을 말한다. "내가 작성한 리뷰는 a이다. 나는 b 식당 리뷰를 작성중이다."
    - 리뷰 작성을 위해서는 먼저 리뷰 유형 선택이 필요하다.
    - 리뷰 유형은 "내돈내산" 또는 "광고성" 중 하나를 필수로 선택해야 한다.
    - 내돈내산: 내 돈으로 직접 구매한 순수 개인 경험 리뷰
    - 광고성: 협찬, 제공, 할인, 초대 등 혜택을 받고 작성하는 리뷰
    - 광고성 리뷰는 피드에서 명확한 **[광고]** 표시와 함께 노출된다.
    - 내돈내산 리뷰는 **[내돈내산]** 인증 마크와 함께 우선 노출된다.

### 2.4 식당 정보 관리 (Restaurant Management)

- 식당은 위치, 카테고리, 기본 정보와 같은 메타데이터를 가진 하나 이상의 메뉴로 구성된다.
    - 메뉴는 개수가 많을 수 있다. 그래서 메뉴는 카테고리로 구분한다.
    - 하나의 식당은 여러 개의 메뉴 카테고리와 각 카테고리에 속한 메뉴로 구성된다.
    - 메뉴와 카테고리는 표시 순서를 가진다.
    - 리뷰는 전체 식당에 대한 리뷰 또는 특정 메뉴에 대한 리뷰로 작성할 수 있다.
    - 식당의 모든 메뉴가 충분한 리뷰를 확보하면 해당 식당은 **[검증된 맛집]** 이 된다.

### 2.5 소셜 네트워킹 (Social Networking)

- 회원은 다른 회원을 팔로우하여 소셜 네트워크를 형성할 수 있다.
    - 팔로우 관계는 일방향이며 상호 팔로우 시 "맛친구" 관계가 된다.
    - 팔로우한 회원의 리뷰는 개인 피드에 우선적으로 노출된다.
    - 회원은 리뷰에 좋아요, 댓글, 공유 등의 상호작용을 할 수 있다.
    - 도움이 되는 리뷰에는 **[도움됨]** 반응을 줄 수 있으며 이는 작성자의 신뢰도 향상에 기여한다.

### 2.6 리뷰 유형 및 신뢰도 시스템 (Review Type & Trust System)

- 모든 리뷰는 내돈내산 또는 광고성 중 하나의 유형을 명시해야 한다.
    - 내돈내산 리뷰: 개인이 직접 비용을 지불하고 작성한 리뷰로 가장 높은 신뢰도를 갖는다.
    - 광고성 리뷰: 협찬, 제공, 할인 등의 혜택을 받고 작성한 리뷰로 명확히 표시한다.
    - 리뷰 유형에 따라 피드 노출 우선순위와 신뢰도 점수가 차등 적용된다.
    - 사용자는 필터 기능을 통해 내돈내산 리뷰만 선별해서 볼 수 있다.
    - 잘못된 유형으로 분류된 리뷰는 커뮤니티 신고를 통해 수정될 수 있다.

### 2.7 커뮤니티 신뢰도 (Community Trust)

- 회원의 신뢰도는 내돈내산 리뷰 비율과 커뮤니티 기여도를 바탕으로 산정된다.
    - 내돈내산 리뷰: 신뢰도 +5점
    - 광고성 리뷰: 신뢰도 +1점
    - 내돈내산 비율이 높을수록 더 높은 등급을 받는다.
    - 신뢰도에 따라 브론즈, 실버, 골드, 다이아몬드 등급이 부여된다.
    - 잘못된 리뷰 유형 분류가 적발되면 신뢰도가 대폭 하락한다.

### 2.8 피드 및 추천 시스템 (Feed & Recommendation System)

- 개인 피드는 내돈내산 리뷰 우선으로 개인화된 콘텐츠를 제공한다.
    - 피드 우선순위: 내돈내산 리뷰 > 광고성 리뷰 > 관심 카테고리 > 트렌딩 순
    - 내돈내산 필터 기능으로 순수 내돈내산 리뷰만 선별 조회 가능
    - AI 기반 개인 취향 분석으로 맞춤형 내돈내산 맛집 우선 추천

# 3. RMRT 도메인 모델

## 3.1 회원 애그리거트 (Member Aggregate)

### 3.1.1 회원 (Member)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 회원 식별자 (PK, BaseEntity에서 상속)
* `email: Email` - 이메일 (Embedded, Natural ID)
* `nickname: Nickname` - 닉네임 (Embedded)
* `passwordHash: PasswordHash` - 비밀번호 해시 (Embedded)
* `status: MemberStatus` - 회원 상태 (Enum)
* `detail: MemberDetail` - 회원 상세 정보 (1:1 관계, Cascade)
* `trustScore: TrustScore` - 신뢰도 점수 (1:1 관계, Cascade)
* `updatedAt: LocalDateTime` - 마지막 수정 일시

#### 행위

* `static register(Email, Nickname, PasswordHash): Member`
    - 회원 등록
    - 초기 상태: PENDING
    - MemberDetail과 TrustScore를 초기 상태로 생성
    - registeredAt을 현재 시간으로 설정

* `activate(): void`
    - 회원 활성화 (이메일 인증 완료)
    - PENDING → ACTIVE 상태 전이
    - detail.activate() 호출하여 activatedAt 기록
    - updatedAt 갱신

* `deactivate(): void`
    - 회원 탈퇴
    - ACTIVE → DEACTIVATED 상태 전이
    - detail.deactivate() 호출하여 deactivatedAt 기록
    - updatedAt 갱신

* `verifyPassword(RawPassword, PasswordEncoder): boolean`
    - 비밀번호 검증
    - 입력된 평문 비밀번호와 저장된 해시 비교

* `changePassword(PasswordHash): void`
    - 비밀번호 변경
    - ACTIVE 상태에서만 가능
    - updatedAt 갱신

* `updateInfo(Nickname?, ProfileAddress?, Introduction?): void`
    - 회원 정보 수정
    - ACTIVE 상태에서만 가능
    - null이 아닌 값만 업데이트
    - updatedAt 갱신

* `updateTrustScore(TrustScore): void`
    - 신뢰도 점수 업데이트
    - 새로운 TrustScore로 교체

* `canWriteReview(): boolean`
    - 리뷰 작성 권한 확인
    - ACTIVE 상태일 때만 true

#### 비즈니스 규칙

* 회원 생성 후 초기 상태는 PENDING
* PENDING 상태에서만 activate() 가능
* ACTIVE 상태에서만 deactivate() 가능
* ACTIVE 상태에서만 정보 수정 및 비밀번호 변경 가능
* 닉네임은 중복 불가 (애플리케이션 레이어에서 검증)
* 이메일은 중복 불가 (DB 제약조건)

#### 인덱스

* `idx_member_email`: email 컬럼
* `idx_member_nickname`: nickname 컬럼
* `idx_member_status`: status 컬럼

---

### 3.1.2 회원 상세 (MemberDetail)

**_Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `profileAddress: ProfileAddress?` - 프로필 주소 (Embedded, nullable)
* `introduction: Introduction?` - 자기 소개 (Embedded, nullable)
* `registeredAt: LocalDateTime` - 등록 일시
* `activatedAt: LocalDateTime?` - 활성화 일시 (nullable)
* `deactivatedAt: LocalDateTime?` - 탈퇴 일시 (nullable)

#### 행위

* `static register(ProfileAddress?, Introduction?): MemberDetail`
    - 회원 상세 정보 생성
    - registeredAt을 현재 시간으로 설정

* `static register(): MemberDetail`
    - 기본값으로 회원 상세 정보 생성 (프로필 주소, 소개 없음)

* `activate(): void`
    - 활성화 일시 기록

* `deactivate(): void`
    - 탈퇴 일시 기록

* `updateInfo(ProfileAddress?, Introduction?): void`
    - 프로필 주소와 자기 소개 업데이트

---

### 3.1.3 회원 상태 (MemberStatus)

**_Enum_**

#### 상수

* `PENDING` - 등록 대기 (이메일 인증 전)
* `ACTIVE` - 등록 완료 (정상 활동 가능)
* `DEACTIVATED` - 탈퇴

---

### 3.1.4 신뢰도 점수 (TrustScore)

**_Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `score: int` - 신뢰도 점수 (0-1000)
* `level: TrustLevel` - 신뢰도 레벨 (Enum)
* `realMoneyReviewCount: int` - 내돈내산 리뷰 수
* `adReviewCount: int` - 광고성 리뷰 수

#### 행위

* `static create(): TrustScore`
    - 초기 신뢰도 생성 (점수 0, BRONZE 레벨, 리뷰 수 0)

* `static calculateScore(int, int, int, int): int`
    - 신뢰도 점수 계산 (정적 메서드)
    - 파라미터: realMoneyReviewCount, adReviewCount, helpfulCount, penaltyCount
    - 공식: (내돈내산×5) + (광고×1) + (도움됨×2) - (위반×20)
    - 범위: 0-1000

* `addRealMoneyReview(): void`
    - 내돈내산 리뷰 추가
    - 점수 +5, 리뷰 수 +1
    - 레벨 재계산

* `addAdReview(): void`
    - 광고성 리뷰 추가
    - 점수 +1, 리뷰 수 +1
    - 레벨 재계산

* `penalize(int): void`
    - 점수 감소
    - 최소값 0으로 제한
    - 레벨 재계산

* `getRealMoneyRatio(): double`
    - 내돈내산 리뷰 비율 계산
    - 전체 리뷰 대비 내돈내산 리뷰 비율

#### 상수

* `REAL_MONEY_REVIEW_WEIGHT = 5`
* `AD_REVIEW_WEIGHT = 1`
* `HELPFUL_VOTE_WEIGHT = 2`
* `PENALTY_WEIGHT = 20`

#### 규칙

* Entity로 관리 (Member와 1:1 관계)
* 점수 범위: 0-1000
* 점수 변경 시 자동으로 레벨 재계산
* 리뷰 추가 시 최대값 1000으로 제한

---

### 3.1.5 신뢰도 레벨 (TrustLevel)

**_Enum_**

#### 상수

* `BRONZE(0..199)` - 브론즈 (0-199점)
* `SILVER(200..499)` - 실버 (200-499점)
* `GOLD(500..799)` - 골드 (500-799점)
* `DIAMOND(800..1000)` - 다이아몬드 (800-1000점)

#### 속성

* `scoreRange: IntRange` - 점수 범위

#### 행위

* `static fromScore(int): TrustLevel`
    - 점수로부터 레벨 결정
    - 점수 범위에 해당하는 레벨 반환
    - 범위 밖이면 NoSuchElementException 발생

---

### 3.1.6 이메일 (Email)

**_Value Object (Embeddable)_**

#### 속성

* `address: String` - 이메일 주소
    - `@NaturalId` 적용
    - `unique = true, nullable = false`

#### 행위

* `getDomain(): String`
    - '@' 뒤의 도메인 부분 반환

* `validate(): void` (private, init 블록에서 호출)
    - 이메일 형식 검증
    - 정규식: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*\.[A-Za-z]{2,}$`

#### 규칙

* blank 불가
* 이메일 형식 준수 필수
* 불변 객체 (data class)

---

### 3.1.7 닉네임 (Nickname)

**_Value Object (Embeddable)_**

#### 속성

* `value: String` - 닉네임
    - `unique = true, nullable = false`
    - `length = 20`

#### 행위

* `validate(): void` (private, init 블록에서 호출)
    - 닉네임 유효성 검증
    - 정규식: `^[가-힣a-zA-Z0-9]+$`

#### 규칙

* blank 불가
* 길이: 2-20자
* 한글, 영문, 숫자만 허용
* 불변 객체 (data class)

---

### 3.1.8 비밀번호 해시 (PasswordHash)

**_Value Object (Embeddable)_**

#### 속성

* `hash: String` - 비밀번호 해시값 (private)
    - `nullable = false`

#### 행위

* `static of(RawPassword, PasswordEncoder): PasswordHash`
    - 평문 비밀번호를 해시화하여 PasswordHash 생성

* `matches(RawPassword, PasswordEncoder): boolean`
    - 평문 비밀번호와 해시 비교

* `toString(): String`
    - 보안을 위해 실제 값 출력 안 함

#### 규칙

* hash는 blank 불가
* hash 값은 외부에서 직접 접근 불가 (private)
* protected 생성자로 직접 생성 방지

---

### 3.1.9 평문 비밀번호 (RawPassword)

**_Value Object (일반 클래스)_**

#### 속성

* `value: String` - 평문 비밀번호

#### 규칙

* blank 불가
* 길이: 8-20자
* 소문자 1개 이상 포함
* 대문자 1개 이상 포함
* 숫자 1개 이상 포함
* 특수문자 1개 이상 포함
* 허용 특수문자: `!@#$%^&*`

#### 상수

* `ALLOWED_SPECIAL_CHARS = "!@#$%^&*"`

#### 주의사항

* 영속화되지 않음 (Embeddable 아님)
* 입력 검증용 Value Object
* PasswordEncoder를 통해 PasswordHash로 변환

---

### 3.1.10 프로필 주소 (ProfileAddress)

**_Value Object (Embeddable)_**

#### 속성

* `address: String` - 프로필 URL 경로
    - `length = 15`

#### 행위

* `validate(): void` (private, init 블록에서 호출)
    - address가 blank가 아닐 때만 검증
    - 정규식: `^[a-zA-Z0-9가-힣]+$`

#### 규칙

* 길이: 3-15자
* 영문, 숫자, 한글만 허용
* blank 허용 (선택 속성)
* 불변 객체 (data class)

---

### 3.1.11 자기 소개 (Introduction)

**_Value Object (Embeddable)_**

#### 속성

* `value: String` - 자기 소개 텍스트
    - 기본값: 빈 문자열
    - `length = 500`

#### 행위

* `validate(): void` (private, init 블록에서 호출)
    - 최대 길이 검증

#### 규칙

* 최대 길이: 500자
* 기본값 제공 (빈 문자열)
* 불변 객체 (data class)

---

### 3.1.12 비밀번호 인코더 (PasswordEncoder)

**_인터페이스_**

#### 행위

* `encode(RawPassword): String`
    - 평문 비밀번호를 해시로 인코딩

* `matches(RawPassword, String): boolean`
    - 평문 비밀번호와 해시 비교

#### 구현체

* BCryptPasswordEncoder (Spring Security)

---

## 3.2 활성화 토큰 애그리거트

### 3.2.1 활성화 토큰 (ActivationToken)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `memberId: Long` - 회원 식별자 (unique, nullable = false)
* `token: String` - 활성화 토큰 값
* `createdAt: LocalDateTime` - 생성 일시
* `expiresAt: LocalDateTime` - 만료 일시

#### 행위

* `isExpired(): boolean`
    - 토큰 만료 여부 확인
    - 현재 시간이 expiresAt 이후인지 검사

#### 용도

* 이메일 인증용 토큰
* Member와 별도 애그리거트로 관리
* 토큰 검증 후 Member.activate() 호출

---

## 3.3 BaseEntity

### 속성

* `id: Long` - 모든 Entity의 공통 식별자 (PK)
* `createdAt: LocalDateTime` - 생성 일시 (자동 설정)
* `lastModifiedAt: LocalDateTime` - 수정 일시 (자동 갱신)

### 규칙

* 모든 Entity는 BaseEntity를 상속
* Auditing 기능으로 생성/수정 일시 자동 관리

---

## 3.4 연관관계 정리

### Member ↔ MemberDetail

* 관계: 1:1
* 소유: Member가 소유
* Cascade: ALL
* OrphanRemoval: true
* 설명: Member 생명주기에 완전히 종속

### Member ↔ TrustScore

* 관계: 1:1
* 소유: Member가 소유
* Cascade: ALL
* OrphanRemoval: true
* 설명: Member 생명주기에 완전히 종속

### Member ↔ ActivationToken

* 관계: 논리적 연관만 존재
* 물리적 FK 없음
* memberId로 조회
* 별도 애그리거트

---

## 3.5 영속성 매핑 전략

### Entity

* Member, MemberDetail, TrustScore, ActivationToken
* 각각 독립된 테이블로 매핑
* BaseEntity 상속으로 공통 필드 관리

### Embeddable (Value Object)

* Email, Nickname, PasswordHash, ProfileAddress, Introduction
* 소유 Entity의 테이블에 컬럼으로 포함
* @Embedded 어노테이션 사용

### 일반 클래스 (Value Object)

* RawPassword
* 영속화되지 않음
* 입력 검증 및 변환용도로만 사용

### Enum

* MemberStatus, TrustLevel
* @Enumerated(EnumType.STRING)으로 매핑

---

## 3.6 설계 특징

### Aggregate 설계

* **Member Aggregate**: Member, MemberDetail, TrustScore
    - 강한 일관성 보장
    - 트랜잭션 경계

* **ActivationToken Aggregate**: ActivationToken
    - 독립적 생명주기
    - 별도 트랜잭션 처리 가능

### Value Object 활용

* 도메인 개념을 명확히 표현
* 유효성 검증을 생성 시점에 수행
* 불변성으로 안전성 확보

### Entity vs Value Object

* **Entity로 분리한 이유**:
    - MemberDetail: 독립적인 생명주기 이벤트 (등록/활성화/탈퇴 일시)
    - TrustScore: 복잡한 비즈니스 로직과 상태 변화

* **Embeddable로 유지한 이유**:
    - Email, Nickname 등: 단순 값 객체로 Member의 일부

### Kotlin 특성 활용

* data class로 Value Object 간결하게 표현
* init 블록으로 생성 시점 검증
* require()로 제약조건 명시
* protected constructor로 생성 제어
* companion object로 정적 팩토리 메서드
