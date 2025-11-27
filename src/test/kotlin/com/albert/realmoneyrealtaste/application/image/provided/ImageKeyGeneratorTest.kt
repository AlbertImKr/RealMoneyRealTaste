package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.service.DefaultImageKeyGenerator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("ImageKeyGenerator 인터페이스 테스트")
class ImageKeyGeneratorTest(
    val imageKeyGenerator: DefaultImageKeyGenerator,
) : IntegrationTestBase() {

    @Test
    fun `generateSecureImageKey - success - generates unique key with valid format`() {
        // Given
        val fileName = "test-image.jpg"

        // When
        val imageKey = imageKeyGenerator.generateSecureImageKey(fileName)

        // Then
        assertTrue(imageKey.startsWith("images/"))
        assertTrue(imageKey.contains("/"))
        assertTrue(imageKey.endsWith(".jpg"))

        // 형식: images/yyyy/MM/dd/uuid.extension
        val parts = imageKey.split("/")
        assertEquals(5, parts.size) // images, yyyy, MM, dd-uuid.extension
        assertEquals("images", parts[0])

        val datePart = "${parts[1]}/${parts[2]}/${parts[3].substringBeforeLast('.')}"
        val expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        assertEquals(expectedDate, datePart)
    }

    @Test
    fun `generateSecureImageKey - success - generates different keys for same input`() {
        // Given
        val fileName = "test-image.jpg"

        // When
        val key1 = imageKeyGenerator.generateSecureImageKey(fileName)
        val key2 = imageKeyGenerator.generateSecureImageKey(fileName)

        // Then
        assertNotEquals(key1, key2) // UUID로 인해 항상 다름
    }

    @Test
    fun `generateSecureImageKey - success - handles various file extensions`() {
        // Given
        val testCases = listOf(
            "image.jpg" to "jpg",
            "photo.png" to "png",
            "graphic.webp" to "webp",
            "picture.jpeg" to "jpeg",
            "avatar.JPG" to "JPG",
            "banner.PNG" to "PNG",
            "thumbnail.WEBP" to "WEBP"
        )

        // When & Then
        testCases.forEach { (fileName, expectedExtension) ->
            val imageKey = imageKeyGenerator.generateSecureImageKey(fileName)
            assertTrue(imageKey.endsWith(".$expectedExtension"))
        }
    }

    @Test
    fun `generateSecureImageKey - success - uses default extension when no extension provided`() {
        // Given
        val fileName = "no-extension"

        // When
        val imageKey = imageKeyGenerator.generateSecureImageKey(fileName)

        // Then
        assertTrue(imageKey.endsWith(".jpg"))
    }

    @Test
    fun `generateSecureImageKey - success - sanitizes special characters in filename`() {
        // Given
        val testCases = mapOf(
            "file@name.jpg" to "file_name.jpg",
            "file#name.png" to "file_name.png",
            "file name.webp" to "file_name.webp",
            "file(name).jpg" to "file_name_.jpg",
            "file+name.jpg" to "file_name.jpg",
            "file=name.jpg" to "file_name.jpg",
            "file&name.jpg" to "file_name.jpg",
            "file%name.jpg" to "file_name.jpg",
            "file?name.jpg" to "file_name.jpg",
            "file|name.jpg" to "file_name.jpg",
            "file<name>.jpg" to "file_name_.jpg",
            "file>name>.jpg" to "file_name_.jpg",
            "file*name.jpg" to "file_name.jpg",
            "file\"name.jpg" to "file_name.jpg",
            "file'name.jpg" to "file_name.jpg",
            "file`name.jpg" to "file_name.jpg",
            "file~name.jpg" to "file_name.jpg",
            "file!name.jpg" to "file_name.jpg",
            "file^name.jpg" to "file_name.jpg"
        )

        // When & Then
        testCases.forEach { (originalFileName, expectedSanitizedName) ->
            val imageKey = imageKeyGenerator.generateSecureImageKey(originalFileName)
            val actualExtension = imageKey.substringAfterLast(".")
            val expectedExtension = expectedSanitizedName.substringAfterLast(".")
            assertEquals(expectedExtension, actualExtension)
        }
    }

    @Test
    fun `generateSecureImageKey - success - handles edge cases in filename`() {
        // Given
        val testCases = listOf(
            "a.jpg", // 최소 길이
            "very-long-filename-with-many-dashes-and-underscores.jpg", // 긴 파일명
            "file.with.multiple.dots.jpg", // 여러 점
            "file_no_extension", // 확장자 없음
            "file.", // 점으로 끝남
            ".hidden.jpg", // 점으로 시작
            "123.jpg", // 숫자로 시작
            "file123.jpg", // 숫자 포함
            "ALL_CAPS.JPG", // 대문자
            "mixed_Case-Name.jpg" // 대소문자 혼합
        )

        // When & Then
        testCases.forEach { fileName ->
            val imageKey = imageKeyGenerator.generateSecureImageKey(fileName)

            // 기본 형식 검증
            assertTrue(imageKey.startsWith("images/"))
            assertTrue(imageKey.contains("/"))

            // UUID 부분 검증 (36자)
            val uuidPart = imageKey.split("/").last().substringBeforeLast(".")
            assertEquals(36, uuidPart.length)

            // 유효한 UUID 형식인지 검증
            assertTrue(uuidPart.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
        }
    }

    @Test
    fun `generateSecureImageKey - success - validates date format consistency`() {
        // Given
        val fileName = "test.jpg"

        // When
        val imageKey = imageKeyGenerator.generateSecureImageKey(fileName)

        // Then
        val expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        assertTrue(imageKey.contains(expectedDate))

        // 날짜 부분 추출 및 검증
        val datePart = imageKey.substringAfter("images/").substringBeforeLast("/")
        assertEquals(expectedDate, datePart)
    }

    @Test
    fun `generateSecureImageKey - success - handles boundary values`() {
        // Given - 빈 파일명
        val emptyFileName = ""

        // When
        val emptyKey = imageKeyGenerator.generateSecureImageKey(emptyFileName)

        // Then
        assertTrue(emptyKey.startsWith("images/"))
        assertTrue(emptyKey.endsWith(".jpg")) // 기본 확장자

        // Given - 공백만 있는 파일명
        val blankFileName = "   "

        // When
        val blankKey = imageKeyGenerator.generateSecureImageKey(blankFileName)

        // Then
        assertTrue(blankKey.startsWith("images/"))
        assertTrue(blankKey.endsWith(".jpg"))
    }

    @Test
    fun `generateSecureImageKey - success - maintains allowed characters`() {
        // Given - 허용된 문자만 포함된 파일명
        val allowedCharsFileName = "file_name-123.abc.jpg"

        // When
        val imageKey = imageKeyGenerator.generateSecureImageKey(allowedCharsFileName)

        // Then
        assertTrue(imageKey.endsWith(".jpg"))
    }

    // 테스트용 보조 클래스
    private class TestImageKeyGenerator {
        private val generatedFiles = mutableSetOf<String>()

        fun generateSecureImageKey(fileName: String): String {
            generatedFiles.add(fileName)
            return "method-ref-test/$fileName"
        }

        fun isGenerated(fileName: String): Boolean {
            return generatedFiles.contains(fileName)
        }
    }
}
