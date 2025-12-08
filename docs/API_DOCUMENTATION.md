# 📖 API 문서

## 🌐 API 개요

RMRT는 RESTful API와 WebView 기반의 하이브리드 구조를 제공합니다.

- **Base URL**: `http://localhost:8080`,`https://rmrt.albert-im.com/`
- **인증**: Spring Security 기반 Session 인증
- **Content-Type**: `application/json` (API) / `application/x-www-form-urlencoded` (Form)

## 📚 API 목차

### 👥 멤버 관련 API

- [멤버 프로필 조회](#멤버-프로필-조회)
- [추천 사용자 목록](#추천-사용자-목록)
- [멤버 인증](#멤버-인증)
- [멤버 설정](#멤버-설정)
- [멤버 프래그먼트](#멤버-프래그먼트)

### 🍽️ 포스트 관련 API

- [포스트 목록](#포스트-목록)
- [내 포스트 목록](#내-포스트-목록)
- [포스트 상세](#포스트-상세)
- [포스트 생성](#포스트-생성)
- [포스트 수정](#포스트-수정)

### 📷 이미지 관련 API (NEW!)

- [Presigned URL 요청](#presigned-url-요청)
- [업로드 확인](#업로드-확인)
- [업로드 상태 조회](#업로드-상태-조회)
- [이미지 URL 조회](#이미지-url-조회)
- [내 이미지 목록](#내-이미지-목록)
- [이미지 삭제](#이미지-삭제)

### 💬 댓글 관련 API

- [댓글 수 조회](#댓글-수-조회)
- [댓글 생성](#댓글-생성)
- [댓글 수정](#댓글-수정)

### 👥 친구 관련 API

- [친구 목록](#친구-목록)
- [친구 수 조회](#친구-수-조회)
- [친구 요청](#친구-요청)
- [친구 수락/거절](#친구-수락거절)
- [친구 삭제](#친구-삭제)

### ➿ 팔로우 관련 API

- [팔로우 통계 조회](#팔로우-통계-조회)
- [팔로잉 목록](#팔로잉-목록)
- [팔로워 목록](#팔로워-목록)
- [팔로우 생성](#팔로우-생성)
- [팔로우 삭제](#팔로우-삭제)
- [팔로우 프래그먼트](#팔로우-프래그먼트)

### 📚 컬렉션 관련 API

- [컬렉션 생성](#컬렉션-생성)
- [컬렉션 수정](#컬렉션-수정)
- [컬렉션 삭제](#컬렉션-삭제)
- [컬렉션 게시글 추가/제거](#컬렉션-게시글-추가제거)
- [컬렉션 상세 조회](#컬렉션-상세-조회)

### 🔔 회원 이벤트 관련 API (NEW!)

- [내 이벤트 목록 조회](#내-이벤트-목록-조회)
- [읽지 않은 이벤트 수 조회](#읽지-않은-이벤트-수-조회)
- [이벤트 읽음 처리](#이벤트-읽음-처리)
- [이벤트 일괄 읽음 처리](#이벤트-일괄-읽음-처리)

---

## 👥 멤버 관련 API

### 멤버 프로필 조회

```http
GET /members/{id}/profile
```

**응답:** HTML 프로필 페이지

### 추천 사용자 목록

```http
GET /members/fragment/suggest-users-sidebar
```

**인증 필요**

**응답:** HTML 프래그먼트

### 멤버 인증

**컨트롤러 리팩토링 (feature36):**

```kotlin
@Controller
class MemberAuthView  // 인증 전담 컨트롤러
```

**담당 기능:**

- 로그인/로그아웃
- 회원가입
- 이메일 인증
- 비밀번호 재설정 (이메일 토큰)
- 커스텀 PasswordResetFormValidator 적용

### 멤버 설정

**컨트롤러 리팩토링 (feature36):**

```kotlin
@Controller
class MemberSettingsView  // 설정 전담 컨트롤러
```

**담당 기능:**

- 계정 정보 수정
- 비밀번호 변경
- 프로필 이미지 변경
- 회원 탈퇴

### 멤버 프래그먼트

**컨트롤러 리팩토링 (feature36):**

```kotlin
@Controller
class MemberFragmentView  // 프래그먼트 전담 컨트롤러
```

**담당 기능:**

- 추천 사용자 사이드바 프래그먼트
- 프로필 프래그먼트
- 동적 컨텐츠 로딩

**아키텍처 개선:**

- MemberView (700줄+) → 4개 전문 컨트롤러 분리
- 단일 책임 원칙 적용
- 패키지 구조 체계화: form, validator, converter, util, message
- 테스트 클래스 분리 (MemberAuthViewTest, MemberSettingsViewTest 등)

---

## 🍽️ 포스트 관련 API

### 포스트 목록

```http
GET /posts/fragment
```

**쿼리 파라미터:**

- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 기준 (기본값: createdAt,desc)

**응답:** HTML 프래그먼트

### 내 포스트 목록

```http
GET /members/my/posts
GET /members/my/posts/fragment
```

**인증 필요**

**응답:** HTML 페이지/프래그먼트

### 포스트 상세

```http
GET /posts/{postId}
GET /posts/{postId}/modal
```

**응답:** HTML 상세 페이지/모달

### 포스트 생성

```http
POST /posts/create
Content-Type: application/x-www-form-urlencoded
```

**인증 필요**

**요청 파라미터:**

- `restaurantName`: 식당 이름
- `restaurantAddress`: 식당 주소
- `content`: 내용
- `isRealMoney`: 내돈내산 여부
- `rating`: 평점

### 포스트 수정

```http
POST /posts/{postId}/update
Content-Type: application/x-www-form-urlencoded
```

**인증 필요**

---

## 📷 이미지 관련 API

> **AWS S3 Presigned URL 방식**을 사용하여 서버 부하를 최소화하고 안전한 이미지 업로드를 제공합니다.

### Presigned URL 요청

```http
POST /api/images/upload-request
Content-Type: application/json
```

**인증 필요**

**요청:**

```json
{
  "fileName": "photo.jpg",
  "contentType": "image/jpeg",
  "fileSize": 1024000,
  "imageType": "POST_IMAGE",
  "width": 1920,
  "height": 1080
}
```

**요청 필드:**

| 필드            | 타입     | 필수 | 설명                                            |
|---------------|--------|----|-----------------------------------------------|
| `fileName`    | String | ✅  | 원본 파일명                                        |
| `contentType` | String | ✅  | MIME 타입 (image/jpeg, image/png 등)             |
| `fileSize`    | Long   | ✅  | 파일 크기 (바이트, 최대 5MB)                           |
| `imageType`   | String | ✅  | 이미지 타입 (POST_IMAGE, PROFILE_IMAGE, THUMBNAIL) |
| `width`       | Int    | ✅  | 이미지 가로 크기                                     |
| `height`      | Int    | ✅  | 이미지 세로 크기                                     |

**응답:**

```json
{
  "uploadUrl": "https://bucket.s3.region.amazonaws.com/path/to/file?X-Amz-Algorithm=...",
  "key": "posts/123/uuid-photo.jpg",
  "expiresAt": "2025-11-30T12:15:00Z",
  "metadata": {
    "original-name": "photo.jpg",
    "content-type": "image/jpeg",
    "file-size": "1024000",
    "width": "1920",
    "height": "1080"
  }
}
```

**응답 필드:**

| 필드          | 타입       | 설명                            |
|-------------|----------|-------------------------------|
| `uploadUrl` | String   | S3 Presigned PUT URL (15분 유효) |
| `key`       | String   | S3 파일 키                       |
| `expiresAt` | DateTime | URL 만료 시간                     |
| `metadata`  | Map      | 이미지 메타데이터                     |

**제한 사항:**

- 일일 업로드 제한: 100개
- 파일 크기 제한: 5MB
- 지원 형식: JPEG, PNG, GIF, WebP

### 업로드 확인

Presigned URL로 S3에 업로드 완료 후, 데이터베이스에 메타데이터를 저장합니다.

```http
POST /api/images/upload-confirm?key={fileKey}
```

**인증 필요**

**쿼리 파라미터:**

- `key`: S3 파일 키 (Presigned URL 응답에서 받은 값)

**응답:**

```json
{
  "success": true,
  "imageId": 123
}
```

**응답 필드:**

| 필드        | 타입      | 설명         |
|-----------|---------|------------|
| `success` | Boolean | 업로드 성공 여부  |
| `imageId` | Long    | 생성된 이미지 ID |

### 업로드 상태 조회

```http
GET /api/images/upload-status/{fileKey}
```

**인증 필요**

**응답:**

```json
{
  "success": true,
  "imageId": 123
}
```

### 이미지 URL 조회

```http
GET /api/images/{imageId}/url
```

**인증 필요**

**응답:**

```json
{
  "url": "https://bucket.s3.region.amazonaws.com/path/to/file?X-Amz-Algorithm=..."
}
```

**설명:**

- Presigned GET URL (15분 유효)
- 이미지 조회 권한이 있는 사용자만 접근 가능

### 내 이미지 목록

```http
GET /api/images/my-images
```

**인증 필요**

**응답:**

```json
[
  {
    "imageId": 123,
    "fileKey": "posts/123/uuid-photo.jpg",
    "imageType": "POST_IMAGE",
    "createdAt": "2025-11-30T10:00:00Z"
  },
  {
    "imageId": 124,
    "fileKey": "profiles/456/uuid-avatar.jpg",
    "imageType": "PROFILE_IMAGE",
    "createdAt": "2025-11-30T09:00:00Z"
  }
]
```

**응답 필드:**

| 필드          | 타입       | 설명      |
|-------------|----------|---------|
| `imageId`   | Long     | 이미지 ID  |
| `fileKey`   | String   | S3 파일 키 |
| `imageType` | String   | 이미지 타입  |
| `createdAt` | DateTime | 생성 일시   |

### 이미지 삭제

```http
DELETE /api/images/{imageId}
```

**인증 필요**

**응답:**

```json
{
  "message": "이미지가 성공적으로 삭제되었습니다"
}
```

**설명:**

- 소프트 삭제 방식 (is_deleted 플래그)
- 업로드한 사용자만 삭제 가능

### 이미지 타입

| 타입              | 설명      | 용도           |
|-----------------|---------|--------------|
| `POST_IMAGE`    | 게시글 이미지 | 게시글에 첨부되는 사진 |
| `PROFILE_IMAGE` | 프로필 이미지 | 사용자 프로필 사진   |
| `THUMBNAIL`     | 썸네일 이미지 | 미리보기용 작은 이미지 |

### 보안 고려사항

1. **인증 필수**: 모든 이미지 API는 인증 필요
2. **업로드 제한**: 일일 100개, 파일당 5MB
3. **파일 키 안전성**: UUID 기반 고유 파일명, 경로 탐색 공격 방지
4. **시간 제한**: Presigned URL 15분 유효
5. **CSRF 보호**: 모든 POST/PUT/DELETE 요청에 CSRF 토큰 필요

### 에러 응답

```json
{
  "success": false,
  "error": "일일 업로드 제한을 초과했습니다",
  "timestamp": "2025-11-30T12:00:00Z",
  "path": "/api/images/upload-request"
}
```

**주요 에러:**

| 상태 코드 | 에러 메시지            | 원인              |
|-------|-------------------|-----------------|
| 400   | 지원하지 않는 이미지 형식입니다 | 허용되지 않은 MIME 타입 |
| 400   | 파일 크기가 제한을 초과했습니다 | 5MB 초과          |
| 403   | 일일 업로드 제한을 초과했습니다 | 100개 제한 초과      |
| 403   | 이미지에 대한 권한이 없습니다  | 다른 사용자의 이미지     |
| 404   | 이미지를 찾을 수 없습니다    | 존재하지 않는 이미지 ID  |

---

## 💬 댓글 관련 API

### 댓글 수 조회

```http
GET /api/posts/{postId}/comments/count
```

**응답:**

```json
5
```

### 댓글 생성

```http
POST /api/posts/{postId}/comments
Content-Type: application/x-www-form-urlencoded
```

**인증 필요**

**요청 파라미터:**

- `content`: 댓글 내용
- `parentCommentId`: 부모 댓글 ID (답글인 경우)

**응답:** Comment 엔티티 JSON

### 댓글 수정

```http
PUT /api/posts/{postId}/comments/{commentId}
Content-Type: application/x-www-form-urlencoded
```

**인증 필요**

**요청 파라미터:**

- `content`: 수정할 댓글 내용

**응답:** Comment 엔티티 JSON

---

## 👥 친구 관련 API

### 친구 목록

```http
GET /api/members/{memberId}/friends
```

**쿼리 파라미터:**

- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 기준 (기본값: createdAt,desc)

**응답:** Page<FriendshipResponse>

### 친구 수 조회

```http
GET /api/members/{memberId}/friends/count
```

**응답:**

```json
5
```

### 친구 요청

```http
POST /friend-requests
Content-Type: application/json
```

**인증 필요**

**요청:**

```json
{
  "toMemberId": 2,
  "toMemberNickname": "친구1"
}
```

**응답:** HTML 프래그먼트 (친구 버튼)

### 친구 수락/거절

```http
PUT /friend-requests/{friendshipId}
Content-Type: application/x-www-form-urlencoded
```

**인증 필요**

**요청 파라미터:**

- `accept`: true (수락) / false (거절)

**응답:** HTML 프래그먼트 (친구 버튼)

### 친구 삭제

```http
DELETE /friendships/{friendshipId}/{friendMemberId}
```

**인증 필요**

**응답:** HTML 프래그먼트 (친구 버튼)

---

## ➿ 팔로우 관련 API

### 팔로우 통계 조회

```http
GET /api/members/{memberId}/follow-stats
```

**응답:**

```json
{
  "followingCount": 10,
  "followerCount": 25,
  "isFollowing": false
}
```

### 팔로잉 목록

```http
GET /api/members/{memberId}/followings
```

**쿼리 파라미터:**

- `keyword`: 검색 키워드 (선택)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 기준 (기본값: createdAt,desc)

**응답:**

```json
{
  "success": true,
  "data": {
    "content": [
      ...
    ],
    "totalElements": 10
  },
  "message": "팔로잉 목록 조회 성공"
}
```

### 팔로워 목록

```http
GET /api/members/{memberId}/followers
```

**응답:** 팔로잉 목록과 동일한 형식

### 팔로우 생성

```http
POST /members/{targetId}/follow
```

**인증 필요**

**응답:** HTML 버튼 프래그먼트

### 팔로우 삭제

```http
DELETE /members/{targetId}/follow
```

**인증 필요**

**응답:** HTML 버튼 프래그먼트

### 팔로우 프래그먼트

**팔로워 목록 프래그먼트:**

```http
GET /follow/followers?memberId={memberId}
```

**응답:** HTML 프래그먼트 (followers.html)

**기술적 특징:**

- HTMX 기반 동적 로딩
- 팔로우 상태 실시간 업데이트
- 페이지 새로고침 없는 UX

**팔로잉 목록 프래그먼트:**

```http
GET /follow/following?memberId={memberId}
```

**응답:** HTML 프래그먼트 (following.html)

**리팩토링 내역 (feature36):**

- `fragment.html` (275줄) → `followers.html` (133줄) + `following.html` (135줄) 분리
- 기능별 프래그먼트 분리로 유지보수성 향상
- 팔로우 관계 생성 시 닉네임 정보 저장으로 데이터 완전성 확보

---

## 📚 컬렉션 관련 API

### 컬렉션 생성

```http
POST /api/collections
Content-Type: application/json
```

**인증 필요**

**요청:**

```json
{
  "name": "내가 사랑하는 맛집",
  "description": "자주 가는 맛집들"
}
```

**응답:**

```json
{
  "success": true,
  "collectionId": 1,
  "message": "컬렉션이 성공적으로 생성되었습니다."
}
```

### 컬렉션 수정

```http
PUT /api/collections/{collectionId}
Content-Type: application/json
```

**인증 필요**

**요청:**

```json
{
  "name": "수정된 컬렉션",
  "description": "수정된 설명"
}
```

**응답:**

```json
{
  "success": true,
  "collectionId": 1,
  "message": "컬렉션 정보가 성공적으로 업데이트되었습니다."
}
```

### 컬렉션 삭제

```http
DELETE /api/collections/{collectionId}
```

**인증 필요**

**응답:** HTTP 204 No Content

### 컬렉션 게시글 추가/제거

```http
# 게시글 추가
POST /api/collections/{collectionId}/posts/{postId}

# 게시글 제거
DELETE /api/collections/{collectionId}/posts/{postId}
```

**인증 필요**

### 컬렉션 상세 조회

```http
GET /collections/{collectionId}
```

**응답:** HTML 페이지 (컬렉션 상세)

**Backend 개선사항 (feature36):**

```kotlin
// DTO 표준화
data class PostCollectionDetailResponse(
    val collection: PostCollectionResponse,
    val posts: List<PostResponse>,
    val authorPosts: List<PostResponse>,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val timestamp: Instant = Instant.now()
)
```

**개선 내용:**

- 단일 API 호출로 컬렉션 정보 + 포스트 목록 + 작성자 포스트 통합 제공
- 뷰 컨트롤러에서 서비스 계층으로 데이터 집계 책임 이관
- @JsonFormat으로 타임존 형식 표준화 (클라이언트 호환성 확보)
- CollectionReadService.readDetail() 메서드 추가

**응답:** 추가는 HTTP 200, 제거는 HTTP 204

---

## 🎨 WebView 프래그먼트 API

### 컬렉션 관련 프래그먼트

```http
GET /members/my/collections/fragment
GET /members/{id}/collections/fragment
GET /collections/{collectionId}/detail/fragment
GET /collections/{collectionId}/posts/fragment
```

**인증 필요**

**응답:** HTML 프래그먼트

### 포스트 관련 프래그먼트

```http
GET /members/{id}/posts/fragment
GET /posts/fragment
```

**응답:** HTML 프래그먼트

---

## 🚨 에러 응답

### API 에러 형식

```json
{
  "success": false,
  "error": "에러 메시지",
  "timestamp": "2024-01-01T00:00:00",
  "path": "/api/collections"
}
```

### WebView 에러 처리

- **400.html**: 잘못된 요청
- **401.html**: 인증 필요
- **403.html**: 권한 없음
- **404.html**: 리소스 없음
- **5xx.html**: 서버 에러

### 상태 코드

- `200`: 성공
- `204`: 성공 (컨텐츠 없음)
- `400`: 잘못된 요청
- `401`: 인증 필요
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 에러

---

## 🔐 인증

### 세션 기반 인증

- **로그인**: `POST /signin`
- **로그아웃**: 세션 무효화
- **인증 확인**: `@AuthenticationPrincipal` 사용

### 인증 필요 API

대부분의 API는 인증이 필요하며, Spring Security가 자동으로 처리합니다.

---

## 📝 테스트

API 테스트는 다음 명령으로 실행할 수 있습니다:

```bash
# API 테스트
./gradlew test --tests "*ApiTest*"

# WebView 테스트
./gradlew test --tests "*ViewTest*"

# 통합 테스트
./gradlew test --tests "*IntegrationTest*"
```

---

## 🔄 아키텍처 특징

### 하이브리드 구조

- **REST API**: JSON 데이터 교환 (`/api/*`)
- **WebView**: HTML 프래그먼트 반환 (HTMX 사용)
- **Form 처리**: `application/x-www-form-urlencoded`

### HTMX 통합

- 동적인 UI 업데이트
- 프래그먼트 기반의 렌더링
- 페이지 새로고침 없는 상호작용

## 📚 추가 자료

- [이미지 관리 시스템 상세 문서](IMAGE_MANAGEMENT.md)
- [테스트 가이드](TESTING_GUIDE.md)
- [빠른 시작](QUICK_START.md)
- [아키텍처 문서](ARCHITECTURE.md)

---

**API 사용 시 주의사항:**

- 이 프로젝트는 REST API와 WebView가 혼합된 구조입니다
- JSON API는 `/api/*` 경로를 사용합니다

---

## 🔔 회원 이벤트 관련 API

### 내 이벤트 목록 조회

현재 로그인한 사용자의 이벤트 목록을 조회합니다.

```http
GET /api/events/me?isRead={isRead}&page={page}&size={size}
```

**Query Parameters:**

| 파라미터     | 타입      | 필수 | 설명                                                      | 기본값       |
|----------|---------|----|---------------------------------------------------------|-----------|
| `isRead` | Boolean | ✗  | 읽음 여부 필터 (`true`: 읽은 이벤트만, `false`: 읽지 않은 이벤트만, 생략: 전체) | null (전체) |
| `page`   | Integer | ✗  | 페이지 번호 (0부터 시작)                                         | 0         |
| `size`   | Integer | ✗  | 페이지당 이벤트 수                                              | 20        |

**응답 예시 (200 OK):**

```json
{
  "events": [
    {
      "id": 123,
      "eventType": "FRIEND_REQUEST_RECEIVED",
      "title": "새로운 친구 요청",
      "message": "홍길동님이 친구 요청을 보냈습니다",
      "isRead": false,
      "createdAt": "2025-12-08T10:30:00",
      "relatedMemberId": 456,
      "relatedMemberNickname": "홍길동",
      "relatedMemberProfileImageUrl": "https://example.com/profile.jpg",
      "relatedPostId": null,
      "relatedCommentId": null
    },
    {
      "id": 122,
      "eventType": "POST_COMMENTED",
      "title": "새로운 댓글",
      "message": "내 게시물에 댓글이 달렸습니다",
      "isRead": false,
      "createdAt": "2025-12-08T09:15:00",
      "relatedMemberId": 789,
      "relatedMemberNickname": "김철수",
      "relatedMemberProfileImageUrl": "https://example.com/profile2.jpg",
      "relatedPostId": 100,
      "relatedCommentId": 50
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "currentPage": 0,
  "size": 20,
  "hasNext": true
}
```

**이벤트 타입 (MemberEventType):**

| 이벤트 타입                    | 설명             |
|---------------------------|----------------|
| `FRIEND_REQUEST_SENT`     | 친구 요청을 보냈습니다   |
| `FRIEND_REQUEST_RECEIVED` | 친구 요청을 받았습니다   |
| `FRIEND_REQUEST_ACCEPTED` | 친구 요청이 수락되었습니다 |
| `FRIEND_REQUEST_REJECTED` | 친구 요청이 거절되었습니다 |
| `FRIENDSHIP_TERMINATED`   | 친구 관계가 해제되었습니다 |
| `POST_CREATED`            | 새 게시물을 작성했습니다  |
| `POST_DELETED`            | 게시물을 삭제했습니다    |
| `POST_COMMENTED`          | 게시물에 댓글이 달렸습니다 |
| `COMMENT_CREATED`         | 댓글을 작성했습니다     |
| `COMMENT_DELETED`         | 댓글을 삭제했습니다     |
| `COMMENT_REPLIED`         | 대댓글이 달렸습니다     |
| `PROFILE_UPDATED`         | 프로필이 업데이트되었습니다 |
| `ACCOUNT_ACTIVATED`       | 계정이 활성화되었습니다   |
| `ACCOUNT_DEACTIVATED`     | 계정이 비활성화되었습니다  |

**오류 응답:**

- `401 Unauthorized`: 인증되지 않은 사용자

---

### 읽지 않은 이벤트 수 조회

현재 로그인한 사용자의 읽지 않은 이벤트 수를 조회합니다.

```http
GET /api/events/me/unread-count
```

**응답 예시 (200 OK):**

```json
{
  "unreadCount": 5
}
```

**오류 응답:**

- `401 Unauthorized`: 인증되지 않은 사용자

---

### 이벤트 읽음 처리

특정 이벤트를 읽음으로 표시합니다.

```http
PUT /api/events/{eventId}/mark-as-read
```

**Path Parameters:**

| 파라미터      | 타입   | 설명     |
|-----------|------|--------|
| `eventId` | Long | 이벤트 ID |

**응답 예시 (204 No Content):**

이벤트가 성공적으로 읽음 처리되었습니다. 응답 본문 없음.

**오류 응답:**

- `401 Unauthorized`: 인증되지 않은 사용자
- `403 Forbidden`: 다른 사용자의 이벤트에 접근 시도
- `404 Not Found`: 존재하지 않는 이벤트 ID

---

### 이벤트 일괄 읽음 처리

여러 이벤트를 한 번에 읽음으로 표시합니다.

```http
PUT /api/events/me/mark-all-as-read
```

**Request Body:**

```json
{
  "eventIds": [
    123,
    124,
    125,
    126
  ]
}
```

**응답 예시 (204 No Content):**

모든 이벤트가 성공적으로 읽음 처리되었습니다. 응답 본문 없음.

**오류 응답:**

- `401 Unauthorized`: 인증되지 않은 사용자
- `403 Forbidden`: 다른 사용자의 이벤트 포함 시

---

### 이벤트 프래그먼트 조회 (WebView)

HTMX를 사용한 동적 이벤트 목록 조회입니다.

```http
GET /events/fragment/list?isRead={isRead}&page={page}
```

**Query Parameters:**

| 파라미터     | 타입      | 필수 | 설명       | 기본값       |
|----------|---------|----|----------|-----------|
| `isRead` | Boolean | ✗  | 읽음 여부 필터 | null (전체) |
| `page`   | Integer | ✗  | 페이지 번호   | 0         |

**응답:** HTML 프래그먼트 (이벤트 목록)

**사용 예시 (HTMX):**

```html

<div hx-get="/events/fragment/list"
     hx-trigger="load"
     hx-target="#event-list">
</div>
```

---

## 🔔 이벤트 사용 가이드

### 실시간 알림

이벤트는 도메인 이벤트 발생 시 자동으로 생성됩니다:

1. **친구 요청 발송**: 발신자와 수신자 모두에게 이벤트 생성
2. **게시물 작성**: 작성자에게 이벤트 생성
3. **댓글 작성**: 작성자와 게시물 작성자에게 이벤트 생성

### 폴링 vs 웹소켓

현재 버전은 **폴링 방식**을 사용합니다:

- 클라이언트가 주기적으로 `/api/events/me/unread-count` 호출
- 권장 폴링 간격: 30초

**향후 개선 (WebSocket):**

- 실시간 알림 푸시
- 서버 부하 감소
- 즉각적인 사용자 경험

### 이벤트 자동 삭제

- **읽은 이벤트**: 90일 후 자동 삭제
- **읽지 않은 이벤트**: 무기한 보관
