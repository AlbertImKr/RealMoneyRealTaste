package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.adapter.webapi.collection.request.CreateCollectionApiRequest
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - creates collection and returns success response`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "API 테스트 컬렉션",
            description = "API를 통해 생성된 컬렉션",
            coverImageUrl = "https://example.com/api-cover.jpg",
            visibility = "PUBLIC"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("컬렉션이 성공적으로 생성되었습니다."))
            .andExpect(jsonPath("$.collectionId").isNumber)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - saves collection to repository`() {
        val member = testMemberHelper.createActivatedMember()
        val initialCount = collectionRepository.countByOwnerMemberId(member.requireId())

        val request = CreateCollectionApiRequest(
            name = "저장 테스트 컬렉션",
            description = "저장 확인용",
            coverImageUrl = null,
            visibility = "PRIVATE"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val finalCount = collectionRepository.countByOwnerMemberId(member.requireId())
        assertEquals(initialCount + 1, finalCount)

        val collections = collectionRepository.findByOwnerMemberIdAndPrivacy(
            member.requireId(),
            CollectionPrivacy.PRIVATE
        )
        val createdCollection = collections.find { it.info.name == "저장 테스트 컬렉션" }

        assertAll(
            { assertNotNull(createdCollection) },
            { assertEquals("저장 확인용", createdCollection?.info?.description) },
            { assertEquals(null, createdCollection?.info?.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PRIVATE, createdCollection?.privacy) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - creates collection with minimal fields`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "최소 컬렉션"
            // description, coverImageUrl, visibility는 기본값 사용
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.collectionId").isNumber)
            .andExpect(jsonPath("$.message").value("컬렉션이 성공적으로 생성되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - creates private collection by default`() {
        val member = testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "기본 비공개 컬렉션",
            description = "기본값 테스트"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val collections = collectionRepository.findByOwnerMemberIdAndPrivacy(
            member.requireId(),
            CollectionPrivacy.PRIVATE
        )
        val createdCollection = collections.find { it.info.name == "기본 비공개 컬렉션" }

        assertAll(
            { assertNotNull(createdCollection) },
            { assertEquals(CollectionPrivacy.PRIVATE, createdCollection?.privacy) }
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - creates public collection`() {
        val member = testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "공개 API 컬렉션",
            description = "공개 설명",
            coverImageUrl = "https://example.com/public.jpg",
            visibility = "PUBLIC"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val collections = collectionRepository.findByOwnerMemberIdAndPrivacy(
            member.requireId(),
            CollectionPrivacy.PUBLIC
        )
        val createdCollection = collections.find { it.info.name == "공개 API 컬렉션" }

        assertAll(
            { assertNotNull(createdCollection) },
            { assertEquals(CollectionPrivacy.PUBLIC, createdCollection?.privacy) },
            { assertEquals("https://example.com/public.jpg", createdCollection?.info?.coverImageUrl) }
        )
    }

    @Test
    fun `createCollection - failure - returns unauthorized when not authenticated`() {
        val request = CreateCollectionApiRequest(
            name = "인증 실패 테스트",
            description = "설명"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - validation error when name is blank`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "",
            description = "설명"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - validation error when name exceeds max length`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "a".repeat(101), // 최대 길이 초과
            description = "설명"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - validation error when description exceeds max length`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "컬렉션",
            description = "a".repeat(501) // 최대 길이 초과
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - validation error when visibility is invalid`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "컬렉션",
            description = "설명",
            visibility = "INVALID"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - validation error when cover image url is invalid`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = "invalid-url"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - missing required content type`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "컬렉션",
            description = "설명"
        )

        mockMvc.perform(
            post("/api/collections")
                .content(objectMapper.writeValueAsString(request))
            // .contentType 누락
        )
            .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - failure - does not save collection when validation fails`() {
        val member = testMemberHelper.createActivatedMember()
        val initialCount = collectionRepository.countByOwnerMemberId(member.requireId())

        val request = CreateCollectionApiRequest(
            name = "", // 빈 이름으로 검증 실패
            description = "설명"
        )

        mockMvc.perform(
            post("/api/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        val finalCount = collectionRepository.countByOwnerMemberId(member.requireId())
        assertEquals(initialCount, finalCount)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - returns correct response structure`() {
        testMemberHelper.createActivatedMember()

        val request = CreateCollectionApiRequest(
            name = "응답 구조 테스트",
            description = "응답 구조 확인용",
            coverImageUrl = "https://example.com/test.jpg",
            visibility = "PUBLIC"
        )

        mockMvc.perform(
            post("/api/collections")
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
            .andExpect(jsonPath("$.message").value("컬렉션이 성공적으로 생성되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - handles various valid image url formats`() {
        testMemberHelper.createActivatedMember()

        val validUrls = listOf(
            "https://example.com/image.jpg",
            "http://example.com/image.png",
            "https://cdn.example.com/path/to/image.gif"
        )

        validUrls.forEach { url ->
            val request = CreateCollectionApiRequest(
                name = "이미지 테스트 ${validUrls.indexOf(url)}",
                description = "URL 테스트",
                coverImageUrl = url
            )

            mockMvc.perform(
                post("/api/collections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createCollection - success - handles null and empty cover image url`() {
        testMemberHelper.createActivatedMember()

        val requestWithNull = CreateCollectionApiRequest(
            name = "NULL URL 테스트",
            description = "NULL 테스트",
            coverImageUrl = null
        )
        val requestWithEmpty = CreateCollectionApiRequest(
            name = "빈 URL 테스트",
            description = "빈 값 테스트",
            coverImageUrl = ""
        )

        assertAll(
            {
                mockMvc.perform(
                    post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNull))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
            },
            {
                mockMvc.perform(
                    post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithEmpty))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
            }
        )
    }
}
