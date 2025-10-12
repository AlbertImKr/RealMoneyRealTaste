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
      - [3.1.2 회원 상세 (MemberDetail)](#312-회원-상세-memberdetail)
      - [3.1.3 회원 상태 (MemberStatus)](#313-회원-상태-memberstatus)
      - [3.1.4 신뢰도 점수 (TrustScore)](#314-신뢰도-점수-trustscore)
      - [3.1.5 신뢰도 레벨 (TrustLevel)](#315-신뢰도-레벨-trustlevel)
      - [3.1.6 역할 (Role)](#316-역할-role)
      - [3.1.7 역할 목록 (Roles)](#317-역할-목록-roles)
      - [3.1.8 이메일 (Email)](#318-이메일-email)
      - [3.1.9 닉네임 (Nickname)](#319-닉네임-nickname)
      - [3.1.10 비밀번호 해시 (PasswordHash)](#3110-비밀번호-해시-passwordhash)
      - [3.1.11 평문 비밀번호 (RawPassword)](#3111-평문-비밀번호-rawpassword)
      - [3.1.12 프로필 주소 (ProfileAddress)](#3112-프로필-주소-profileaddress)
      - [3.1.13 자기 소개 (Introduction)](#3113-자기-소개-introduction)
      - [3.1.14 비밀번호 인코더 (PasswordEncoder)](#3114-비밀번호-인코더-passwordencoder)
    - [3.2 활성화 토큰 애그리거트 (Activation Token Aggregate)](#32-활성화-토큰-애그리거트-activation-token-aggregate)
        - [3.2.1 활성화 토큰 (ActivationToken)](#321-활성화-토큰-activationtoken)
    - [3.3 비밀번호 재설정 토큰 애그리거트 (Password Reset Token Aggregate)](#33-비밀번호-재설정-토큰-애그리거트-password-reset-token-aggregate)
        - [3.3.1 비밀번호 재설정 토큰 (PasswordResetToken)](#331-비밀번호-재설정-토큰-passwordresettoken)
    - [3.4 BaseEntity](#34-baseentity)
    - [3.5 연관관계 정리](#35-연관관계-정리)
    - [3.6 영속성 전략](#36-영속성-전략)
    - [3.7 설계 특징](#37-설계-특징)
    - [3.8 애플리케이션 레이어](#38-애플리케이션-레이어)
    - [3.9 아키텍처 패턴](#39-아키텍처-패턴)
    - [3.10 테스트 전략](#310-테스트-전략)
    - [3.11 확장 고려사항](#311-확장-고려사항)

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


- 회원 가입
    - 등록 신청 시 이메일, 닉네임, 비밀번호를 입력한다.
        - **이메일**: 유효한 형식이어야 하며 중복될 수 없다.
        - **닉네임**: 한글, 영문, 숫자로 구성된 2-20자 이내의 중복되지 않은 값이어야 한다.
        - **비밀번호**: 8-20자이며, 소문자, 대문자, 숫자, 특수문자(!@#$%^&*)를 각각 1개 이상 포함해야 한다.


- 이메일 인증
    - 등록 신청 후 이메일 인증을 완료하면 등록이 완료되며 상태가 PENDING에서 ACTIVE로 변경된다.
        - 활성화 토큰은 UUID로 생성되며 설정된 만료 시간이 있다.
        - 토큰은 사용 후 즉시 삭제된다.
        - 인증 이메일을 재발송할 수 있으며 이 경우 기존 토큰은 무효화되고 새 토큰이 생성된다.


- 프로필 관리
    - 등록을 완료한 회원(ACTIVE 상태)만 프로필 주소와 자기소개를 등록하거나 수정할 수 있다.
        - **프로필 주소**: 영문, 숫자, 한글로 구성된 3-15자 이내의 중복되지 않은 선택 항목이다.
            - 고유한 URL 경로로 사용된다.
        - **자기소개**: 최대 500자까지 입력 가능한 선택 항목이다.


- 시간 정보 관리
    - 등록 신청 시간
    - 등록 완료 시간
    - 탈퇴 시간


- 비밀번호 관리
    - 회원은 등록 완료 후 비밀번호를 변경할 수 있다.
        - 현재 비밀번호를 확인한 후 새 비밀번호로 변경한다.
        - 등록을 완료한(ACTIVE 상태) 상태에서만 비밀번호 변경이 가능하다.
    - 비밀번호를 분실한 경우 재설정할 수 있다.
        - 이메일로 비밀번호 재설정 토큰을 발송한다.
        - 토큰을 통해 새 비밀번호를 설정한다.
        - 보안을 위해 회원 존재 여부를 노출하지 않는다.


- 계정 탈퇴
    - 회원은 계정을 탈퇴할 수 있으며 탈퇴 시 상태가 탈퇴(DEACTIVATED)로 변경된다.
        - 등록을 완료한(ACTIVE 상태) 상태에서만 탈퇴가 가능하다.
        - 탈퇴한 회원의 정보는 수정할 수 없다.
        - 탈퇴 시간이 기록된다.


- 역할 관리
    - 회원은 USER, MANAGER, ADMIN 중 하나 이상의 역할을 가진다.
        - **USER**: 모든 회원의 기본 역할
            - 리뷰 작성 및 소셜 기능 사용 가능
        - **MANAGER**: 콘텐츠 관리 권한
            - USER 권한 포함
            - 콘텐츠 관리 기능 사용 가능
        - **ADMIN**: 최고 관리자 권한
            - USER 권한 포함
            - 시스템 전체 관리 기능 사용 가능
        - 회원은 최소 1개 이상의 역할을 유지해야 한다.
        - ACTIVE 상태에서만 역할 부여 및 회수가 가능하다.


- 회원 상태
    - **PENDING**: 등록 대기 (이메일 인증 전)
        - 이메일 인증 대기 중
        - 리뷰 작성 및 정보 수정 불가
    - **ACTIVE**: 등록 완료 (정상 활동 가능)
        - 모든 기능 사용 가능
        - 정보 수정 및 비밀번호 변경 가능
    - **DEACTIVATED**: 탈퇴
        - 모든 기능 사용 불가
        - 정보 수정 불가


- 신뢰도 시스템
    - 모든 회원은 신뢰도 점수를 가진다.
        - 초기 점수: 0점, 레벨: BRONZE
        - 점수 범위: 0-1000
        - 레벨: BRONZE(0-199), SILVER(200-499), GOLD(500-799), DIAMOND(800-1000)
        - 내돈내산 리뷰 작성 시 +5점
        - 광고성 리뷰 작성 시 +1점
        - 도움됨 평가 받을 시 +2점
        - 규칙 위반 시 -20점

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
* `roles: Roles` - 역할 목록 (Embedded)
* `updatedAt: LocalDateTime` - 마지막 수정 일시

#### 행위

* `static register(Email, Nickname, PasswordHash): Member`
    - 일반 회원 등록
    - 초기 상태: PENDING
    - 기본 역할: USER
    - MemberDetail과 TrustScore를 초기 상태로 생성

* `static registerManager(Email, Nickname, PasswordHash): Member`
    - 매니저 권한을 가진 회원 등록
    - 초기 역할: USER, MANAGER

* `static registerAdmin(Email, Nickname, PasswordHash): Member`
    - 관리자 권한을 가진 회원 등록
    - 초기 역할: USER, ADMIN

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
    - 비밀번호 직접 변경 (해시값으로)
    - updatedAt 갱신

* `changePassword(RawPassword, RawPassword, PasswordEncoder): void`
    - 비밀번호 변경 (현재 비밀번호 검증 포함)
    - ACTIVE 상태에서만 가능
    - 현재 비밀번호 일치 여부 검증
    - 새 비밀번호로 해시 생성 및 저장
    - updatedAt 갱신

* `updateInfo(Nickname?, ProfileAddress?, Introduction?): void`
    - 회원 정보 수정
    - ACTIVE 상태에서만 가능
    - null이 아닌 값만 업데이트
    - updatedAt 갱신

* `updateTrustScore(TrustScore): void`
    - 신뢰도 점수 업데이트
    - 새로운 TrustScore로 교체

* `grantRole(Role): void`
    - 역할 부여
    - ACTIVE 상태에서만 가능
    - updatedAt 갱신

* `revokeRole(Role): void`
    - 역할 회수
    - ACTIVE 상태에서만 가능
    - updatedAt 갱신

* `canWriteReview(): boolean`
    - 리뷰 작성 권한 확인
    - ACTIVE 상태일 때만 true

* `canManage(): boolean`
    - 관리 권한 확인
    - ACTIVE 상태이면서 MANAGER 역할 보유 시 true

* `canAdministrate(): boolean`
    - 관리자 권한 확인
    - ACTIVE 상태이면서 ADMIN 역할 보유 시 true

* `hasRole(Role): boolean`
    - 특정 역할 보유 여부 확인

* `hasAnyRole(vararg Role): boolean`
    - 여러 역할 중 하나라도 보유 여부 확인

#### 비즈니스 규칙

* 회원 생성 후 초기 상태는 PENDING
* PENDING 상태에서만 activate() 가능
* ACTIVE 상태에서만 deactivate() 가능
* ACTIVE 상태에서만 정보 수정, 비밀번호 변경, 역할 관리 가능
* 닉네임은 중복 불가 (애플리케이션 레이어에서 검증)
* 이메일은 중복 불가 (DB 제약조건)
* 모든 회원은 최소 USER 역할 보유

#### 인덱스

* `idx_member_email`: email 컬럼
* `idx_member_nickname`: nickname 컬럼
* `idx_member_status`: status 컬럼

---

### 3.1.2 회원 상세 (MemberDetail)

**_Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `profileAddress: ProfileAddress?` - 프로필 주소 (Embedded, nullable, unique)
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
  - null이 아닌 값만 업데이트

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
  - 최대값 1000으로 제한
      - 레벨 재계산

* `addAdReview(): void`
    - 광고성 리뷰 추가
    - 점수 +1, 리뷰 수 +1
  - 최대값 1000으로 제한
      - 레벨 재계산

* `penalize(int): void`
    - 점수 감소
    - 최소값 0으로 제한
    - 레벨 재계산

* `getRealMoneyRatio(): double`
    - 내돈내산 리뷰 비율 계산
  - 전체 리뷰가 0인 경우 0.0 반환
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
* 점수 감소 시 최소값 0으로 제한

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
  - 범위 밖이면 InvalidTrustScoreException 발생

---

### 3.1.6 역할 (Role)

**_Enum_**

#### 상수

* `USER` - 일반 사용자
* `MANAGER` - 관리자 (매니저)
* `ADMIN` - 최고 관리자

---

### 3.1.7 역할 목록 (Roles)

**_Value Object (Embeddable)_**

#### 속성

* `values: MutableSet<Role>` - 역할 목록
    - `@ElementCollection(fetch = FetchType.EAGER)`
    - `@CollectionTable(name = "member_roles")`

#### 행위

* `static ofUser(): Roles`
    - USER 역할만 가진 Roles 생성

* `static of(vararg Role): Roles`
    - 지정된 역할들로 Roles 생성
    - 빈 배열인 경우 RoleValidationException 발생

* `getRoles(): Set<Role>`
    - 역할 목록 조회 (불변 Set 반환)

* `hasRole(Role): boolean`
    - 특정 역할 보유 여부 확인

* `hasAnyRole(vararg Role): boolean`
    - 여러 역할 중 하나라도 보유 여부 확인

* `addRole(Role): void`
    - 역할 추가

* `removeRole(Role): void`
    - 역할 제거
    - USER 역할이 마지막 역할인 경우 제거 불가

* `isAdmin(): boolean`
    - ADMIN 역할 보유 여부

* `isManager(): boolean`
    - MANAGER 역할 보유 여부

#### 규칙

* 최소 1개 이상의 역할 보유 필수
* USER 역할이 마지막 역할인 경우 제거 불가
* EAGER 로딩으로 역할 정보 즉시 조회

---

### 3.1.8 이메일 (Email)

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

### 3.1.9 닉네임 (Nickname)

**_Value Object (Embeddable)_**

#### 속성

* `value: String` - 닉네임
    - `nullable = false, length = 20`

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

### 3.1.10 비밀번호 해시 (PasswordHash)

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

### 3.1.11 평문 비밀번호 (RawPassword)

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

### 3.1.12 프로필 주소 (ProfileAddress)

**_Value Object (Embeddable)_**

#### 속성

* `address: String` - 프로필 URL 경로
    - `length = 15, unique = true`

#### 행위

* `validate(): void` (private, init 블록에서 호출)
    - address가 blank가 아닐 때만 검증
    - 정규식: `^[a-zA-Z0-9가-힣]+$`

#### 규칙

* 길이: 3-15자
* 영문, 숫자, 한글만 허용
* blank 허용 (선택 속성)
* 중복 불가 (unique)
* 불변 객체 (data class)

---

### 3.1.13 자기 소개 (Introduction)

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

### 3.1.14 비밀번호 인코더 (PasswordEncoder)

**_인터페이스_**

#### 행위

* `encode(RawPassword): String`
    - 평문 비밀번호를 해시로 인코딩

* `matches(RawPassword, String): boolean`
    - 평문 비밀번호와 해시 비교

#### 구현체

* SecurityPasswordEncoder (BCryptPasswordEncoder 사용)

---

## 3.2 활성화 토큰 애그리거트 (Activation Token Aggregate)

### 3.2.1 활성화 토큰 (ActivationToken)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `memberId: Long` - 회원 식별자 (unique, nullable = false)
* `token: String` - 활성화 토큰 값 (nullable = false)
* `createdAt: LocalDateTime` - 생성 일시
* `expiresAt: LocalDateTime` - 만료 일시 (nullable = false)

#### 행위

* `isExpired(): boolean`
    - 토큰 만료 여부 확인
    - 현재 시간이 expiresAt 이후인지 검사

#### 용도

* 이메일 인증용 토큰
* Member와 별도 애그리거트로 관리
* 토큰 검증 후 Member.activate() 호출
* 회원당 하나의 토큰만 존재 (unique 제약)

#### 생명주기

* 회원 등록 시 생성
* 이메일 인증 완료 또는 만료 시 삭제
* 재발송 시 기존 토큰 삭제 후 신규 생성

---

## 3.3 비밀번호 재설정 토큰 애그리거트 (Password Reset Token Aggregate)

### 3.3.1 비밀번호 재설정 토큰 (PasswordResetToken)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 식별자 (PK, BaseEntity에서 상속)
* `memberId: Long` - 회원 식별자 (nullable = false)
* `token: String` - 재설정 토큰 값 (unique, nullable = false)
* `createdAt: LocalDateTime` - 생성 일시 (nullable = false)
* `expiresAt: LocalDateTime` - 만료 일시 (nullable = false)

#### 행위

* `isExpired(): boolean`
    - 토큰 만료 여부 확인
    - 현재 시간이 expiresAt 이후인지 검사

#### 용도

* 비밀번호 찾기/재설정용 토큰
* Member와 별도 애그리거트로 관리
* 토큰 검증 후 Member.changePassword() 호출
* 회원당 여러 토큰 존재 가능 (재발송 시)

#### 인덱스

* `idx_password_reset_token`: token 컬럼
* `idx_password_reset_member_id`: member_id 컬럼

#### 생명주기

* 비밀번호 재설정 요청 시 생성
* 비밀번호 재설정 완료 또는 만료 시 삭제
* 재요청 시 기존 토큰 삭제 후 신규 생성

---

## 3.4 BaseEntity

### 속성

* `id: Long` - 모든 Entity의 공통 식별자 (PK)
    - `@GeneratedValue(strategy = GenerationType.IDENTITY)`

### 행위

* `requireId(): Long`
    - 영속화된 엔티티의 ID 반환
    - ID가 null인 경우 IllegalStateException 발생

* `equals(Any?): boolean`
    - Hibernate Proxy 처리를 고려한 동등성 비교
    - ID 기반 비교

* `hashCode(): int`
    - Hibernate Proxy 처리를 고려한 해시코드
    - 엔티티 클래스 기반

* `toString(): String`
    - 엔티티 클래스명과 ID 출력

### 규칙

* 모든 Entity는 BaseEntity를 상속
* Serializable 구현
* Hibernate Proxy 안전한 equals/hashCode 구현

---

## 3.5 연관관계 정리

### Member ↔ MemberDetail

* 관계: 1:1 (양방향)
* 소유: Member가 소유
* Cascade: ALL
* OrphanRemoval: true
* 조인: `detail_id` 컬럼으로 연결 (unique)
* 설명: Member 생명주기에 완전히 종속

### Member ↔ TrustScore

* 관계: 1:1 (단방향)
* 소유: Member가 소유
* Cascade: ALL
* OrphanRemoval: true
* 설명: Member 생명주기에 완전히 종속

### Member ↔ Roles

* 관계: 1:N (임베디드 컬렉션)
* 매핑: `member_roles` 테이블
* Fetch: EAGER
* 설명: Member의 일부로 관리되는 역할 컬렉션

### Member ↔ ActivationToken

* 관계: 논리적 연관만 존재 (별도 애그리거트)
* 물리적 FK 없음
* memberId로 조회
* 일대일 제약: unique 제약으로 회원당 하나의 토큰만 존재

### Member ↔ PasswordResetToken

* 관계: 논리적 연관만 존재 (별도 애그리거트)
* 물리적 FK 없음
* memberId로 조회
* 일대다 관계: 회원당 여러 토큰 가능 (재요청 시)

---

## 3.6 영속성 매핑 전략

### Entity

* **Member**: `members` 테이블
* **MemberDetail**: `member_detail` 테이블
* **TrustScore**: `trust_score` 테이블
* **ActivationToken**: `activation_tokens` 테이블
* **PasswordResetToken**: `password_reset_tokens` 테이블
* 각각 독립된 테이블로 매핑
* BaseEntity 상속으로 공통 필드 관리

### Embeddable (Value Object)

* **Email**: `email` 컬럼
* **Nickname**: `nickname` 컬럼
* **PasswordHash**: `password_hash` 컬럼
* **ProfileAddress**: `profile_address` 컬럼
* **Introduction**: `introduction` 컬럼
* 소유 Entity의 테이블에 컬럼으로 포함
* @Embedded 어노테이션 사용

### ElementCollection

* **Roles**: `member_roles` 테이블
    - `member_id` (FK)
    - `role` (Enum String)
* @ElementCollection으로 별도 테이블 매핑
* EAGER 페치로 즉시 로딩

### 일반 클래스 (Value Object)

* **RawPassword**: 영속화되지 않음
* 입력 검증 및 변환용도로만 사용
* 메모리에서만 존재

### Enum

* **MemberStatus**, **TrustLevel**, **Role**
* @Enumerated(EnumType.STRING)으로 매핑
* 문자열로 저장하여 가독성 향상

### 인덱스 전략

#### members 테이블

* `idx_member_email`: email 컬럼 (조회 성능)
* `idx_member_nickname`: nickname 컬럼 (조회 성능)
* `idx_member_status`: status 컬럼 (상태별 조회)

#### activation_tokens 테이블

* 인덱스 없음 (토큰과 memberId에 unique 제약)

#### password_reset_tokens 테이블

* `idx_password_reset_token`: token 컬럼 (토큰 조회)
* `idx_password_reset_member_id`: member_id 컬럼 (회원별 조회)

---

## 3.7 설계 특징

### Aggregate 설계

#### Member Aggregate

* **구성**: Member, MemberDetail, TrustScore, Roles
* **특징**:
    - 강한 일관성 보장
    - 트랜잭션 경계
  - Member를 통해서만 접근
  - 생명주기 일치

#### ActivationToken Aggregate

* **구성**: ActivationToken
* **특징**:
    - 독립적 생명주기
  - Member와 논리적 연관
      - 별도 트랜잭션 처리 가능
  - 일시적 데이터 (인증 후 삭제)

#### PasswordResetToken Aggregate

* **구성**: PasswordResetToken
* **특징**:
    - 독립적 생명주기
    - Member와 논리적 연관
    - 별도 트랜잭션 처리 가능
    - 일시적 데이터 (재설정 후 삭제)

### Value Object 활용

#### 도메인 개념 명확화

* Email, Nickname, PasswordHash 등으로 의미 전달
* 단순 String이 아닌 타입으로 도메인 표현

#### 불변성 보장

* data class 활용
* 생성 후 변경 불가
* 스레드 안전성

#### 생성 시점 검증

* init 블록에서 유효성 검증
* 잘못된 객체 생성 방지
* 런타임 오류 조기 발견

### Entity vs Value Object 분리 기준

#### Entity로 분리한 이유

**MemberDetail**

* 독립적인 생명주기 이벤트 존재
* 등록/활성화/탈퇴 일시 추적 필요
* nullable 필드가 많아 상태 변화 존재

**TrustScore**

* 복잡한 비즈니스 로직 포함
* 점수 계산 및 레벨 관리
* 독립적인 상태 변화

#### Embeddable로 유지한 이유

**Email, Nickname 등**

* 단순 값 객체
* Member의 필수 속성
* 변경이 드물거나 단순 교체
* 별도 생명주기 불필요

#### ElementCollection 사용 이유

**Roles**

* 다중 값 속성
* Member의 일부지만 컬렉션
* EAGER 로딩으로 즉시 필요
* 독립적 엔티티로 만들기엔 과도

### Kotlin 특성 활용

#### data class

* Value Object 간결하게 표현
* equals/hashCode 자동 생성
* copy 메서드 제공

#### init 블록

* 생성 시점 검증
* 불변 객체 생성 전 유효성 확인

#### require()

* 제약조건 명시적 표현
* 가독성 향상
* 예외 메시지 일관성

#### protected constructor

* 직접 생성 제어
* 팩토리 메서드 강제

#### companion object

* 정적 팩토리 메서드
* 도메인 의미 전달하는 메서드명

#### null safety

* nullable 타입 명시
* 안전한 null 처리
* NPE 방지

### 도메인 규칙 강제

#### 생성 제약

* register() 팩토리 메서드로만 생성
* 초기 상태 보장
* 필수 값 누락 방지

#### 상태 전이 제약

* activate(): PENDING → ACTIVE만 허용
* deactivate(): ACTIVE → DEACTIVATED만 허용
* 잘못된 상태 전이 시 예외 발생

#### 권한 기반 작업

* ACTIVE 상태에서만 정보 수정 가능
* 역할 기반 기능 제한
* canWriteReview(), canManage(), canAdministrate()

### 예외 처리 전략

#### 도메인 예외

* ValueObjectValidationException: Value Object 검증 실패
* InvalidMemberStatusException: 잘못된 상태 전이
* InvalidPasswordException: 비밀번호 불일치
* UnauthorizedRoleOperationException: 권한 부족

#### 예외 계층

```
RuntimeException
├─ ValueObjectValidationException
│  ├─ EmailValidationException
│  ├─ NicknameValidationException
│  ├─ PasswordValidationException
│  ├─ ProfileAddressValidationException
│  ├─ IntroductionValidationException
│  ├─ RoleValidationException
│  └─ InvalidTrustScoreException
└─ MemberDomainException
   ├─ InvalidMemberStatusException
   ├─ InvalidPasswordException
   └─ UnauthorizedRoleOperationException
```

#### 예외 처리 원칙

* 도메인 규칙 위반 시 명확한 예외
* Sealed class로 예외 유형 제한
* 의미 있는 메시지 제공

---

## 3.8 애플리케이션 레이어

### 3.8.1 서비스 계층 구조

#### Provided 인터페이스 (Use Case)

애플리케이션이 외부(Adapter)에 제공하는 기능

**회원 관리**

* `MemberRegister`: 회원 등록
* `MemberActivate`: 회원 활성화 및 재인증
* `MemberReader`: 회원 조회
* `MemberUpdater`: 회원 정보 수정
* `MemberVerify`: 회원 인증 (로그인)

**비밀번호 관리**

* `PasswordResetter`: 비밀번호 재설정

**토큰 관리**

* `ActivationTokenGenerator`: 활성화 토큰 생성
* `ActivationTokenReader`: 활성화 토큰 조회
* `ActivationTokenDeleter`: 활성화 토큰 삭제
* `PasswordResetTokenGenerator`: 비밀번호 재설정 토큰 생성
* `PasswordResetTokenReader`: 비밀번호 재설정 토큰 조회
* `PasswordResetTokenDeleter`: 비밀번호 재설정 토큰 삭제

**이메일 관리**

* `MemberActivationEmailSender`: 활성화 이메일 발송
* `MemberPasswordResetEmailSender`: 비밀번호 재설정 이메일 발송

#### Required 인터페이스 (Dependency)

애플리케이션이 외부(Infrastructure)에 요구하는 기능

**리포지토리**

* `MemberRepository`: 회원 영속성
* `ActivationTokenRepository`: 활성화 토큰 영속성
* `PasswordResetTokenRepository`: 비밀번호 재설정 토큰 영속성

**외부 서비스**

* `EmailSender`: 이메일 전송
* `EmailTemplate`: 이메일 템플릿 생성

### 3.8.2 서비스 구현

#### 회원 등록 서비스 (MemberRegistrationService)

**역할**

* 회원 등록 처리
* 이메일 중복 검증
* 회원 등록 이벤트 발행

**흐름**

1. 이메일 중복 검증
2. 비밀번호 해시화
3. Member 생성 및 저장
4. ActivationToken 생성
5. MemberRegisteredEvent 발행

#### 회원 활성화 서비스 (MemberActivationService)

**역할**

* 이메일 인증 처리
* 인증 이메일 재발송

**활성화 흐름**

1. 토큰 조회 및 만료 검증
2. 회원 조회
3. 회원 활성화 (Member.activate())
4. 토큰 삭제

**재발송 흐름**

1. 회원 조회 및 활성화 상태 검증
2. 기존 토큰 삭제 및 새 토큰 생성
3. ResendActivationEmailEvent 발행

#### 비밀번호 재설정 서비스 (PasswordResetService)

**역할**

* 비밀번호 재설정 이메일 발송
* 비밀번호 재설정 처리

**재설정 이메일 발송 흐름**

1. 회원 존재 여부 확인 (없으면 조용히 무시)
2. 기존 토큰 삭제
3. 새 토큰 생성
4. PasswordResetRequestedEvent 발행

**비밀번호 재설정 흐름**

1. 토큰 조회 및 만료 검증
2. 회원 조회
3. 비밀번호 변경 (Member.changePassword())
4. 토큰 삭제

### 3.8.3 이벤트 기반 처리

#### 도메인 이벤트

**MemberRegisteredEvent**

* 발행 시점: 회원 등록 완료
* 처리: 활성화 이메일 발송

**ResendActivationEmailEvent**

* 발행 시점: 인증 이메일 재발송 요청
* 처리: 활성화 이메일 재발송

**PasswordResetRequestedEvent**

* 발행 시점: 비밀번호 재설정 요청
* 처리: 비밀번호 재설정 이메일 발송

#### 이벤트 리스너 (MemberEventListener)

**특징**

* @Async 비동기 처리
* 이메일 발송 실패가 메인 로직에 영향 없음
* 트랜잭션 분리

### 3.8.4 토큰 생성 전략

#### UUID 기반 토큰 생성

**UuidActivationTokenGenerator**

* UUID.randomUUID()로 토큰 생성
* 만료 시간 설정 (설정 파일에서 주입)
* 기존 토큰 자동 삭제 (unique 제약 준수)
* EntityManager.flush()로 즉시 반영

**UuidPasswordResetTokenGenerator**

* UUID.randomUUID()로 토큰 생성
* 만료 시간 설정 (설정 파일에서 주입)
* 여러 토큰 동시 존재 가능

### 3.8.5 DTO (Data Transfer Object)

#### 요청 DTO

**MemberRegisterRequest**

* email: Email
* password: RawPassword
* nickname: Nickname

**AccountUpdateRequest**

* nickname: Nickname?
* profileAddress: ProfileAddress?
* introduction: Introduction?

#### 특징

* Value Object 직접 사용
* 도메인 모델과 일관성 유지
* 생성 시점 검증 보장

### 3.8.6 트랜잭션 관리

#### 트랜잭션 경계

**@Transactional (Write)**

* MemberRegistrationService.register()
* MemberActivationService.activate()
* MemberActivationService.resendActivationEmail()
* MemberUpdateService.updateInfo()
* MemberUpdateService.updatePassword()
* MemberUpdateService.deactivate()
* PasswordResetService.sendPasswordResetEmail()
* PasswordResetService.resetPassword()
* 토큰 삭제/생성 서비스

**@Transactional(readOnly = true) (Read)**

* MemberReadService 전체
* ActivationTokenReadService.findByToken()
* PasswordResetTokenReadService 전체
* MemberVerifyService.verify()

#### 트랜잭션 전파

* 기본: REQUIRED
* 이벤트 리스너: 별도 트랜잭션 (@Async)
* 토큰 삭제 후 flush: 제약 조건 위반 방지

### 3.8.7 보안 고려사항

#### 비밀번호 관리

* 평문 저장 금지
* BCrypt 해싱 (SecurityPasswordEncoder)
* 비밀번호 검증 실패 시 일반적 메시지 (정보 노출 방지)

#### 토큰 관리

* UUID 사용으로 예측 불가
* 만료 시간 설정
* 사용 후 즉시 삭제
* 재발송 시 기존 토큰 무효화

#### 이메일 인증

* 이메일 소유권 검증
* PENDING 상태로 권한 제한
* 인증 완료 후에만 ACTIVE

#### 개인정보 보호

* 비밀번호 재설정 시 회원 존재 여부 노출 안 함
* 에러 메시지에 민감 정보 포함 안 함
* toString()에서 비밀번호 해시 숨김

---

## 3.9 아키텍처 패턴

### 3.9.1 헥사고날 아키텍처 (포트와 어댑터)

#### 계층 구조

```
┌─────────────────────────────────────────┐
│         Adapter (Web/View)              │  ← 외부 인터페이스
│  AuthView, MemberView, ExceptionHandler │
├─────────────────────────────────────────┤
│      Application (Use Case)             │  ← 애플리케이션 로직
│  Services, Events, DTOs                 │
│                                         │
│  Provided ←→ Required                   │  ← 포트
├─────────────────────────────────────────┤
│          Domain (Core)                  │  ← 핵심 도메인
│  Member, TrustScore, Value Objects      │
├─────────────────────────────────────────┤
│     Adapter (Infrastructure)            │  ← 외부 구현
│  JPA Repositories, Email Sender         │
└─────────────────────────────────────────┘
```

#### 의존성 방향

* 외부 → 내부 (단방향)
* Domain은 아무것도 의존하지 않음
* Application은 Domain만 의존
* Adapter는 Application과 Domain 의존

### 3.9.2 DDD (Domain-Driven Design) 적용

#### Aggregate 설계

* Member Aggregate (Member, MemberDetail, TrustScore, Roles)
* ActivationToken Aggregate
* PasswordResetToken Aggregate
* 각 Aggregate는 독립적 트랜잭션 경계

#### Entity와 Value Object 구분

* Entity: 식별자로 구별, 생명주기 존재
* Value Object: 값으로 구별, 불변

#### Repository 패턴

* Aggregate Root만 Repository 제공
* 컬렉션처럼 사용하는 인터페이스

#### Domain Event

* 도메인 중요 사건 기록
* 비동기 처리로 결합도 감소

### 3.9.3 Clean Architecture 원칙

#### 의존성 역전 (Dependency Inversion)

* 추상화에 의존, 구현에 의존하지 않음
* Required 인터페이스로 외부 의존성 추상화

#### 단일 책임 (Single Responsibility)

* 각 서비스는 하나의 Use Case
* Value Object는 하나의 개념

#### 개방-폐쇄 (Open-Closed)

* 새로운 기능 추가 시 기존 코드 수정 최소화
* 인터페이스를 통한 확장

---

## 3.10 테스트 전략

### 3.10.1 단위 테스트 (Unit Tests)

#### Domain Layer

* Value Object: 생성 검증, 제약조건, 예외 상황
* Entity: 비즈니스 로직, 상태 전이, 도메인 규칙
* BaseEntity: Hibernate Proxy 처리, equals/hashCode

### 3.10.2 통합 테스트 (Integration Tests)

#### Application Layer

* Service 테스트: 실제 Spring Context 및 DB 사용
* 트랜잭션 롤백으로 테스트 격리
* 실제 Bean 주입 (Mock 최소 사용)

#### Adapter Layer

* View Controller: MockMvc로 HTTP 요청/응답 검증
* 보안: @WithMockMember 커스텀 애노테이션
* Flash 속성, 리다이렉트, View 이름 검증

---

## 3.11 확장 고려사항

### 3.11.1 성능 최적화

#### 캐싱

* 회원 정보 캐싱 (Redis)
* 토큰 조회 캐싱
* 신뢰도 레벨 캐싱

#### 쿼리 최적화

* N+1 문제 방지 (EAGER 또는 JOIN FETCH)
* 인덱스 활용
* 페이징 처리

### 3.11.2 확장성

#### 수평 확장

* Stateless 서비스
* 세션 외부화 (Redis)
* DB 복제 (Read Replica)

#### 비동기 처리

* 이메일 발송 비동기화
* 이벤트 기반 아키텍처
* 메시지 큐 도입 가능

### 3.11.3 모니터링

#### 로깅

* 주요 비즈니스 이벤트 로깅
* 예외 상황 추적
* 성능 지표 수집

#### 메트릭

* 회원 가입률
* 활성화율
* 비밀번호 재설정 빈도
* API 응답 시간
