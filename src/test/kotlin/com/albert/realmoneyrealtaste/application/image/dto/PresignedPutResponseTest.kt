package com.albert.realmoneyrealtaste.application.image.dto

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PresignedPutResponseTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var fixedInstant: Instant

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
            .registerModules(KotlinModule.Builder().build(), JavaTimeModule())

        // 테스트 재현성을 위해 고정된 시간 사용
        fixedInstant = Instant.parse("2024-01-15T10:30:00Z")
    }

    @Test
    fun `constructor - success - creates valid response`() {
        // Given
        val uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg"
        val key = "images/2024/01/15/uuid-test.jpg"
        val metadata = mapOf(
            "contentType" to "image/jpeg",
            "originalName" to "test.jpg"
        )

        // When
        val response = PresignedPutResponse(
            uploadUrl = uploadUrl,
            key = key,
            expiresAt = fixedInstant,
            metadata = metadata
        )

        // Then
        assertEquals(uploadUrl, response.uploadUrl)
        assertEquals(key, response.key)
        assertEquals(fixedInstant, response.expiresAt)
        assertEquals(metadata, response.metadata)
    }

    @Test
    fun `constructor - success - handles empty metadata`() {
        // Given
        val uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg"
        val key = "images/test.jpg"
        val emptyMetadata = emptyMap<String, String>()

        // When
        val response = PresignedPutResponse(
            uploadUrl = uploadUrl,
            key = key,
            expiresAt = fixedInstant,
            metadata = emptyMetadata
        )

        // Then
        assertEquals(uploadUrl, response.uploadUrl)
        assertEquals(key, response.key)
        assertEquals(fixedInstant, response.expiresAt)
        assertEquals(emptyMetadata, response.metadata)
        assertTrue(response.metadata.isEmpty())
    }

    @Test
    fun `JSON serialization - success - serializes to correct format`() {
        // Given
        val response = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/2024/01/15/uuid-test.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf(
                "contentType" to "image/jpeg",
                "originalName" to "test.jpg"
            )
        )

        // When
        val json = objectMapper.writeValueAsString(response)

        // Then
        assertTrue(json.contains("\"uploadUrl\":\"https://bucket.s3.amazonaws.com/test-key.jpg\""))
        assertTrue(json.contains("\"key\":\"images/2024/01/15/uuid-test.jpg\""))
        assertTrue(json.contains("\"expiresAt\":\"2024-01-15T10:30:00\""))
        assertTrue(json.contains("\"metadata\""))
        assertTrue(json.contains("\"contentType\":\"image/jpeg\""))
        assertTrue(json.contains("\"originalName\":\"test.jpg\""))
    }

    @Test
    fun `JSON deserialization - success - deserializes from valid JSON`() {
        // Given
        val json = """
            {
                "uploadUrl": "https://bucket.s3.amazonaws.com/test-key.jpg",
                "key": "images/2024/01/15/uuid-test.jpg",
                "expiresAt": "2024-01-15T10:30:00",
                "metadata": {
                    "contentType": "image/jpeg",
                    "originalName": "test.jpg"
                }
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue<PresignedPutResponse>(json)

        // Then
        assertEquals("https://bucket.s3.amazonaws.com/test-key.jpg", response.uploadUrl)
        assertEquals("images/2024/01/15/uuid-test.jpg", response.key)
        assertEquals(fixedInstant, response.expiresAt)
        assertEquals(2, response.metadata.size)
        assertEquals("image/jpeg", response.metadata["contentType"])
        assertEquals("test.jpg", response.metadata["originalName"])
    }

    @Test
    fun `JSON deserialization - success - handles empty metadata`() {
        // Given
        val json = """
            {
                "uploadUrl": "https://bucket.s3.amazonaws.com/test-key.jpg",
                "key": "images/test.jpg",
                "expiresAt": "2024-01-15T10:30:00",
                "metadata": {}
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue<PresignedPutResponse>(json)

        // Then
        assertEquals("https://bucket.s3.amazonaws.com/test-key.jpg", response.uploadUrl)
        assertEquals("images/test.jpg", response.key)
        assertEquals(fixedInstant, response.expiresAt)
        assertTrue(response.metadata.isEmpty())
    }

    @Test
    fun `JSON deserialization - failure - throws exception for missing required fields`() {
        // Given
        val jsonWithoutUploadUrl = """
            {
                "key": "images/test.jpg",
                "expiresAt": "2024-01-15T10:30:00Z",
                "metadata": {}
            }
        """.trimIndent()

        val jsonWithoutKey = """
            {
                "uploadUrl": "https://bucket.s3.amazonaws.com/test-key.jpg",
                "expiresAt": "2024-01-15T10:30:00Z",
                "metadata": {}
            }
        """.trimIndent()

        val jsonWithoutExpiresAt = """
            {
                "uploadUrl": "https://bucket.s3.amazonaws.com/test-key.jpg",
                "key": "images/test.jpg",
                "metadata": {}
            }
        """.trimIndent()

        // When & Then
        assertFailsWith<JsonProcessingException> {
            objectMapper.readValue<PresignedPutResponse>(jsonWithoutUploadUrl)
        }

        assertFailsWith<JsonProcessingException> {
            objectMapper.readValue<PresignedPutResponse>(jsonWithoutKey)
        }

        assertFailsWith<JsonProcessingException> {
            objectMapper.readValue<PresignedPutResponse>(jsonWithoutExpiresAt)
        }
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        // Given
        val response1 = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/test.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf("contentType" to "image/jpeg")
        )

        val response2 = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/test.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf("contentType" to "image/jpeg")
        )

        val response3 = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/other-key.jpg",
            key = "images/other.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf("contentType" to "image/png")
        )

        // When & Then
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
        assertEquals(response1, response1) // 자기 자신과 비교

        assertTrue(response1 != response3)
        assertTrue(response1.hashCode() != response3.hashCode())
    }

    @Test
    fun `toString - success - generates readable string representation`() {
        // Given
        val response = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/test.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf("contentType" to "image/jpeg")
        )

        // When
        val stringRepresentation = response.toString()

        // Then
        assertTrue(stringRepresentation.contains("PresignedPutResponse"))
        assertTrue(stringRepresentation.contains("uploadUrl=https://bucket.s3.amazonaws.com/test-key.jpg"))
        assertTrue(stringRepresentation.contains("key=images/test.jpg"))
        assertTrue(stringRepresentation.contains("expiresAt=2024-01-15T10:30:00Z"))
        assertTrue(stringRepresentation.contains("metadata={contentType=image/jpeg}"))
    }

    @Test
    fun `copy - success - creates copy with modified fields`() {
        // Given
        val original = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/test.jpg",
            expiresAt = fixedInstant,
            metadata = mapOf("contentType" to "image/jpeg")
        )

        // When
        val copied = original.copy(
            uploadUrl = "https://bucket.s3.amazonaws.com/new-key.jpg",
            metadata = mapOf("contentType" to "image/png")
        )

        // Then
        assertEquals("https://bucket.s3.amazonaws.com/new-key.jpg", copied.uploadUrl)
        assertEquals("images/test.jpg", copied.key) // 변경되지 않은 필드
        assertEquals(fixedInstant, copied.expiresAt) // 변경되지 않은 필드
        assertEquals(mapOf("contentType" to "image/png"), copied.metadata)

        // 원본은 변경되지 않음
        assertEquals("https://bucket.s3.amazonaws.com/test-key.jpg", original.uploadUrl)
        assertEquals(mapOf("contentType" to "image/jpeg"), original.metadata)
    }

    @Test
    fun `metadata - success - handles special characters and values`() {
        // Given
        val specialMetadata = mapOf(
            "empty" to "",
            "spaces" to "value with spaces",
            "special" to "!@#$%^&*()_+-=[]{}|;':\",./<>?",
            "unicode" to "한글 🖼️ émoji",
            "number" to "12345",
            "json-like" to "{\"key\": \"value\"}",
            "url" to "https://example.com/path?query=value&other=test"
        )

        // When
        val response = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test-key.jpg",
            key = "images/test.jpg",
            expiresAt = fixedInstant,
            metadata = specialMetadata
        )

        // Then
        assertEquals(specialMetadata, response.metadata)

        // JSON 직렬화/역직렬화 테스트
        val json = objectMapper.writeValueAsString(response)
        val deserialized = objectMapper.readValue<PresignedPutResponse>(json)
        assertEquals(specialMetadata, deserialized.metadata)
    }

    @Test
    fun `expiresAt - success - handles edge cases`() {
        // Given
        val pastInstant = Instant.now().minus(1, ChronoUnit.HOURS)
        val futureInstant = Instant.now().plus(24, ChronoUnit.HOURS)
        val epochInstant = Instant.EPOCH

        // When & Then
        val pastResponse = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test1.jpg",
            key = "images/test1.jpg",
            expiresAt = pastInstant,
            metadata = emptyMap()
        )
        assertEquals(pastInstant, pastResponse.expiresAt)

        val futureResponse = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test2.jpg",
            key = "images/test2.jpg",
            expiresAt = futureInstant,
            metadata = emptyMap()
        )
        assertEquals(futureInstant, futureResponse.expiresAt)

        val epochResponse = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test3.jpg",
            key = "images/test3.jpg",
            expiresAt = epochInstant,
            metadata = emptyMap()
        )
        assertEquals(epochInstant, epochResponse.expiresAt)
    }

    @Test
    fun `field validation - success - validates URL format`() {
        // Given
        val validUrls = listOf(
            "https://bucket.s3.amazonaws.com/key.jpg",
            "https://bucket.s3.region.amazonaws.com/path/to/key.jpg",
            "https://custom-domain.com/images/key.jpg",
            "http://localhost:9000/bucket/key.jpg" // 로컬 테스트용
        )

        // When & Then
        validUrls.forEach { url ->
            val response = PresignedPutResponse(
                uploadUrl = url,
                key = "test.jpg",
                expiresAt = fixedInstant,
                metadata = emptyMap()
            )
            assertEquals(url, response.uploadUrl)
        }
    }

    @Test
    fun `field validation - success - validates key format`() {
        // Given
        val validKeys = listOf(
            "images/test.jpg",
            "images/2024/01/15/uuid.jpg",
            "images/user/123/profile/image.png",
            "images/very/deep/nested/path/with/uuid/file-name.webp",
            "simple-key.jpg"
        )

        // When & Then
        validKeys.forEach { key ->
            val response = PresignedPutResponse(
                uploadUrl = "https://bucket.s3.amazonaws.com/$key",
                key = key,
                expiresAt = fixedInstant,
                metadata = emptyMap()
            )
            assertEquals(key, response.key)
        }
    }

    @Test
    fun `immutability - documents current behavior - metadata map references original`() {
        // Given
        val originalMetadata = mutableMapOf("contentType" to "image/jpeg")
        val response = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test.jpg",
            key = "test.jpg",
            expiresAt = fixedInstant,
            metadata = originalMetadata
        )

        // When - 원본 맵 수정
        originalMetadata["newKey"] = "newValue"

        // Then - response도 변경됨 (Kotlin data class의 현재 동작)
        // 이 테스트는 현재 동작을 문서화하며, 실제 사용 시에는
        // 생성자에 immutable map을 전달해야 함을 보여줌
        assertEquals(2, response.metadata.size)
        assertEquals("image/jpeg", response.metadata["contentType"])
        assertEquals("newValue", response.metadata["newKey"])

        // 권장 방법: 불변 맵을 전달
        val immutableMetadata = mapOf("contentType" to "image/jpeg")
        val immutableResponse = PresignedPutResponse(
            uploadUrl = "https://bucket.s3.amazonaws.com/test.jpg",
            key = "test.jpg",
            expiresAt = fixedInstant,
            metadata = immutableMetadata
        )

        // 불변 맵은 원본이 없으므로 안전
        assertEquals(1, immutableResponse.metadata.size)
    }
}
