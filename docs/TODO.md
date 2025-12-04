# 🧪 테스트 가이드

## 🎯 테스트 철학

RMRT는 **실제 사용 시나리오**를 중심으로 테스트합니다. Mock을 최소화하고 실제 데이터베이스와 Spring Context를 사용하여 통합 테스트를 선호합니다.

- **통합 테스트 우선**: 단위 테스트보다 통합 테스트 중심
- **실제 데이터**: Testcontainers MySQL 사용
- **인증 테스트**: `@WithMockMember`로 실제 인증 시나리오
- **CSRF 보호**: 모든 POST/PUT/DELETE 요청에 CSRF 적용

---

## 🛠 테스트 도구 스택

### 핵심 도구

- **JUnit 5**: 테스트 프레임워크
- **MockK**: Mock 객체 생성 (Kotlin 친화적)
- **Testcontainers**: 실제 Docker MySQL 컨테이너
- **LocalStack**: AWS S3 로컬 테스트 환경
- **MockMvc**: 웹 계층 테스트
- **Spring Boot Test**: 통합 테스트 지원

### 테스트 유틸리티

- **IntegrationTestBase**: 모든 통합 테스트의 기본 클래스
- **TestMemberHelper**: 멤버 생성 유틸리티
- **TestPostHelper**: 포스트 생성 유틸리티
- **@WithMockMember**: 인증된 사용자 시뮬레이션

---

## 🏗 테스트 구조

### 기본 클래스 설정

```kotlin
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestcontainersConfiguration::class, TestConfig::class)
@AutoConfigureMockMvc
abstract class IntegrationTestBase() {
    @Autowired
    protected lateinit var entityManager: EntityManager

    protected fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}
```

### 테스트 클래스 패턴

```kotlin
class CollectionDeleteApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var collectionCreator: CollectionCreator

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `deleteCollection - success - deletes own collection`() {
        // Given: 테스트 데이터 준비
        val owner = testMemberHelper.getDefaultMember()
        val collection = collectionCreator.createCollection(...)

        // When: API 호출
        mockMvc.perform(
            delete("/api/collections/${collection.requireId()}")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().string(""))

        // Then: 결과 검증
        assertFailsWith<IllegalArgumentException> {
            collectionReader.readById(collection.requireId())
        }
    }
}
```

---

## 🔐 인증 테스트

### @WithMockMember 사용

```kotlin
@WithMockMember(email = "test@example.com", nickname = "테스트")
@Test
fun `authenticated request test`() {
    // 인증된 상태로 테스트 실행
}
```

### 인증 실패 테스트

```kotlin
@Test
fun `deleteCollection - forbidden - when not authenticated`() {
    mockMvc.perform(
        delete("/api/collections/1")
            .with(csrf())
    )
        .andExpect(status().isForbidden())
}
```

---

## 📝 MockMvc 테스트 패턴

### API 테스트

```kotlin
// 성공 케이스
mockMvc.perform(
    delete("/api/collections/${collection.requireId()}")
        .with(csrf())
)
    .andExpect(status().isOk)
    .andExpect(content().string(""))

// 에러 응답 검증
mockMvc.perform(
    delete("/api/collections/${collection.requireId()}")
        .with(csrf())
)
    .andExpect(status().isBadRequest())
    .andExpect(content().contentType("application/json"))
    .andExpect(jsonPath("$.success").value(false))
    .andExpect(jsonPath("$.error").value("컬렉션을 삭제할 수 없습니다."))
```

### WebView 테스트

```kotlin
mockMvc.perform(
    get(CollectionUrls.MY_LIST_FRAGMENT)
        .param("filter", CollectionFilter.PUBLIC.name)
)
    .andExpect(status().isOk)
    .andExpect(view().name(CollectionViews.MY_LIST))
    .andExpect(model().attributeExists("collections"))
    .andExpect(model().attributeExists("member"))

// 반환된 모델 데이터 검증
val result = mockMvc.perform(...).andReturn()
val collections = result.modelAndView!!.model["collections"] as Page<*>
assertEquals(1, collections.content.size)
```

---

## 🏭 테스트 데이터 생성

### TestMemberHelper 사용

```kotlin
// 기본 멤버 생성
val member = testMemberHelper.getDefaultMember()

// 커스텀 멤버 생성
val customMember = testMemberHelper.createActivatedMember(
    email = "custom@test.com",
    nickname = "커스텀"
)

// 비활성 멤버 생성
val inactiveMember = testMemberHelper.createMember(
    email = "inactive@test.com"
)
```

### TestPostHelper 사용

```kotlin
val post = testPostHelper.createPost(
    authorMemberId = member.requireId(),
    authorNickname = "작성자",
    restaurant = Restaurant("식당", "주소"),
    content = PostContent("맛있어요!")
)
```

### 직접 도메인 생성

```kotlin
val collection = collectionCreator.createCollection(
    CollectionCreateCommand(
        name = "테스트 컬렉션",
        description = "설명",
        ownerMemberId = owner.requireId(),
        ownerName = owner.nickname.value
    )
)
```

---

## 🎯 테스트 시나리오 예제

### 성공 시나리오

```kotlin
@WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
@Test
fun `createCollection - success - creates new collection`() {
    val member = testMemberHelper.getDefaultMember()

    val result = mockMvc.perform(
        post("/api/collections")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                    "name": "새 컬렉션",
                    "description": "설명"
                }
            """.trimIndent()
            )
    )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.collectionId").exists())
        .andReturn()

    // 생성된 컬렉션 확인
    val collectionId = result.response.jsonPath.getLong("collectionId")
    assertNotNull(collectionReader.readById(collectionId))
}
```

### 권한 없음 시나리오

```kotlin
@WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
@Test
fun `deleteCollection - failure - when trying to delete other's collection`() {
    val owner = testMemberHelper.createActivatedMember("other@user.com")
    val collection = createCollectionForOwner(owner)

    mockMvc.perform(
        delete("/api/collections/${collection.requireId()}")
            .with(csrf())
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("컬렉션을 삭제할 수 없습니다."))
}
```

### 이벤트 발행 테스트

```kotlin
@RecordApplicationEvents
@Test
fun `unfriend - success - publishes friendship terminated event`() {
    // 친구 관계 생성 및 삭제

    val events = applicationEvents
        .stream(FriendshipTerminatedEvent::class.java)
        .toList()

    assertEquals(1, events.size)
    assertEquals(member1.requireId(), events[0].memberId)
}
```

---

## 🔄 테스트 실행 방법

### 전체 테스트 실행

```bash
./gradlew test
```

### 특정 테스트만 실행

```bash
# API 테스트만
./gradlew test --tests "*ApiTest*"

# WebView 테스트만
./gradlew test --tests "*ViewTest*"

# 애플리케이션 테스트만
./gradlew test --tests "*application*"

# 특정 클래스 테스트
./gradlew test --tests "*CollectionDeleteApiTest*"
```

### 테스트 커버리지 확인

```bash
./gradlew jacocoTestReport
# 결과: build/reports/jacoco/test/html/index.html
```

---

## 🖼 이미지 관리 시스템 테스트

### LocalStack S3 테스트

이미지 업로드/조회/삭제 기능은 LocalStack을 통해 실제 S3 환경과 동일하게 테스트됩니다:

```kotlin
@SpringBootTest
@Import(TestcontainersConfiguration::class)
class ImageUploadServiceTest {

    @Autowired
    private lateinit var imageUploadRequester: ImageUploadRequester

    @Autowired
    private lateinit var imageUploadTracker: ImageUploadTracker

    @Test
    fun `requestPresignedPutUrl - success - returns valid presigned URL`() {
        // Given
        val request = ImageUploadRequest(
            memberId = 1L,
            imageType = ImageType.POST_IMAGE,
            contentType = "image/jpeg"
        )

        // When
        val response = imageUploadRequester.requestPresignedPutUrl(request)

        // Then
        assertNotNull(response.uploadUrl)
        assertTrue(response.uploadUrl.contains("localhost"))
        assertNotNull(response.key)
        assertNotNull(response.expiresAt)
    }

    @Test
    fun `trackUploadCompletion - success - saves image metadata`() {
        // Given: Presigned URL 발급
        val uploadRequest = ImageUploadRequest(...)
        val presignedResponse = imageUploadRequester.requestPresignedPutUrl(uploadRequest)

        // When: 업로드 완료 추적
        val result = imageUploadTracker.trackUploadCompletion(
            key = presignedResponse.key,
            memberId = 1L
        )

        // Then: 메타데이터 저장 확인
        assertTrue(result.success)
        assertNotNull(result.imageId)
    }
}
```

### 이미지 테스트 시나리오

#### 1. Presigned URL 발급 테스트

```kotlin
@Test
fun `image upload request - success - returns presigned PUT URL`() {
    mockMvc.perform(
        post("/api/images/upload-request")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                    "imageType": "POST_IMAGE",
                    "contentType": "image/jpeg"
                }
            """.trimIndent()
            )
    )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.uploadUrl").exists())
        .andExpect(jsonPath("$.key").exists())
        .andExpect(jsonPath("$.expiresAt").exists())
}
```

#### 2. 업로드 완료 추적 테스트

```kotlin
@Test
fun `image upload confirmation - success - saves metadata to database`() {
    // Given: Presigned URL 발급
    val uploadResponse = requestPresignedUrl()

    // When: 업로드 완료 알림
    mockMvc.perform(
        post("/api/images/upload-confirm")
            .with(csrf())
            .param("key", uploadResponse.key)
    )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.imageId").exists())
}
```

#### 3. 이미지 삭제 테스트

```kotlin
@Test
fun `delete image - success - soft deletes image`() {
    // Given: 이미지 업로드
    val image = createTestImage()

    // When: 삭제 요청
    mockMvc.perform(
        delete("/api/images/${image.id}")
            .with(csrf())
    )
        .andExpect(status().isOk)

    // Then: Soft Delete 확인
    val deletedImage = imageRepository.findById(image.id!!).get()
    assertTrue(deletedImage.isDeleted)
}
```

#### 4. 일일 업로드 제한 테스트

```kotlin
@Test
fun `upload request - failure - when daily limit exceeded`() {
    // Given: 100개 이미지 업로드 완료
    repeat(100) { uploadImage() }

    // When: 101번째 업로드 시도
    mockMvc.perform(
        post("/api/images/upload-request")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"imageType": "POST_IMAGE", "contentType": "image/jpeg"}""")
    )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("일일 업로드 한도를 초과했습니다"))
}
```

### LocalStack 설정 확인

Testcontainers Configuration에서 LocalStack S3가 자동으로 시작됩니다:

```kotlin
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun localStackContainer(): LocalStackContainer {
        return LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
            .withEnv("DEBUG", "1")
    }
}
```

---
