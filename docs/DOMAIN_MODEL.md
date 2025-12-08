# RMRT 도메인 모델 문서

> 본 문서는 RMRT의 DDD 기반 도메인 설계를 상세히 설명합니다.

## 목차

- [1. 도메인 설계 개요](#1-도메인-설계-개요)
- [2. 회원 애그리거트](#2-회원-애그리거트)
- [3. 게시글 애그리거트](#3-게시글-애그리거트)
- [4. 이미지 애그리거트](#4-이미지-애그리거트)
- [5. 컬렉션 애그리거트](#5-컬렉션-애그리거트)
- [6. 친구 관계 애그리거트](#6-친구-관계-애그리거트)
- [7. 팔로우 애그리거트](#7-팔로우-애그리거트)
- [8. 댓글 애그리거트](#8-댓글-애그리거트)
- [9. 토큰 애그리거트](#9-토큰-애그리거트)
- [10. 회원 이벤트 애그리거트](#10-회원-이벤트-애그리거트)
- [11. 도메인 이벤트 시스템](#11-도메인-이벤트-시스템)
- [12. 공통 설계 요소](#12-공통-설계-요소)
- [13. 애플리케이션 레이어](#13-애플리케이션-레이어)
- [14. 설계 특징 및 패턴](#14-설계-특징-및-패턴)

---

## 1. 도메인 설계 개요

### 1.1 DDD 적용

RMRT는 Domain-Driven Design(DDD) 원칙을 따라 설계되었습니다.

**핵심 원칙**

- Aggregate를 통한 트랜잭션 경계 설정
- Value Object로 도메인 개념 명확화
- Domain Event를 통한 느슨한 결합
- Repository를 통한 영속성 추상화

**Aggregate 구성**

```
Member Aggregate
├── Member (Root)
├── MemberDetail
├── TrustScore
└── Roles

Post Aggregate
├── Post (Root)
└── PostImages (ElementCollection)

Image Aggregate
├── Image (Root)
└── FileKey (Value Object)

PostCollection Aggregate
├── PostCollection (Root)
├── CollectionInfo
├── CollectionOwner
└── CollectionPosts

Friendship Aggregate
├── Friendship (Root)
├── FriendRelationship
└── FriendshipStatus

Follow Aggregate
├── Follow (Root)
├── FollowRelationship
└── FollowStatus

Comment Aggregate
├── Comment (Root)
├── CommentContent
├── CommentAuthor
└── CommentMention

PostHeart (독립 Entity, Post와 논리적 연관)

ActivationToken Aggregate
└── ActivationToken (Root)

PasswordResetToken Aggregate
└── PasswordResetToken (Root)

MemberEvent Aggregate
├── MemberEvent (Root)
├── MemberEventType (Enum)
└── 관련 엔티티 ID (Long)
```

---

## 2. 회원 애그리거트

### 2.1 회원 (Member)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 회원 식별자 (PK)
* `email: Email` - 이메일 (Embedded, Natural ID)
* `nickname: Nickname` - 닉네임 (Embedded)
* `passwordHash: PasswordHash` - 비밀번호 해시 (Embedded)
* `status: MemberStatus` - 회원 상태 (Enum)
* `detail: MemberDetail` - 회원 상세 정보 (1:1)
* `trustScore: TrustScore` - 신뢰도 점수 (1:1)
* `roles: Roles` - 역할 목록 (Embedded)
* `updatedAt: LocalDateTime` - 마지막 수정 일시

#### 주요 행위

**생성**

- `static register(Email, Nickname, PasswordHash): Member` - 일반 회원 등록
- `static registerManager(...)` - 매니저 권한 회원 등록
- `static registerAdmin(...)` - 관리자 권한 회원 등록

**상태 전이**

- `activate()` - PENDING → ACTIVE
- `deactivate()` - ACTIVE → DEACTIVATED

**정보 관리**

- `updateInfo(Nickname?, ProfileAddress?, Introduction?)` - 회원 정보 수정
- `changePassword(RawPassword, RawPassword, PasswordEncoder)` - 비밀번호 변경
- `updateTrustScore(TrustScore)` - 신뢰도 점수 업데이트

**역할 관리**

- `grantRole(Role)` - 역할 부여
- `revokeRole(Role)` - 역할 회수
- `hasRole(Role): Boolean` - 역할 확인

#### 비즈니스 규칙

* 초기 상태: PENDING
* PENDING → ACTIVE만 가능 (이메일 인증 완료)
* ACTIVE → DEACTIVATED만 가능 (탈퇴)
* ACTIVE 상태에서만 정보 수정, 역할 관리 가능
* 모든 회원은 최소 USER 역할 보유 필수

#### 인덱스

* `idx_member_email`: email
* `idx_member_nickname`: nickname
* `idx_member_status`: status

---

### 2.2 회원 상세 (MemberDetail)

**_Entity_**

#### 속성

* `profileAddress: ProfileAddress?` - 프로필 주소 (선택)
* `introduction: Introduction?` - 자기소개 (선택)
* `registeredAt: LocalDateTime` - 등록 일시
* `activatedAt: LocalDateTime?` - 활성화 일시
* `deactivatedAt: LocalDateTime?` - 탈퇴 일시

---

### 2.3 신뢰도 점수 (TrustScore)

**_Entity_**

#### 속성

* `score: int` - 신뢰도 점수 (0-1000)
* `level: TrustLevel` - 신뢰도 레벨
* `realMoneyReviewCount: int` - 내돈내산 리뷰 수
* `adReviewCount: int` - 광고성 리뷰 수

#### 계산 로직

**점수 가중치**

```
내돈내산 리뷰: +5
광고성 리뷰: +1
도움됨 투표: +2
위반 페널티: -20
```

**신뢰도 레벨**

- BRONZE: 0-199점
- SILVER: 200-499점
- GOLD: 500-799점
- DIAMOND: 800-1000점

---

### 2.4 Value Objects

#### Email

```kotlin
@Embeddable
data class Email(
    @NaturalId
    @Column(unique = true)
    val address: String
)
```

- 정규식: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*\.[A-Za-z]{2,}$`

#### Nickname

```kotlin
@Embeddable
data class Nickname(
    @Column(length = 20)
    val value: String
)
```

- 길이: 2-20자
- 정규식: `^[가-힣a-zA-Z0-9]+$`

#### RawPassword

```kotlin
class RawPassword(val value: String)
```

- 길이: 8-20자
- 소문자, 대문자, 숫자, 특수문자 각 1개 이상
- 허용 특수문자: `!@#$%^&*`

#### ProfileAddress

```kotlin
@Embeddable
data class ProfileAddress(
    @Column(length = 15, unique = true)
    val address: String
)
```

- 길이: 3-15자
- 정규식: `^[a-zA-Z0-9가-힣]+$`

#### Roles

```kotlin
@Embeddable
class Roles(
    @ElementCollection(fetch = FetchType.EAGER)
    private val values: MutableSet<Role>
)
```

- Role: USER, MANAGER, ADMIN
- 최소 1개 역할 필수

---

## 3. 게시글 애그리거트

### 3.1 게시글 (Post)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 게시글 식별자 (PK)
* `author: Author` - 작성자 정보 (Embedded)
* `restaurant: Restaurant` - 맛집 정보 (Embedded)
* `content: PostContent` - 내용 및 평점 (Embedded)
* `images: PostImages` - 이미지 목록 (Embedded)
* `status: PostStatus` - 게시글 상태
* `heartCount: Int` - 좋아요 수
* `viewCount: Int` - 조회 수
* `createdAt: LocalDateTime` - 작성 일시
* `updatedAt: LocalDateTime` - 수정 일시

#### 주요 행위

**생성**

```kotlin
static create (
    authorMemberId: Long,
authorNickname: String,
restaurant: Restaurant,
content: PostContent,
images: PostImages
): Post
```

**수정/삭제**

- `update(memberId, content, images, restaurant)` - 게시글 수정
- `delete(memberId)` - 게시글 삭제 (Soft Delete)

**권한 확인**

- `canEditBy(memberId): Boolean` - 수정 권한 확인
- `ensureCanEditBy(memberId)` - 권한 검증 (예외 발생)
- `ensurePublished()` - 공개 상태 검증

#### 비즈니스 규칙

* 초기 상태: PUBLISHED
* PUBLISHED 상태에서만 수정/삭제 가능
* 작성자만 수정/삭제 권한 보유
* Soft Delete: 상태만 DELETED로 변경
* 카운트는 이벤트를 통해 비동기 증감

#### 인덱스

* `idx_post_author_id`: author_member_id
* `idx_post_status`: status
* `idx_post_created_at`: created_at
* `idx_post_restaurant_name`: restaurant_name

---

### 3.2 게시글 좋아요 (PostHeart)

**_Entity_** (Post와 별도 관리)

#### 속성

* `postId: Long` - 게시글 ID
* `memberId: Long` - 회원 ID
* `createdAt: LocalDateTime` - 생성 일시

#### 규칙

* Unique 제약: (post_id, member_id)
* 게시글당 회원당 1개만 가능
* 물리적 삭제 (Soft Delete 아님)

---

### 3.3 Value Objects

#### Author

```kotlin
@Embeddable
data class Author(
    val memberId: Long,
    @Column(length = 20)
    val nickname: String
)
```

#### Restaurant

```kotlin
@Embeddable
data class Restaurant(
    @Column(length = 100)
    val name: String,
    @Column(length = 255)
    val address: String,
    val latitude: Double,   // -90.0 ~ 90.0
    val longitude: Double   // -180.0 ~ 180.0
)
```

#### PostContent

```kotlin
@Embeddable
data class PostContent(
    @Column(columnDefinition = "TEXT")
    val text: String,    // 최대 2000자
    val rating: Int      // 1-5
)
```

#### PostImages

```kotlin
@Embeddable
data class PostImages(
    @ElementCollection
    @CollectionTable(name = "post_images")
    @OrderColumn(name = "image_order")
    @Column(length = 500)
    val urls: List<String>  // 최대 5장
)
```

---

### 3.4 도메인 이벤트

#### 게시글 이벤트

```kotlin
// 게시글 생성
data class PostCreatedEvent(
    val postId: Long,
    val authorMemberId: Long,
    val restaurantName: String
)

// 게시글 삭제
data class PostDeletedEvent(
    val postId: Long,
    val authorMemberId: Long
)

// 게시글 조회 (작성자가 아닌 경우)
data class PostViewedEvent(
    val postId: Long,
    val viewerMemberId: Long,
    val authorMemberId: Long
)
```

#### 좋아요 이벤트

```kotlin
// 좋아요 추가
data class PostHeartAddedEvent(
    val postId: Long,
    val memberId: Long
)

// 좋아요 제거
data class PostHeartRemovedEvent(
    val postId: Long,
    val memberId: Long
)
```

---

## 4. 이미지 애그리거트

### 4.1 이미지 (Image)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 이미지 식별자 (PK)
* `fileKey: FileKey` - S3 파일 키 (Embedded, Unique)
* `uploadedBy: Long` - 업로드한 회원 ID
* `imageType: ImageType` - 이미지 타입 (Enum)
* `isDeleted: Boolean` - 삭제 여부 (Soft Delete)
* `createdAt: LocalDateTime` - 업로드 일시
* `updatedAt: LocalDateTime` - 수정 일시

#### 주요 행위

**생성**

```kotlin
static create (
    fileKey: FileKey,
uploadedBy: Long,
imageType: ImageType
): Image
```

**삭제**

- `delete()` - 이미지 삭제 (Soft Delete)
    - `isDeleted = true`로 설정
    - 실제 S3 파일은 별도 배치 작업으로 삭제

#### 비즈니스 규칙

* 초기 상태: isDeleted = false
* 이미 삭제된 이미지는 재삭제 불가
* 하루 업로드 제한: 회원당 50장
* 파일 크기 제한: 5MB
* 지원 타입: PROFILE, POST, COLLECTION

#### 인덱스

* `idx_image_uploaded_by`: uploaded_by
* `idx_image_type`: image_type
* `uidx_image_file_key`: file_key (UNIQUE)

---

### 4.2 Value Objects

#### FileKey

**경로 안전성 검증**

```kotlin
@Embeddable
data class FileKey(
    @Column(name = "file_key", unique = true, length = 512)
    val value: String
) {
    init {
        require(!value.contains("..")) { "경로 탐색 공격 방지" }
        require(!value.startsWith("/")) { "절대 경로 사용 불가" }
        require(value.length <= MAX_LENGTH) { "파일 키는 512자를 초과할 수 없습니다" }
    }

    companion object {
        const val MAX_LENGTH = 512
    }
}
```

**특징**

- 경로 탐색 공격 (`../`) 방지
- 절대 경로 사용 금지
- S3 객체 키 직접 매핑

#### ImageType

```kotlin
enum class ImageType {
    PROFILE,    // 프로필 이미지
    POST,       // 게시글 이미지
    COLLECTION  // 컬렉션 커버 이미지
}
```

---

### 4.3 도메인 이벤트

현재 이미지 관련 도메인 이벤트는 정의되지 않음 (향후 확장 가능)

---

## 5. 컬렉션 애그리거트

### 5.1 컬렉션 (PostCollection)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 컬렉션 식별자 (PK)
* `owner: CollectionOwner` - 소유자 정보 (Embedded)
* `info: CollectionInfo` - 컬렉션 정보 (Embedded)
* `posts: CollectionPosts` - 포스트 목록 (Embedded)
* `privacy: CollectionPrivacy` - 공개 여부 (Enum)
* `status: CollectionStatus` - 컬렉션 상태 (Enum)
* `createdAt: LocalDateTime` - 생성 일시
* `updatedAt: LocalDateTime` - 수정 일시

#### 주요 행위

**생성**

```kotlin
static create (
    owner: CollectionOwner,
info: CollectionInfo,
posts: CollectionPosts,
privacy: CollectionPrivacy
): PostCollection
```

**관리**

- `updateInfo(info: CollectionInfo)` - 컬렉션 정보 수정
- `addPost(postId: Long)` - 포스트 추가
- `removePost(postId: Long)` - 포스트 제거
- `changePrivacy(privacy: CollectionPrivacy)` - 공개 여부 변경
- `delete()` - 컬렉션 삭제 (Soft Delete)

#### 비즈니스 규칙

* 초기 상태: ACTIVE
* 소유자만 수정/삭제 권한 보유
* Soft Delete: 상태만 DELETED로 변경
* 공개/비공개 설정 가능

---

## 5. 친구 관계 애그리거트

### 5.1 친구 관계 (Friendship)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 친구 관계 식별자 (PK)
* `relationship: FriendRelationship` - 관계 정보 (Embedded)
* `status: FriendshipStatus` - 친구 상태 (Enum)
* `createdAt: LocalDateTime` - 요청 일시
* `acceptedAt: LocalDateTime?` - 수락 일시

#### 주요 행위

**생성 (feature36 개선)**

```kotlin
companion object {
    fun request(command: FriendRequestCommand): Friendship {
        // 친구 요청 시 양방향 닉네임 정보 저장
        val relationship = FriendRelationship(
            fromMemberId = command.fromMemberId,
            fromMemberNickname = command.fromMemberNickname,  // 추가
            toMemberId = command.toMemberId,
            toMemberNickname = command.toMemberNickname  // 추가
        )
        return Friendship(
            relationship = relationship,
            status = FriendshipStatus.PENDING
        )
    }
}
```

**상태 전이**

- `accept()` - PENDING → ACCEPTED
- `reject()` - PENDING → REJECTED
- `terminate()` - ACCEPTED → TERMINATED

#### 비즈니스 규칙

* 초기 상태: PENDING
* PENDING → ACCEPTED/REJECTED만 가능
* ACCEPTED → TERMINATED만 가능
* 양방향 관계 관리
* **데이터 완전성 확보 (feature36)**: 친구 요청 시 양방향 닉네임 저장으로 조회 성능 개선

#### 도메인 이벤트

```kotlin
// 친구 요청
data class FriendRequestSentEvent(
    val fromMemberId: Long,
    val toMemberId: Long
)

// 친구 수락
data class FriendRequestAcceptedEvent(
    val fromMemberId: Long,
    val toMemberId: Long
)

// 친구 관계 종료
data class FriendshipTerminatedEvent(
    val fromMemberId: Long,
    val toMemberId: Long
)
```

---

## 6. 팔로우 애그리거트

### 6.1 팔로우 (Follow)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 팔로우 식별자 (PK)
* `relationship: FollowRelationship` - 관계 정보 (Embedded)
* `status: FollowStatus` - 팔로우 상태 (Enum)
* `createdAt: LocalDateTime` - 팔로우 일시
* `terminatedAt: LocalDateTime?` - 팔로우 해제 일시

#### 주요 행위

**생성 (feature36 개선)**

```kotlin
companion object {
    fun start(command: FollowCreateCommand): Follow {
        // 팔로우 관계 생성 시 닉네임 정보 저장
        val relationship = FollowRelationship(
            followerId = command.followerId,
            followerNickname = command.followerNickname,  // 추가
            followingId = command.followingId,
            followingNickname = command.followingNickname  // 추가
        )
        return Follow(relationship = relationship, status = FollowStatus.ACTIVE)
    }
}
```

**관리**

- `terminate()` - ACTIVE → TERMINATED

#### 비즈니스 규칙

* 초기 상태: ACTIVE
* ACTIVE → TERMINATED만 가능
* 단방향 관계 (팔로워 → 팔로잉)
* 자기 자신 팔로우 불가
* **데이터 완전성 확보 (feature36)**: 팔로우 관계 생성 시 닉네임 저장으로 조회 성능 개선

#### 도메인 이벤트

```kotlin
// 팔로우 시작
data class FollowStartedEvent(
    val followerId: Long,
    val followingId: Long
)

// 팔로우 해제
data class UnfollowedEvent(
    val followerId: Long,
    val followingId: Long
)
```

---

## 7. 댓글 애그리거트

### 7.1 댓글 (Comment)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 댓글 식별자 (PK)
* `author: CommentAuthor` - 작성자 정보 (Embedded)
* `content: CommentContent` - 댓글 내용 (Embedded)
* `mention: CommentMention?` - 멘션 정보 (Embedded, 선택)
* `status: CommentStatus` - 댓글 상태 (Enum)
* `createdAt: LocalDateTime` - 생성 일시
* `updatedAt: LocalDateTime` - 수정 일시

#### 주요 행위

**생성**

```kotlin
static create (
    postId: Long,
author: CommentAuthor,
content: CommentContent,
mention: CommentMention?
): Comment
```

**관리**

- `update(content: CommentContent)` - 댓글 수정
- `delete()` - 댓글 삭제 (Soft Delete)

#### 비즈니스 규칙

* 초기 상태: ACTIVE
* 작성자만 수정/삭제 권한 보유
* Soft Delete: 상태만 DELETED로 변경
* 멘션 기능 지원

#### 도메인 이벤트

```kotlin
// 댓글 생성
data class CommentCreatedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long
)

// 댓글 수정
data class CommentUpdatedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long
)

// 댓글 삭제
data class CommentDeletedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long
)
```

---

## 8. 토큰 애그리거트

### 8.1 활성화 토큰 (ActivationToken)

**_Aggregate Root, Entity_**

```kotlin
class ActivationToken(
    val memberId: Long,      // unique
    val token: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
)
```

**용도**: 이메일 인증  
**생명주기**: 회원당 1개, 인증 완료 또는 만료 시 삭제

---

### 8.2 비밀번호 재설정 토큰 (PasswordResetToken)

**_Aggregate Root, Entity_**

```kotlin
class PasswordResetToken(
    val memberId: Long,
    val token: String,       // unique
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
)
```

**용도**: 비밀번호 찾기/재설정  
**생명주기**: 재설정 완료 또는 만료 시 삭제

**인덱스**

* `idx_password_reset_token`: token
* `idx_password_reset_member_id`: member_id

---

## 9. 회원 이벤트 애그리거트

### 9.1 회원 이벤트 (MemberEvent)

**_Aggregate Root, Entity_**

#### 속성

* `id: Long` - 이벤트 식별자 (PK)
* `memberId: Long` - 이벤트 대상 회원 ID
* `eventType: MemberEventType` - 이벤트 타입 (Enum)
* `title: String` - 이벤트 제목 (최대 100자)
* `message: String` - 이벤트 상세 메시지 (최대 500자)
* `relatedMemberId: Long?` - 관련 회원 ID (선택)
* `relatedPostId: Long?` - 관련 게시물 ID (선택)
* `relatedCommentId: Long?` - 관련 댓글 ID (선택)
* `isRead: Boolean` - 읽음 여부 (기본값: false)
* `createdAt: LocalDateTime` - 생성 일시

#### 주요 행위

**생성**

```kotlin
static create (
    memberId: Long,
eventType: MemberEventType,
title: String,
message: String,
relatedMemberId: Long? = null,
relatedPostId: Long? = null,
relatedCommentId: Long? = null
): MemberEvent
```

**관리**

- `markAsRead()` - 이벤트를 읽음으로 표시

#### 비즈니스 규칙

* 초기 읽음 상태: false
* 본인의 이벤트만 조회 및 읽음 처리 가능
* Hard Delete 방식 (90일 이상 경과한 읽은 이벤트 자동 삭제)
* 도메인 이벤트 발생 시 자동 생성

### 9.2 회원 이벤트 타입 (MemberEventType)

**_Enum_**

```kotlin
enum class MemberEventType {
    // 친구 관련
    FRIEND_REQUEST_SENT,      // 친구 요청을 보냈습니다
    FRIEND_REQUEST_RECEIVED,  // 친구 요청을 받았습니다
    FRIEND_REQUEST_ACCEPTED,  // 친구 요청이 수락되었습니다
    FRIEND_REQUEST_REJECTED,  // 친구 요청이 거절되었습니다
    FRIENDSHIP_TERMINATED,    // 친구 관계가 해제되었습니다

    // 게시물 관련
    POST_CREATED,             // 새 게시물을 작성했습니다
    POST_DELETED,             // 게시물을 삭제했습니다
    POST_COMMENTED,           // 게시물에 댓글이 달렸습니다

    // 댓글 관련
    COMMENT_CREATED,          // 댓글을 작성했습니다
    COMMENT_DELETED,          // 댓글을 삭제했습니다
    COMMENT_REPLIED,          // 대댓글이 달렸습니다

    // 프로필 관련
    PROFILE_UPDATED,          // 프로필이 업데이트되었습니다

    // 시스템 관련
    ACCOUNT_ACTIVATED,        // 계정이 활성화되었습니다
    ACCOUNT_DEACTIVATED,      // 계정이 비활성화되었습니다
}
```

**인덱스**

* `idx_member_event_member_id`: member_id
* `idx_member_event_event_type`: event_type
* `idx_member_event_is_read`: is_read
* `idx_member_event_created_at`: created_at

---

## 10. 도메인 이벤트 시스템

### 10.1 AggregateRoot 패턴

**_Interface_**

```kotlin
fun interface AggregateRoot {
    /**
     * 애그리거트에 축적된 도메인 이벤트를 모두 가져오고 초기화합니다.
     */
    fun drainDomainEvents(): List<Any>
}
```

#### 구현 엔티티

* **Member**: 회원 도메인 이벤트 발행
* **Post**: 게시물 도메인 이벤트 발행
* **Comment**: 댓글 도메인 이벤트 발행
* **Friendship**: 친구 관계 도메인 이벤트 발행

### 10.2 도메인 이벤트 계층 구조

#### 공통 인터페이스

```kotlin
interface DomainEvent {
    val occurredOn: Instant  // 이벤트 발생 시각
}
```

#### 도메인별 이벤트 마커 인터페이스

```kotlin
interface MemberDomainEvent : DomainEvent
interface PostDomainEvent : DomainEvent
interface CommentDomainEvent : DomainEvent
interface FriendDomainEvent : DomainEvent
```

### 10.3 회원 도메인 이벤트

```kotlin
// 회원 가입
data class MemberRegisteredDomainEvent(
    val memberId: Long,
    val email: String,
    val nickname: String,
    override val occurredOn: Instant
) : MemberDomainEvent

// 회원 활성화
data class MemberActivatedDomainEvent(
    val memberId: Long,
    val email: String,
    val nickname: String,
    override val occurredOn: Instant
) : MemberDomainEvent

// 회원 비활성화
data class MemberDeactivatedDomainEvent(
    val memberId: Long,
    override val occurredOn: Instant
) : MemberDomainEvent

// 프로필 업데이트
data class MemberProfileUpdatedDomainEvent(
    val memberId: Long,
    val nickname: String,
    val introduction: String?,
    val profileAddress: String?,
    override val occurredOn: Instant
) : MemberDomainEvent

// 비밀번호 변경
data class PasswordChangedDomainEvent(
    val memberId: Long,
    override val occurredOn: Instant
) : MemberDomainEvent
```

### 10.4 게시물 도메인 이벤트

```kotlin
// 게시물 생성
data class PostCreatedEvent(
    val postId: Long,
    val authorMemberId: Long,
    val restaurantName: String,
    val isSponsored: Boolean,
    override val occurredOn: Instant
) : PostDomainEvent

// 게시물 삭제
data class PostDeletedEvent(
    val postId: Long,
    val authorMemberId: Long,
    override val occurredOn: Instant
) : PostDomainEvent
```

### 10.5 댓글 도메인 이벤트

```kotlin
// 댓글 생성
data class CommentCreatedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    val postAuthorMemberId: Long,
    val parentCommentId: Long?,
    override val occurredOn: Instant
) : CommentDomainEvent

// 댓글 삭제
data class CommentDeletedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    override val occurredOn: Instant
) : CommentDomainEvent
```

### 10.6 친구 관계 도메인 이벤트

```kotlin
// 친구 요청 발송
data class FriendRequestSentEvent(
    val friendshipId: Long,
    val requesterId: Long,
    val recipientId: Long,
    override val occurredOn: Instant
) : FriendDomainEvent

// 친구 요청 수락
data class FriendRequestAcceptedEvent(
    val friendshipId: Long,
    val requesterId: Long,
    val recipientId: Long,
    override val occurredOn: Instant
) : FriendDomainEvent

// 친구 요청 거절
data class FriendRequestRejectedEvent(
    val friendshipId: Long,
    val requesterId: Long,
    val recipientId: Long,
    override val occurredOn: Instant
) : FriendDomainEvent

// 친구 관계 해제
data class FriendshipTerminatedEvent(
    val friendshipId: Long,
    val initiatorId: Long,
    val targetId: Long,
    override val occurredOn: Instant
) : FriendDomainEvent
```

### 10.7 이벤트 발행 및 처리

#### 발행 방식

```
1. 엔티티 상태 변경 메서드 호출
   ↓
2. 도메인 이벤트 생성 및 내부 리스트에 저장
   ↓
3. 트랜잭션 커밋 전 DomainEventPublisher가 이벤트 발행
   ↓
4. Spring ApplicationEventPublisher를 통해 이벤트 발행
   ↓
5. 이벤트 리스너가 비동기로 처리 (@Async)
```

#### 주요 리스너

* **MemberEventDomainEventListener**: 도메인 이벤트 → MemberEvent 생성
* **EmailEventListener**: 이메일 발송 이벤트 처리
* **FriendshipDomainEventListener**: 친구 관계 이벤트 → MemberEvent 생성
* **PostDomainEventListener**: 게시물 이벤트 → MemberEvent 생성
* **CommentDomainEventListener**: 댓글 이벤트 → MemberEvent 생성

---

## 11. 공통 설계 요소

### 11.1 BaseEntity

```kotlin
@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    fun requireId(): Long
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
```

**특징**

- 모든 Entity가 상속
- Hibernate Proxy 안전한 equals/hashCode
- ID 기반 동등성 비교

---

### 11.2 예외 계층

```
RuntimeException
├─ ValueObjectValidationException
│  ├─ EmailValidationException
│  ├─ NicknameValidationException
│  ├─ PasswordValidationException
│  └─ ...
├─ MemberDomainException
│  ├─ InvalidMemberStatusException
│  ├─ InvalidPasswordException
│  └─ UnauthorizedRoleOperationException
├─ PostDomainException
│  ├─ InvalidPostContentException
│  ├─ InvalidPostStatusException
│  └─ UnauthorizedPostOperationException
├─ CollectionDomainException
│  ├─ CollectionNotFoundException
│  └─ CollectionUpdateException
├─ FriendDomainException
│  └─ FriendApplicationException
├─ FollowDomainException
│  └─ FollowApplicationException
├─ CommentDomainException
│  └─ CommentApplicationException
└─ ApplicationException
   ├─ MemberApplicationException
   │  ├─ DuplicateEmailException
   │  └─ DuplicateProfileAddressException
   └─ PostApplicationException
      └─ PostNotFoundException
```

---

## 12. 애플리케이션 레이어

### 12.1 서비스 구조

#### Provided 인터페이스 (Use Case)

**회원 관리**

- `MemberRegister`: 회원 등록
- `MemberActivate`: 회원 활성화
- `MemberReader`: 회원 조회
- `MemberUpdater`: 회원 수정
- `MemberVerify`: 회원 인증 (로그인)
- `PasswordResetter`: 비밀번호 재설정

**게시글 관리**

- `PostCreator`: 게시글 생성
- `PostReader`: 게시글 조회
- `PostUpdater`: 게시글 수정/삭제
- `PostHeartManager`: 좋아요 관리
- `PostHeartReader`: 좋아요 조회

**컬렉션 관리**

- `CollectionCreator`: 컬렉션 생성
- `CollectionReader`: 컬렉션 조회
- `CollectionUpdater`: 컬렉션 수정
- `CollectionDeleter`: 컬렉션 삭제

**친구 관리**

- `FriendRequestor`: 친구 요청
- `FriendResponder`: 친구 응답
- `FriendshipReader`: 친구 관계 조회
- `FriendshipTerminator`: 친구 관계 종료

**팔로우 관리**

- `FollowCreator`: 팔로우 시작
- `FollowReader`: 팔로우 조회
- `FollowTerminator`: 팔로우 해제

**댓글 관리**

- `CommentCreator`: 댓글 생성
- `CommentReader`: 댓글 조회
- `CommentUpdater`: 댓글 수정

**토큰 관리**

- `ActivationTokenGenerator`: 활성화 토큰 생성
- `ActivationTokenReader`: 활성화 토큰 조회
- `PasswordResetTokenGenerator`: 재설정 토큰 생성
- `PasswordResetTokenReader`: 재설정 토큰 조회

#### Required 인터페이스 (Dependency)

**리포지토리**

- `MemberRepository`
- `PostRepository`
- `PostHeartRepository`
- `CollectionRepository`
- `FriendshipRepository`
- `FollowRepository`
- `CommentRepository`
- `ActivationTokenRepository`
- `PasswordResetTokenRepository`

**외부 서비스**

- `EmailSender`: 이메일 전송
- `EmailTemplate`: 이메일 템플릿

---

### 12.2 주요 서비스 흐름

#### 회원 등록

```
1. MemberRegistrationService.register()
2. 이메일 중복 검증
3. 비밀번호 해시화
4. Member.register() 호출
5. Member 저장
6. ActivationToken 생성
7. MemberRegisteredEvent 발행
8. → MemberEventListener (비동기)
9. → 활성화 이메일 발송
```

#### 컬렉션 생성

```
1. CollectionCreationService.create()
2. 회원 조회 (ACTIVE 확인)
3. PostCollection.create() 호출
4. PostCollection 저장
5. CollectionCreatedEvent 발행
```

#### 친구 요청

```
1. FriendRequestService.request()
2. 대상 회원 조회
3. 중복 요청 확인
4. Friendship.request() 호출
5. Friendship 저장
6. FriendRequestSentEvent 발행
```

---

### 12.3 이벤트 기반 처리

#### 회원 이벤트 리스너

```kotlin
@Component
class MemberEventListener(
    private val activationEmailSender: MemberActivationEmailSender,
    private val passwordResetEmailSender: MemberPasswordResetEmailSender
) {
    @Async
    @EventListener
    fun handleMemberRegistered(event: MemberRegisteredEvent)

    @Async
    @EventListener
    fun handleResendActivationEmail(event: ResendActivationEmailEvent)

    @Async
    @EventListener
    fun handlePasswordResetRequested(event: PasswordResetRequestedEvent)
}
```

#### 친구 이벤트 리스너

```kotlin
@Component
class FriendEventListener {
    @Async
    @EventListener
    fun handleFriendRequestSent(event: FriendRequestSentEvent)

    @Async
    @EventListener
    fun handleFriendRequestAccepted(event: FriendRequestAcceptedEvent)

    @Async
    @EventListener
    fun handleFriendshipTerminated(event: FriendshipTerminatedEvent)
}
```

**특징**

- `@Async` 비동기 처리
- 메인 로직과 트랜잭션 분리
- 실패해도 메인 로직에 영향 없음

---

## 13. 설계 특징 및 패턴

### 13.1 Aggregate 설계 원칙

#### Member Aggregate

- **구성**: Member, MemberDetail, TrustScore, Roles
- **특징**: 강한 일관성, Member를 통해서만 접근
- **트랜잭션 경계**: Member 단위

#### Post Aggregate

- **구성**: Post, PostImages
- **특징**: Post가 Root, PostImages는 ElementCollection
- **PostHeart 분리 이유**: 동시성 제어, 독립적 생명주기

#### PostCollection Aggregate

- **구성**: PostCollection, CollectionInfo, CollectionOwner, CollectionPosts
- **특징**: 컬렉션 단위 일관성, 포스트 목록 관리

#### Social Aggregates (Friendship, Follow, Comment)

- **특징**: 독립적 생명주기, 별도 트랜잭션
- **분리 이유**: 동시성 제어, 관계 관리 복잡성

#### Token Aggregates

- **특징**: Member와 논리적 연관만 존재
- **분리 이유**: 독립적 생명주기, 별도 트랜잭션

---

### 13.2 Value Object 활용

#### Embedded 방식

- Author, Restaurant, PostContent → Post 테이블에 포함
- CollectionInfo, CollectionOwner → PostCollection 테이블에 포함
- 장점: 응집도 향상, 조회 성능 최적화

#### ElementCollection 방식

- Roles, PostImages, CollectionPosts → 별도 테이블
- 장점: 다중 값 관리, 순서 보장

#### 일반 클래스

- RawPassword → 영속화 안 됨
- 용도: 입력 검증 및 변환

---

### 13.3 동시성 처리

#### 문제 상황

- 여러 사용자가 동시에 좋아요/조회/팔로우
- 카운트 업데이트 경합 상태

#### 해결 방법

```kotlin
@Modifying
@Query("UPDATE Post p SET p.heartCount = p.heartCount + 1 WHERE p.id = :postId")
fun incrementHeartCount(postId: Long)
```

**전략**

- DB 레벨 UPDATE 쿼리 사용
- 이벤트 기반 비동기 처리
- Eventually Consistent 허용

---

### 13.4 Soft Delete 패턴

**구현**

- 삭제 시 status만 DELETED로 변경
- 실제 데이터 보존
- 조회 시 `status != DELETED` 필터링

**적용 대상**

- Member, Post, PostCollection, Comment
- 장점: 데이터 복구 가능, 통계 분석 가능

---

### 13.5 아키텍처 패턴

#### 헥사고날 아키텍처

```
┌─────────────────────────────────────────┐
│         Adapter (Web/View)              │
│  Controllers, Views, ExceptionHandlers  │
├─────────────────────────────────────────┤
│      Application (Use Case)             │
│      Services, Events, DTOs             │
│                                         │
│       Provided ←→ Required              │  ← 포트
├─────────────────────────────────────────┤
│          Domain (Core)                  │
│  Aggregates, Entities, Value Objects    │
├─────────────────────────────────────────┤
│     Adapter (Infrastructure)            │
│  JPA Repositories, Email Sender         │
└─────────────────────────────────────────┘
```

**의존성 방향**: 외부 → 내부 (단방향)

#### Clean Architecture 원칙

- 의존성 역전 (Dependency Inversion)
- 단일 책임 (Single Responsibility)
- 개방-폐쇄 (Open-Closed)

---

### 13.6 Kotlin 특성 활용

#### data class

- Value Object 간결하게 표현
- equals/hashCode 자동 생성

#### init 블록

- 생성 시점 검증
- 불변 객체 보장

#### require()

- 제약조건 명시적 표현
- 가독성 향상

#### protected constructor

- 직접 생성 제어
- 팩토리 메서드 강제

#### companion object

- 정적 팩토리 메서드
- 도메인 의미 전달

---

## 부록

### A. 주요 상수

#### 회원

- 닉네임: 2-20자
- 비밀번호: 8-20자
- 프로필 주소: 3-15자
- 자기소개: 최대 500자

#### 게시글

- 내용: 최대 2000자
- 평점: 1-5
- 이미지: 최대 5장
- 이미지 URL: 최대 500자

#### 컬렉션

- 이름: 최대 50자
- 설명: 최대 500자
- 포스트: 최대 100개

#### 댓글

- 내용: 최대 1000자
- 멘션: 최대 10개

#### 신뢰도

- 내돈내산 리뷰: +5점
- 광고성 리뷰: +1점
- 도움됨: +2점
- 위반: -20점

### B. 인덱스 전략

#### members

- `idx_member_email`: email
- `idx_member_nickname`: nickname
- `idx_member_status`: status

#### posts

- `idx_post_author_id`: author_member_id
- `idx_post_status`: status
- `idx_post_created_at`: created_at
- `idx_post_restaurant_name`: restaurant_name

#### post_collections

- `idx_collection_owner_member_id`: owner_member_id
- `idx_collection_privacy_status`: privacy, status
- `idx_collection_created_at`: created_at
- `idx_collection_status`: status

#### friendships

- `idx_friendship_from_member_id`: from_member_id
- `idx_friendship_to_member_id`: to_member_id
- `idx_friendship_status`: status

#### follows

- `idx_follow_follower_id`: follower_id
- `idx_follow_following_id`: following_id
- `idx_follow_status`: status

#### comments

- `idx_comment_post_id`: post_id
- `idx_comment_author_id`: author_member_id
- `idx_comment_status`: status

#### post_hearts

- `idx_post_heart_post_id`: post_id
- `idx_post_heart_member_id`: member_id
- `uk_post_heart_post_member`: (post_id, member_id) UNIQUE

#### password_reset_tokens

- `idx_password_reset_token`: token
- `idx_password_reset_member_id`: member_id
