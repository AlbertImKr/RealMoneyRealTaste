# 📖 API 문서

## 🌐 API 개요

RMRT는 RESTful API와 WebView 기반의 하이브리드 구조를 제공합니다.

- **Base URL**: `http://localhost:8080`
- **인증**: Spring Security 기반 Session 인증
- **Content-Type**: `application/json` (API) / `application/x-www-form-urlencoded` (Form)

## 📚 API 목차

### 👥 멤버 관련 API

- [멤버 프로필 조회](#멤버-프로필-조회)
- [추천 사용자 목록](#추천-사용자-목록)

### 🍽️ 포스트 관련 API

- [포스트 목록](#포스트-목록)
- [내 포스트 목록](#내-포스트-목록)
- [포스트 상세](#포스트-상세)
- [포스트 생성](#포스트-생성)
- [포스트 수정](#포스트-수정)

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

### 📚 컬렉션 관련 API

- [컬렉션 생성](#컬렉션-생성)
- [컬렉션 수정](#컬렉션-수정)
- [컬렉션 삭제](#컬렉션-삭제)
- [컬렉션 게시글 추가/제거](#컬렉션-게시글-추가제거)

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

大部分의 API는 인증이 필요하며, Spring Security가 자동으로 처리합니다.

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

- [테스트 가이드](TESTING_GUIDE.md)
- [빠른 시작](QUICK_START.md)
- [아키텍처 문서](ARCHITECTURE.md)

---

**API 사용 시 주의사항:**

- 이 프로젝트는 REST API와 WebView가 혼합된 구조입니다
- JSON API는 `/api/*` 경로를 사용합니다
