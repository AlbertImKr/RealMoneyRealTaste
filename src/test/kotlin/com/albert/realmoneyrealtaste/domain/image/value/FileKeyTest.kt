package com.albert.realmoneyrealtaste.domain.image.value

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@DisplayName("FileKey 값 객체 테스트")
class FileKeyTest {

    @Test
    @DisplayName("FileKey 생성 - 성공 - 기본적인 파일 키")
    fun `constructor - success - creates valid file key`() {
        // Given
        val keyValue = "test.jpg"

        // When
        val fileKey = FileKey(keyValue)

        // Then
        assertEquals(keyValue, fileKey.value)
    }

    @Test
    @DisplayName("FileKey 생성 - 성공 - 다양한 유효한 형식")
    fun `constructor - success - creates file key with various valid formats`() {
        // Given
        val validKeys = listOf(
            "simple.jpg",
            "with-dash.png",
            "with_underscore.webp",
            "complex-name-123.jpg",
            "path/to/file.jpg",
            "path/to/file.png",
            "path/to/file.webp",
            "images/2024/01/01/uuid.jpg",
            "users/123/profile/avatar.jpg",
            "thumbnails/posts/456/thumb.jpg",
            "a.jpg", // 최소 길이
            "very/long/path/with/many/directories/and/long/filename/that/is/still/within/limit.jpg"
        )

        // When & Then
        validKeys.forEach { keyValue ->
            val fileKey = FileKey(keyValue)
            assertEquals(keyValue, fileKey.value)
        }
    }

    @Test
    @DisplayName("FileKey 생성 - 성공 - 최대 길이")
    fun `constructor - success - creates file key with maximum length`() {
        // Given
        val path = "a".repeat(496) // 500 - 4 (".jpg") = 496
        val keyValue = "$path.jpg"

        // When
        val fileKey = FileKey(keyValue)

        // Then
        assertEquals(keyValue, fileKey.value)
        assertEquals(500, keyValue.length)
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 빈 문자열")
    fun `constructor - failure - empty string`() {
        // Given
        val keyValue = ""

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            FileKey(keyValue)
        }
        assertEquals("파일 키는 비어있을 수 없습니다", exception.message)
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 너무 짧은 문자열")
    fun `constructor - failure - too short string`() {
        // Given
        val keyValue = "a"

        // When &  Then
        assertFailsWith<IllegalArgumentException> {
            FileKey(keyValue)
        }
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 공백 문자열")
    fun `constructor - failure - blank string`() {
        // Given
        val blankKeys = listOf(" ", "   ", "\t", "\n")

        // When & Then
        blankKeys.forEach { keyValue ->
            val exception = assertFailsWith<IllegalArgumentException> {
                FileKey(keyValue)
            }
            assertEquals("파일 키는 비어있을 수 없습니다", exception.message)
        }
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 너무 긴 문자열")
    fun `constructor - failure - string too long`() {
        // Given
        val path = "a".repeat(497) // 500 - 3 (".jpg") + 1 = 497
        val keyValue = "$path.jpg" // 501 characters

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            FileKey(keyValue)
        }
        assertEquals("파일 키가 너무 깁니다 (최대 500자)", exception.message)
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 경로 순회 공격")
    fun `constructor - failure - path traversal attack`() {
        // Given
        val maliciousKeys = listOf(
            "../etc/passwd",
            "../../etc/passwd",
            "../../../etc/passwd",
            "folder/../secret.jpg",
            "images/../../config.jpg",
            "sensitive/../database.jpg",
            "normal/../../../etc/passwd.jpg"
        )

        // When & Then
        maliciousKeys.forEach { keyValue ->
            val exception = assertFailsWith<IllegalArgumentException> {
                FileKey(keyValue)
            }
            assertEquals("파일 키에 잘못된 경로 문자가 포함되어 있습니다", exception.message)
        }
    }

    @Test
    @DisplayName("FileKey 생성 - 실패 - 잘못된 형식")
    fun `constructor - failure - invalid format`() {
        // Given
        val invalidKeys = listOf(
            "no-extension", // 확장자 없음
            "file.", // 확장자만 점
            ".hidden", // 숨김 파일
            "file.txt", // 허용되지 않는 확장자
            "file.exe", // 실행 파일
            "file.php", // 스크립트 파일
            "file.jsp", // 웹 스크립트
            "file.asp", // 웹 스크립트
            "file.sh", // 쉘 스크립트
            "file@name.jpg", // 특수문자 @
            "file#name.jpg", // 특수문자 #
            "file name.jpg", // 공백
            "file(name).jpg", // 괄호
            "file+name.jpg", // 플러스
            "file=name.jpg", // 등호
            "file&name.jpg", // 앰퍼샌드
            "file%name.jpg", // 퍼센트
            "file?name.jpg", // 물음표
            "file|name.jpg", // 파이프
            "file<name>.jpg", // 꺽쇠
            "file>name>.jpg", // 꺽쇠
            "file*name.jpg", // 별표
            "file\"name.jpg", // 따옴표
            "file'name.jpg", // 작은따옴표
            "file`name.jpg", // 백틱
            "file~name.jpg", // 틸드
            "file!name.jpg", // 느낌표
            "file^name.jpg", // 캐럿
            "file{name}.jpg", // 중괄호
            "file}name.jpg", // 중괄호
            "file[name].jpg", // 대괄호
            "file]name.jpg", // 대괄호
            "file;name.jpg", // 세미콜론
            "file:name.jpg", // 콜론
            "file,name.jpg", // 쉼표
            "file\\name.jpg" // 백슬래시
        )

        // When & Then
        invalidKeys.forEach { keyValue ->
            val exception = assertFailsWith<IllegalArgumentException> {
                FileKey(keyValue)
            }
            assertEquals("잘못된 파일 키 형식입니다 (영문, 숫자, _, -, / 만 허용되며 확장자가 필요합니다)", exception.message)
        }
    }

    @Test
    @DisplayName("FileKey data class 기능")
    fun `data class features - success - tests equals, hashCode, toString`() {
        // Given
        val fileKey1 = FileKey("test.jpg")
        val fileKey2 = FileKey("test.jpg")
        val fileKey3 = FileKey("different.jpg")

        // When & Then - equals
        assertEquals(fileKey1, fileKey2)
        assertEquals(fileKey1.hashCode(), fileKey2.hashCode())
        assertEquals(fileKey1.toString(), fileKey2.toString())

        // Different values
        assert(fileKey1 != fileKey3)
        assert(fileKey1.hashCode() != fileKey3.hashCode())

        // copy function
        val copiedFileKey = fileKey1.copy(value = "copied.jpg")
        assertEquals("copied.jpg", copiedFileKey.value)
        assertEquals(fileKey1, fileKey2) // 원본은 변경되지 않음
    }

    @Test
    @DisplayName("FileKey 구조 분해 할당")
    fun `destructuring - success - tests component functions`() {
        // Given
        val fileKey = FileKey("test.jpg")

        // When - 구조 분해 할당
        val (value) = fileKey

        // Then
        assertEquals("test.jpg", value)
    }

    @Test
    @DisplayName("FileKey 실제 시나리오")
    fun `real world scenarios - success - tests practical usage patterns`() {
        // Scenario 1: 사용자 프로필 이미지
        val profileFileKey = FileKey("users/123/profile/avatar.jpg")
        assertEquals("users/123/profile/avatar.jpg", profileFileKey.value)

        // Scenario 2: 게시글 이미지
        val postFileKey = FileKey("posts/456/content/image1.jpg")
        assertEquals("posts/456/content/image1.jpg", postFileKey.value)

        // Scenario 3: 썸네일
        val thumbnailFileKey = FileKey("thumbnails/posts/456/thumb.jpg")
        assertEquals("thumbnails/posts/456/thumb.jpg", thumbnailFileKey.value)

        // Scenario 4: 날짜별 폴더 구조
        val dateFileKey = FileKey("images/2024/01/01/uuid-generated-filename.jpg")
        assertEquals("images/2024/01/01/uuid-generated-filename.jpg", dateFileKey.value)

        // Scenario 5: UUID 기반 파일명
        val uuidFileKey = FileKey("uploads/550e8400-e29b-41d4-a716-446655440000.jpg")
        assertEquals("uploads/550e8400-e29b-41d4-a716-446655440000.jpg", uuidFileKey.value)
    }

    @Test
    @DisplayName("FileKey 경계값 테스트")
    fun `boundary values - success - tests edge cases`() {
        // Given & When & Then - 최소 길이
        val minFileKey = FileKey("a.jpg")
        assertEquals("a.jpg", minFileKey.value)
        assertEquals(5, minFileKey.value.length)

        // Given & When & Then - 최대 길이
        val longPath = "a".repeat(496)
        val maxFileKey = FileKey("$longPath.jpg")
        assertEquals(500, maxFileKey.value.length)

        // Given & When & Then - 최대 길이 + 1 (실패)
        val tooLongPath = "a".repeat(497)
        assertFailsWith<IllegalArgumentException> {
            FileKey("$tooLongPath.jpg")
        }
    }

    @Test
    @DisplayName("FileKey 정규식 패턴 검증")
    fun `regex pattern - success - validates allowed characters`() {
        // Given - 허용된 문자들
        val validPatterns = listOf(
            "abc123.jpg", // 영문+숫자
            "file_name.jpg", // 언더스코어
            "file-name.jpg", // 하이픈
            "path/file.jpg", // 슬래시
            "deep/nested/path/file.jpg", // 다중 슬래시
            "123/456/789.jpg", // 숫자 경로
            "A-B_C_D.jpg", // 대문자 포함
            "mixed-CASE_123.jpg", // 대소문자 혼합
            "file.webp", // webp 확장자
            "file.png", // png 확장자
            "file.jpeg", // jpeg 확장자
        )

        // When & Then
        validPatterns.forEach { keyValue ->
            val fileKey = FileKey(keyValue)
            assertEquals(keyValue, fileKey.value)
            assertTrue(keyValue.matches(FileKey.FILE_KEY_PATTERN))
        }
    }
}
