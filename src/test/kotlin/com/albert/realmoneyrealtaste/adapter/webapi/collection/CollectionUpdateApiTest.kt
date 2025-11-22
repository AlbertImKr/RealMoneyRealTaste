package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.adapter.webapi.collection.request.CollectionUpdateApiRequest
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionUpdateApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - updates collection info and returns success response`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "업데이트된 컬렉션",
            description = "업데이트된 설명",
            coverImageUrl = "https://example.com/updated-cover.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.collectionId").value(collection.requireId()))
            .andExpect(jsonPath("$.message").value("컬렉션 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - persists changes to repository`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "저장 확인 컬렉션",
            description = "데이터베이스 저장 확인용",
            coverImageUrl = null
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())

        assertAll(
            { assertNotNull(updated) },
            { assertEquals("저장 확인 컬렉션", updated?.info?.name) },
            { assertEquals("데이터베이스 저장 확인용", updated?.info?.description) },
            { assertEquals(null, updated?.info?.coverImageUrl) },
            { assertEquals(member.id, updated?.owner?.memberId) },
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - updates with minimal fields`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "최소 업데이트"
            // description, coverImageUrl는 기본값 사용
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("컬렉션 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - updates with empty description`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "빈 설명 테스트",
            description = "",
            coverImageUrl = "https://example.com/empty-desc.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())

        assertAll(
            { assertEquals("빈 설명 테스트", updated?.info?.name) },
            { assertEquals("", updated?.info?.description) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - removes cover image by setting null`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = collection.info.name,
            description = collection.info.description,
            coverImageUrl = null
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())
        assertEquals(null, updated?.info?.coverImageUrl)
    }

    @Test
    fun `updateInfo - failure - returns unauthorized when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "인증 실패 테스트",
            description = "설명"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - returns not found when collection does not exist`() {
        val request = CollectionUpdateApiRequest(
            name = "존재하지 않는 컬렉션",
            description = "테스트"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest) // CollectionUpdateException으로 인한 400
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - returns error when not owner`() {
        val owner = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람"
        )
        val collection = createTestCollection(owner.requireId(), owner.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "권한 없는 수정",
            description = "다른 사람이 수정 시도"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest) // CollectionUpdateException으로 인한 400
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - validation error when name is blank`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "",
            description = "설명"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - validation error when name exceeds max length`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "a".repeat(101), // 최대 길이 초과
            description = "설명"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - validation error when description exceeds max length`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "a".repeat(501) // 최대 길이 초과
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - validation error when cover image url is invalid`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = "invalid-url"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - missing required content type`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .content(objectMapper.writeValueAsString(request))
            // .contentType 누락
        )
            .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - does not persist changes when validation fails`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val originalName = collection.info.name
        val originalDescription = collection.info.description

        val request = CollectionUpdateApiRequest(
            name = "", // 빈 이름으로 검증 실패
            description = "저장되면 안 됨"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        val unchanged = collectionRepository.findById(collection.requireId())

        assertAll(
            { assertEquals(originalName, unchanged?.info?.name) },
            { assertEquals(originalDescription, unchanged?.info?.description) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - handles various valid image url formats`() {
        val member = testMemberHelper.getDefaultMember()

        val validUrls = listOf(
            "https://example.com/image.jpg",
            "http://example.com/image.png",
            "https://cdn.example.com/path/to/image.gif"
        )

        validUrls.forEachIndexed { index, url ->
            val collection = createTestCollection(member.requireId(), "컬렉션 $index")

            val request = CollectionUpdateApiRequest(
                name = "이미지 테스트 $index",
                description = "URL 테스트",
                coverImageUrl = url
            )

            mockMvc.perform(
                put("/api/collections/{collectionId}", collection.requireId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - returns correct response structure`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "응답 구조 테스트",
            description = "응답 구조 확인용",
            coverImageUrl = "https://example.com/response-test.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").exists())
            .andExpect(jsonPath("$.success").isBoolean)
            .andExpect(jsonPath("$.collectionId").exists())
            .andExpect(jsonPath("$.collectionId").isNumber)
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").isString)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.collectionId").value(collection.requireId()))
            .andExpect(jsonPath("$.message").value("컬렉션 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - maintains collection properties except info`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val originalPrivacy = collection.privacy
        val originalPostCount = collection.posts.size()
        val originalCreatedAt = collection.createdAt

        val request = CollectionUpdateApiRequest(
            name = "속성 유지 테스트",
            description = "다른 속성들이 유지되는지 확인",
            coverImageUrl = "https://example.com/maintained.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())

        assertAll(
            { assertEquals(originalPrivacy, updated?.privacy) },
            { assertEquals(originalPostCount, updated?.posts?.size()) },
            { assertEquals(originalCreatedAt, updated?.createdAt) },
            { assertEquals(member.id, updated?.owner?.memberId) }
        )
    }

    private fun createTestCollection(
        ownerMemberId: Long,
        ownerMemberName: String,
        name: String = "테스트 컬렉션",
    ) = collectionRepository.save(
        PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = ownerMemberId,
                ownerName = ownerMemberName,
                name = name,
                description = "테스트용 컬렉션입니다",
                coverImageUrl = "https://example.com/test.jpg",
                privacy = CollectionPrivacy.PUBLIC,
            )
        )
    )

    private fun createTestPost(
        authorId: Long,
        title: String = "테스트 게시글",
    ): Post {
        return postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorId,
                authorNickname = "테스트작성자"
            )
        )
    }

    // ========== addPost 테스트 ==========

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `addPost - success - adds post to collection`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isOk)

        val updatedCollection = collectionRepository.findById(collection.requireId())
        assertTrue(updatedCollection?.posts?.postIds?.contains(post.requireId()) == true)
    }

    @Test
    fun `addPost - failure - returns unauthorized when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `addPost - failure - returns not found when collection does not exist`() {
        val member = testMemberHelper.getDefaultMember()
        val post = createTestPost(member.requireId())

        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", 999L, post.requireId())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `addPost - success - adds non-existent post id to collection`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        // 존재하지 않는 postId도 추가 시도 - 서비스 레벨에서는 postId 유효성 검증 안 함
        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), 999L)
        )
            .andExpect(status().isOk)

        val updatedCollection = collectionRepository.findById(collection.requireId())
        assertTrue(updatedCollection?.posts?.postIds?.contains(999L) == true)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `addPost - failure - returns error when not owner`() {
        val owner = testMemberHelper.createActivatedMember(email = "owner@test.com")
        val collection = createTestCollection(owner.requireId(), owner.nickname.value)
        val post = createTestPost(owner.requireId())

        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `addPost - success - does not add duplicate post`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        // 첫 번째 추가
        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isOk)

        // 두 번째 추가 (중복) - 예외 발생
        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isBadRequest)

        val updatedCollection = collectionRepository.findById(collection.requireId())
        assertEquals(1, updatedCollection?.posts?.postIds?.size)
    }

    // ========== removePost 테스트 ==========

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `removePost - success - removes post from collection`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        // 먼저 게시글 추가
        mockMvc.perform(
            post("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isOk)

        // 게시글 제거
        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isNoContent)

        val updatedCollection = collectionRepository.findById(collection.requireId())
        assertTrue(updatedCollection?.posts?.postIds?.contains(post.requireId()) != true)
    }

    @Test
    fun `removePost - failure - returns unauthorized when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `removePost - failure - returns not found when collection does not exist`() {
        val member = testMemberHelper.getDefaultMember()
        val post = createTestPost(member.requireId())

        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", 999L, post.requireId())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `removePost - failure - returns not found when post does not exist`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), 999L)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `removePost - failure - returns error when not owner`() {
        val owner = testMemberHelper.createActivatedMember(email = "owner@test.com")
        val collection = createTestCollection(owner.requireId(), owner.nickname.value)
        val post = createTestPost(owner.requireId())

        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `removePost - success - handles removal of non-existent post gracefully`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)
        val post = createTestPost(member.requireId())

        // 컬렉션에 추가하지 않은 게시글 제거 시도 - 예외 발생
        mockMvc.perform(
            delete("/api/collections/{collectionId}/posts/{postId}", collection.requireId(), post.requireId())
        )
            .andExpect(status().isBadRequest) // 예외 발생으로 400 반환
    }

    // ========== 추가 엣지 케이스 테스트 ==========

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - handles special characters in name and description`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        val request = CollectionUpdateApiRequest(
            name = "특수字符 테스트 !@#$%^&*()",
            description = "설명에 특수문자와 이모지 🍔🍕🌮 포함",
            coverImageUrl = "https://example.com/special-chars.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())
        assertAll(
            { assertEquals("특수字符 테스트 !@#$%^&*()", updated?.info?.name) },
            { assertEquals("설명에 특수문자와 이모지 🍔🍕🌮 포함", updated?.info?.description) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - success - updates multiple times sequentially`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        // 첫 번째 업데이트
        val request1 = CollectionUpdateApiRequest(
            name = "첫 번째 업데이트",
            description = "첫 번째 설명"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        )
            .andExpect(status().isOk)

        // 두 번째 업데이트
        val request2 = CollectionUpdateApiRequest(
            name = "두 번째 업데이트",
            description = "두 번째 설명",
            coverImageUrl = "https://example.com/second-update.jpg"
        )

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        )
            .andExpect(status().isOk)

        val updated = collectionRepository.findById(collection.requireId())
        assertAll(
            { assertEquals("두 번째 업데이트", updated?.info?.name) },
            { assertEquals("두 번째 설명", updated?.info?.description) },
            { assertEquals("https://example.com/second-update.jpg", updated?.info?.coverImageUrl) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateInfo - failure - handles malformed JSON`() {
        val member = testMemberHelper.getDefaultMember()
        val collection = createTestCollection(member.requireId(), member.nickname.value)

        mockMvc.perform(
            put("/api/collections/{collectionId}", collection.requireId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ malformed json }")
        )
            .andExpect(status().isBadRequest)
    }
}
