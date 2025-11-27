package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageUrlGeneratorTest(
    val imageUrlGenerator: ImageUrlGenerator,
) : IntegrationTestBase() {

    @Test
    fun `generateImageUrl - success - generates valid S3 URL for simple key`() {
        // Given
        val key = "images/test.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(key)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.startsWith("https://"))
        assertTrue(imageUrl.contains("amazonaws.com"))
        assertTrue(imageUrl.contains(key))
        assertTrue(imageUrl.endsWith("/$key"))
    }

    @Test
    fun `generateImageUrl - success - generates valid S3 URL for complex key`() {
        // Given
        val key = "images/2024/01/01/${UUID.randomUUID()}/nested/path/test-image.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(key)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.startsWith("https://"))
        assertTrue(imageUrl.contains("amazonaws.com"))
        assertTrue(imageUrl.contains(key))
        assertTrue(imageUrl.endsWith("/$key"))
    }

    @Test
    fun `generateImageUrl - success - generates different URLs for different keys`() {
        // Given
        val key1 = "images/test1.jpg"
        val key2 = "images/test2.png"

        // When
        val url1 = imageUrlGenerator.generateImageUrl(key1)
        val url2 = imageUrlGenerator.generateImageUrl(key2)

        // Then
        assertTrue(url1 != url2)
        assertTrue(url1.contains(key1))
        assertTrue(url2.contains(key2))
        assertTrue(url1.endsWith("/$key1"))
        assertTrue(url2.endsWith("/$key2"))
    }

    @Test
    fun `generateImageUrl - success - handles various file extensions`() {
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
            val key = "images/$fileName"
            val imageUrl = imageUrlGenerator.generateImageUrl(key)

            assertNotNull(imageUrl)
            assertTrue(imageUrl.contains(key))
            assertTrue(imageUrl.endsWith("/$key"))
        }
    }

    @Test
    fun `generateImageUrl - success - handles special characters in key`() {
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
        specialKeys.forEach { key ->
            val imageUrl = imageUrlGenerator.generateImageUrl(key)

            assertNotNull(imageUrl)
            assertTrue(imageUrl.contains(key))
            assertTrue(imageUrl.endsWith("/$key"))
        }
    }

    @Test
    fun `generateImageUrl - success - handles UUID-based keys`() {
        // Given
        val uuid = UUID.randomUUID()
        val uuidKey = "images/2024/01/01/$uuid/test.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(uuidKey)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.contains(uuid.toString()))
        assertTrue(imageUrl.contains(uuidKey))
        assertTrue(imageUrl.endsWith("/$uuidKey"))
    }

    @Test
    fun `generateImageUrl - success - handles deeply nested keys`() {
        // Given
        val deepKey = "images/2024/01/01/${UUID.randomUUID()}/very/deep/nested/directory/structure/test.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(deepKey)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.contains(deepKey))
        assertTrue(imageUrl.endsWith("/$deepKey"))
    }

    @Test
    fun `generateImageUrl - success - maintains URL format consistency`() {
        // Given
        val key = "images/test.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(key)

        // Then
        // URL 형식: https://bucket-name.s3.region.amazonaws.com/key
        assertTrue(imageUrl.matches(Regex("^https://[a-zA-Z0-9.-]+\\.s3\\.[a-zA-Z0-9-]+\\.amazonaws\\.com/.+$")))

        // HTTPS 프로토콜 사용
        assertTrue(imageUrl.startsWith("https://"))

        // S3 도메인 포함
        assertTrue(imageUrl.contains(".s3."))
        assertTrue(imageUrl.contains(".amazonaws.com"))

        // 키가 마지막에 위치
        assertTrue(imageUrl.endsWith("/$key"))
    }

    @Test
    fun `generateImageUrl - success - boundary values`() {
        // Given - 최소 길이 키
        val minKey = "a.jpg"

        // When
        val minImageUrl = imageUrlGenerator.generateImageUrl(minKey)

        // Then
        assertNotNull(minImageUrl)
        assertTrue(minImageUrl.contains(minKey))
        assertTrue(minImageUrl.endsWith("/$minKey"))

        // Given - 매우 긴 키
        val longKey = "images/" + "a".repeat(200) + ".jpg"

        // When
        val longImageUrl = imageUrlGenerator.generateImageUrl(longKey)

        // Then
        assertNotNull(longImageUrl)
        assertTrue(longImageUrl.contains(longKey))
        assertTrue(longImageUrl.endsWith("/$longKey"))
    }

    @Test
    fun `generateImageUrl - success - handles empty path segments`() {
        // Given
        val keyWithEmptySegments = "images//2024//01//01//test.jpg"

        // When
        val imageUrl = imageUrlGenerator.generateImageUrl(keyWithEmptySegments)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.contains(keyWithEmptySegments))
        assertTrue(imageUrl.endsWith("/$keyWithEmptySegments"))
    }

    @Test
    fun `generateImageUrl - success - consistent URL generation`() {
        // Given
        val key = "images/consistent.jpg"

        // When
        val url1 = imageUrlGenerator.generateImageUrl(key)
        val url2 = imageUrlGenerator.generateImageUrl(key)

        // Then
        assertEquals(url1, url2) // 동일한 키에 대해 동일한 URL
        assertTrue(url1.contains(key))
        assertTrue(url2.contains(key))
    }
}
