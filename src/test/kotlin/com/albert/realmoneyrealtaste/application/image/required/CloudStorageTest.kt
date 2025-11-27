package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CloudStorageTest(
    val cloudStorage: CloudStorage,
) : IntegrationTestBase() {

    @Test
    fun `generatePresignedUrl - success - generates valid presigned URL for simple key`() {
        // Given
        val fileKey = "images/test.jpg"

        // When
        val presignedUrl = cloudStorage.generatePresignedUrl(fileKey)

        // Then
        assertNotNull(presignedUrl)
        assertTrue(presignedUrl.isNotEmpty())
        assertTrue(presignedUrl.contains(fileKey))

        // Presigned URL 특성 확인
        assertTrue(presignedUrl.contains("X-Amz-Signature"))
        assertTrue(presignedUrl.contains("X-Amz-Expires"))
        assertTrue(presignedUrl.contains("X-Amz-Credential"))
        assertTrue(presignedUrl.contains("X-Amz-Date"))
        assertTrue(presignedUrl.contains("X-Amz-Algorithm"))
    }

    @Test
    fun `generatePresignedUrl - success - generates valid presigned URL for complex key`() {
        // Given
        val fileKey = "images/2024/01/01/${UUID.randomUUID()}/nested/path/test-image.jpg"

        // When
        val presignedUrl = cloudStorage.generatePresignedUrl(fileKey)

        // Then
        assertNotNull(presignedUrl)
        assertTrue(presignedUrl.isNotEmpty())
        assertTrue(presignedUrl.contains(fileKey))
        assertTrue(presignedUrl.contains("X-Amz-Signature"))
    }

    @Test
    fun `generatePresignedUrl - success - generates different URLs for different keys`() {
        // Given
        val key1 = "images/test1.jpg"
        val key2 = "images/test2.png"

        // When
        val url1 = cloudStorage.generatePresignedUrl(key1)
        val url2 = cloudStorage.generatePresignedUrl(key2)

        // Then
        assertTrue(url1 != url2)
        assertTrue(url1.contains(key1))
        assertTrue(url2.contains(key2))
        assertTrue(url1.contains("X-Amz-Signature"))
        assertTrue(url2.contains("X-Amz-Signature"))
    }

    @Test
    fun `generatePresignedUrl - success - handles various file extensions`() {
        // Given
        val testCases = listOf(
            "image.jpg",
            "photo.png",
            "graphic.webp",
            "picture.jpeg",
            "avatar.JPG",
            "banner.PNG",
            "thumbnail.WEBP"
        )

        // When & Then
        testCases.forEach { fileName ->
            val fileKey = "images/$fileName"
            val presignedUrl = cloudStorage.generatePresignedUrl(fileKey)

            assertNotNull(presignedUrl)
            assertTrue(presignedUrl.contains(fileKey))
            assertTrue(presignedUrl.contains("X-Amz-Signature"))
            assertTrue(presignedUrl.contains("X-Amz-Expires"))
        }
    }

    @Test
    fun `generatePresignedUrl - success - handles UUID-based keys`() {
        // Given
        val uuid = UUID.randomUUID()
        val uuidKey = "images/2024/01/01/$uuid/test.jpg"

        // When
        val presignedUrl = cloudStorage.generatePresignedUrl(uuidKey)

        // Then
        assertNotNull(presignedUrl)
        assertTrue(presignedUrl.contains(uuid.toString()))
        assertTrue(presignedUrl.contains(uuidKey))
        assertTrue(presignedUrl.contains("X-Amz-Signature"))
    }

    @Test
    fun `generatePresignedUrl - success - handles deeply nested keys`() {
        // Given
        val deepKey = "images/2024/01/01/${UUID.randomUUID()}/very/deep/nested/directory/structure/test.jpg"

        // When
        val presignedUrl = cloudStorage.generatePresignedUrl(deepKey)

        // Then
        assertNotNull(presignedUrl)
        assertTrue(presignedUrl.contains(deepKey))
        assertTrue(presignedUrl.contains("X-Amz-Signature"))
    }

    @Test
    fun `generatePresignedUrl - success - validates presigned URL structure`() {
        // Given
        val fileKey = "images/test.jpg"

        // When
        val presignedUrl = cloudStorage.generatePresignedUrl(fileKey)

        // Then
        // 필수 AWS 파라미터
        assertTrue(presignedUrl.contains("X-Amz-Algorithm=AWS4-HMAC-SHA256"))
        assertTrue(presignedUrl.contains("X-Amz-Credential="))
        assertTrue(presignedUrl.contains("X-Amz-Date="))
        assertTrue(presignedUrl.contains("X-Amz-Expires="))
        assertTrue(presignedUrl.contains("X-Amz-SignedHeaders="))
        assertTrue(presignedUrl.contains("X-Amz-Signature="))

        // 만료 시간 확인 (기본 1시간 = 3600초)
        assertTrue(presignedUrl.contains("X-Amz-Expires=3600"))
    }

    @Test
    fun `generatePresignedUrl - success - boundary values`() {
        // Given - 최소 길이 키
        val minKey = "a.jpg"

        // When
        val minUrl = cloudStorage.generatePresignedUrl(minKey)

        // Then
        assertNotNull(minUrl)
        assertTrue(minUrl.contains(minKey))
        assertTrue(minUrl.contains("X-Amz-Signature"))

        // Given - 매우 긴 키
        val longKey = "images/" + "a".repeat(200) + ".jpg"

        // When
        val longUrl = cloudStorage.generatePresignedUrl(longKey)

        // Then
        assertNotNull(longUrl)
        assertTrue(longUrl.contains(longKey))
        assertTrue(longUrl.contains("X-Amz-Signature"))
    }

    @Test
    fun `delete - success - deletes file without exception`() {
        // Given
        val fileKey = "images/test-delete.jpg"

        // When & Then - 예외 발생하지 않음
        cloudStorage.delete(fileKey)
    }

    @Test
    fun `delete - success - deletes complex key without exception`() {
        // Given
        val complexKey = "images/2024/01/01/${UUID.randomUUID()}/nested/path/test-delete.jpg"

        // When & Then - 예외 발생하지 않음
        cloudStorage.delete(complexKey)
    }

    @Test
    fun `delete - success - handles various file types`() {
        // Given
        val testCases = listOf(
            "images/test1.jpg",
            "images/test2.png",
            "images/test3.webp",
            "images/2024/01/01/test4.jpeg",
            "images/deep/nested/test5.JPG"
        )

        // When & Then - 모두 예외 발생하지 않음
        testCases.forEach { fileKey ->
            cloudStorage.delete(fileKey)
        }
    }

    @Test
    fun `delete - success - boundary values`() {
        // Given - 최소 길이 키
        val minKey = "a.jpg"

        // When & Then - 예외 발생하지 않음
        cloudStorage.delete(minKey)

        // Given - 매우 긴 키
        val longKey = "images/" + "a".repeat(200) + ".jpg"

        // When & Then - 예외 발생하지 않음
        cloudStorage.delete(longKey)
    }

    @Test
    fun `CloudStorage interface - mock implementation test`() {
        // Given
        val mockStorage: CloudStorage = object : CloudStorage {
            override fun generatePresignedUrl(fileKey: String): String {
                return "https://mock-storage.example.com/$fileKey?signature=mock"
            }

            override fun delete(fileKey: String) {
                // Mock delete operation
            }
        }

        val fileKey = "test/image.jpg"

        // When
        val url = mockStorage.generatePresignedUrl(fileKey)

        // Then
        assertEquals("https://mock-storage.example.com/test/image.jpg?signature=mock", url)

        // When & Then - delete 예외 발생하지 않음
        mockStorage.delete(fileKey)
    }

    @Test
    fun `CloudStorage interface - lambda style implementation`() {
        // Given
        val generatedUrls = mutableListOf<String>()
        val deletedKeys = mutableListOf<String>()

        val lambdaStorage: CloudStorage = object : CloudStorage {
            override fun generatePresignedUrl(fileKey: String): String {
                val url = "https://lambda-storage.example.com/$fileKey?signature=${UUID.randomUUID()}"
                generatedUrls.add(url)
                return url
            }

            override fun delete(fileKey: String) {
                deletedKeys.add(fileKey)
            }
        }

        val fileKey = "test/image.jpg"

        // When
        val url = lambdaStorage.generatePresignedUrl(fileKey)
        lambdaStorage.delete(fileKey)

        // Then
        assertTrue(url.contains("lambda-storage.example.com"))
        assertTrue(url.contains(fileKey))
        assertTrue(url.contains("signature="))
        assertEquals(1, generatedUrls.size)
        assertEquals(url, generatedUrls[0])
        assertEquals(1, deletedKeys.size)
        assertEquals(fileKey, deletedKeys[0])
    }

    @Test
    fun `CloudStorage interface - exception handling simulation`() {
        // Given
        val exceptionStorage: CloudStorage = object : CloudStorage {
            override fun generatePresignedUrl(fileKey: String): String {
                if (fileKey.contains("invalid")) {
                    throw IllegalArgumentException("Invalid file key: $fileKey")
                }
                return "https://example.com/$fileKey"
            }

            override fun delete(fileKey: String) {
                if (fileKey.contains("protected")) {
                    throw IllegalStateException("Cannot delete protected file: $fileKey")
                }
            }
        }

        // When & Then - 성공 케이스
        val successUrl = exceptionStorage.generatePresignedUrl("valid/file.jpg")
        assertEquals("https://example.com/valid/file.jpg", successUrl)

        exceptionStorage.delete("normal/file.jpg") // 예외 발생하지 않음

        // When & Then - 예외 케이스
        try {
            exceptionStorage.generatePresignedUrl("invalid/file.jpg")
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid file key: invalid/file.jpg", e.message)
        }

        try {
            exceptionStorage.delete("protected/file.jpg")
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: IllegalStateException) {
            assertEquals("Cannot delete protected file: protected/file.jpg", e.message)
        }
    }

    @Test
    fun `generatePresignedUrl - success - handles special characters in key`() {
        // Given
        val specialKeys = listOf(
            "images/file_name.jpg",
            "images/file-name.png",
            "images/file.123.jpg",
            "images/123.jpg",
            "images/ALL_CAPS.JPG",
            "images/mixed_Case-Name.jpg"
        )

        // When & Then
        specialKeys.forEach { fileKey ->
            val presignedUrl = cloudStorage.generatePresignedUrl(fileKey)

            assertNotNull(presignedUrl)
            assertTrue(presignedUrl.contains(fileKey))
            assertTrue(presignedUrl.contains("X-Amz-Signature"))
        }
    }

    @Test
    fun `delete - success - handles special characters in key`() {
        // Given
        val specialKeys = listOf(
            "images/file_name.jpg",
            "images/file-name.png",
            "images/file.123.jpg",
            "images/123.jpg",
            "images/ALL_CAPS.JPG",
            "images/mixed_Case-Name.jpg"
        )

        // When & Then - 모두 예외 발생하지 않음
        specialKeys.forEach { fileKey ->
            cloudStorage.delete(fileKey)
        }
    }
}
