package com.albert.realmoneyrealtaste.adapter.webapi.collection.request

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectionUpdateApiRequestTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `toServiceDto - success - converts to CollectionUpdateRequest with valid parameters`() {
        val request = CollectionUpdateApiRequest(
            name = "수정된 컬렉션",
            description = "수정된 설명",
            coverImageUrl = "https://example.com/updated.jpg"
        )

        val serviceDto = request.toServiceDto(collectionId = 123L, ownerMemberId = 456L)

        assertAll(
            { assertEquals(123L, serviceDto.collectionId) },
            { assertEquals(456L, serviceDto.ownerMemberId) },
            { assertEquals("수정된 컬렉션", serviceDto.newInfo.name) },
            { assertEquals("수정된 설명", serviceDto.newInfo.description) },
            { assertEquals("https://example.com/updated.jpg", serviceDto.newInfo.coverImageUrl) }
        )
    }

    @Test
    fun `toServiceDto - success - converts with default values`() {
        val request = CollectionUpdateApiRequest(
            name = "기본값 테스트"
        )

        val serviceDto = request.toServiceDto(collectionId = 789L, ownerMemberId = 101L)

        assertAll(
            { assertEquals(789L, serviceDto.collectionId) },
            { assertEquals(101L, serviceDto.ownerMemberId) },
            { assertEquals("기본값 테스트", serviceDto.newInfo.name) },
            { assertEquals("", serviceDto.newInfo.description) },
            { assertNull(serviceDto.newInfo.coverImageUrl) }
        )
    }

    @Test
    fun `toServiceDto - success - trims whitespace from inputs`() {
        val request = CollectionUpdateApiRequest(
            name = "  수정된 이름  ",
            description = "  수정된 설명  ",
            coverImageUrl = "  https://example.com/image.jpg  "
        )

        val serviceDto = request.toServiceDto(collectionId = 1L, ownerMemberId = 2L)

        assertAll(
            { assertEquals("수정된 이름", serviceDto.newInfo.name) },
            { assertEquals("수정된 설명", serviceDto.newInfo.description) },
            { assertEquals("https://example.com/image.jpg", serviceDto.newInfo.coverImageUrl) }
        )
    }

    @Test
    fun `toServiceDto - success - handles empty cover image url`() {
        val requestWithEmpty = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = ""
        )
        val requestWithBlank = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = "   "
        )

        assertAll(
            { assertNull(requestWithEmpty.toServiceDto(1L, 2L).newInfo.coverImageUrl) },
            { assertNull(requestWithBlank.toServiceDto(1L, 2L).newInfo.coverImageUrl) }
        )
    }

    @Test
    fun `validation - success - passes with valid inputs`() {
        val request = CollectionUpdateApiRequest(
            name = "유효한 컬렉션",
            description = "유효한 설명",
            coverImageUrl = "https://example.com/valid.jpg"
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - success - passes with minimal required fields`() {
        val request = CollectionUpdateApiRequest(
            name = "최소 컬렉션"
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects blank name`() {
        val request = CollectionUpdateApiRequest(
            name = " "
        )

        val violations = validator.validate(request)
        val nameViolation = findViolationByProperty(violations, "name")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(nameViolation) },
            { assertEquals("컬렉션 이름은 비어 있을 수 없습니다.", nameViolation?.message) }
        )
    }

    @Test
    fun `validation - failure - detects whitespace-only name`() {
        val request = CollectionUpdateApiRequest(
            name = "   "
        )

        val violations = validator.validate(request)
        val nameViolation = findViolationByProperty(violations, "name")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(nameViolation) },
            { assertEquals("컬렉션 이름은 비어 있을 수 없습니다.", nameViolation?.message) }
        )
    }

    @Test
    fun `validation - failure - detects name exceeding max length`() {
        val request = CollectionUpdateApiRequest(
            name = "a".repeat(101) // 100자 초과
        )

        val violations = validator.validate(request)
        val nameViolation = findViolationByProperty(violations, "name")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(nameViolation) },
            { assertEquals("컬렉션 이름은 100자를 초과할 수 없습니다.", nameViolation?.message) }
        )
    }

    @Test
    fun `validation - success - accepts name at max length`() {
        val request = CollectionUpdateApiRequest(
            name = "a".repeat(100) // 정확히 100자
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects description exceeding max length`() {
        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "a".repeat(501) // 500자 초과
        )

        val violations = validator.validate(request)
        val descViolation = findViolationByProperty(violations, "description")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(descViolation) },
            { assertEquals("컬렉션 설명은 500자를 초과할 수 없습니다.", descViolation?.message) }
        )
    }

    @Test
    fun `validation - success - accepts description at max length`() {
        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "a".repeat(500) // 정확히 500자
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - success - accepts empty description`() {
        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = ""
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects cover image url exceeding max length`() {
        val longUrl = "https://example.com/" + "a".repeat(481) + ".jpg" // 총 500자 초과
        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            coverImageUrl = longUrl
        )

        val violations = validator.validate(request)
        val urlViolation = findViolationByProperty(violations, "coverImageUrl")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(urlViolation) },
            { assertEquals("커버 이미지 URL은 500자를 초과할 수 없습니다.", urlViolation?.message) }
        )
    }

    @Test
    fun `validation - failure - detects invalid image url format`() {
        val invalidUrls = listOf(
            "not-a-url",
            "ftp://example.com/image.jpg",
            "https://example.com/document.pdf",
            "https://example.com/image",
            "http://example.com/image.txt"
        )

        invalidUrls.forEach { url ->
            val request = CollectionUpdateApiRequest(
                name = "컬렉션",
                coverImageUrl = url
            )

            val violations = validator.validate(request)
            val urlViolation = findViolationByProperty(violations, "coverImageUrl")

            assertAll(
                { assertTrue(violations.isNotEmpty(), "Should fail for URL: $url") },
                { assertNotNull(urlViolation) },
                { assertEquals("올바른 이미지 URL 형식이 아닙니다. (jpg, jpeg, png, gif, webp 형식만 지원)", urlViolation?.message) }
            )
        }
    }

    @Test
    fun `validation - success - accepts valid image url formats`() {
        val validUrls = listOf(
            "https://example.com/image.jpg",
            "http://example.com/image.jpeg",
            "https://example.com/image.png",
            "https://example.com/image.gif",
            "https://example.com/image.webp",
            "https://example.com/image.jpg?size=large",
            "https://subdomain.example.com/path/to/image.png"
        )

        validUrls.forEach { url ->
            val request = CollectionUpdateApiRequest(
                name = "컬렉션",
                coverImageUrl = url
            )

            val violations = validator.validate(request)
            assertTrue(violations.isEmpty(), "Should pass for URL: $url")
        }
    }

    @Test
    fun `validation - success - accepts null and empty cover image url`() {
        val requestWithNull = CollectionUpdateApiRequest(
            name = "컬렉션",
            coverImageUrl = null
        )
        val requestWithEmpty = CollectionUpdateApiRequest(
            name = "컬렉션",
            coverImageUrl = ""
        )

        assertAll(
            { assertTrue(validator.validate(requestWithNull).isEmpty()) },
            { assertTrue(validator.validate(requestWithEmpty).isEmpty()) }
        )
    }

    @Test
    fun `validation - failure - detects multiple validation errors`() {
        val request = CollectionUpdateApiRequest(
            name = "", // 빈 이름
            description = "a".repeat(501), // 설명 길이 초과
            coverImageUrl = "invalid-url" // 잘못된 URL
        )

        val violations = validator.validate(request)

        assertAll(
            { assertEquals(3, violations.size) },
            { assertNotNull(findViolationByProperty(violations, "name")) },
            { assertNotNull(findViolationByProperty(violations, "description")) },
            { assertNotNull(findViolationByProperty(violations, "coverImageUrl")) }
        )
    }

    @Test
    fun `validation - success - accepts cover image url at max length`() {
        val maxLengthUrl = "https://example.com/" + "a".repeat(476) + ".jpg" // 총 500자
        val request = CollectionUpdateApiRequest(
            name = "컬렉션",
            coverImageUrl = maxLengthUrl
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `toServiceDto - success - creates valid CollectionInfo object`() {
        val request = CollectionUpdateApiRequest(
            name = "검증된 컬렉션",
            description = "검증된 설명",
            coverImageUrl = "https://example.com/verified.jpg"
        )

        val serviceDto = request.toServiceDto(collectionId = 100L, ownerMemberId = 200L)
        val collectionInfo = serviceDto.newInfo

        // CollectionInfo가 정상적으로 생성되는지 확인
        assertAll(
            { assertEquals("검증된 컬렉션", collectionInfo.name) },
            { assertEquals("검증된 설명", collectionInfo.description) },
            { assertEquals("https://example.com/verified.jpg", collectionInfo.coverImageUrl) }
        )
    }

    @Test
    fun `toServiceDto - success - handles edge cases in cover image url processing`() {
        val requestWithOnlyWhitespace = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = "    "
        )
        val requestWithEmptyAfterTrim = CollectionUpdateApiRequest(
            name = "컬렉션",
            description = "설명",
            coverImageUrl = "  \t  "
        )

        assertAll(
            { assertNull(requestWithOnlyWhitespace.toServiceDto(1L, 2L).newInfo.coverImageUrl) },
            { assertNull(requestWithEmptyAfterTrim.toServiceDto(1L, 2L).newInfo.coverImageUrl) }
        )
    }

    @Test
    fun `toServiceDto - success - preserves CollectionInfo invariants`() {
        val request = CollectionUpdateApiRequest(
            name = "불변성 테스트",
            description = "불변성 확인용",
            coverImageUrl = "https://example.com/invariant.jpg"
        )

        // 여러 번 호출해도 같은 결과가 나오는지 확인
        val serviceDto1 = request.toServiceDto(1L, 2L)
        val serviceDto2 = request.toServiceDto(1L, 2L)

        assertAll(
            { assertEquals(serviceDto1.newInfo.name, serviceDto2.newInfo.name) },
            { assertEquals(serviceDto1.newInfo.description, serviceDto2.newInfo.description) },
            { assertEquals(serviceDto1.newInfo.coverImageUrl, serviceDto2.newInfo.coverImageUrl) }
        )
    }

    private fun findViolationByProperty(
        violations: Set<ConstraintViolation<CollectionUpdateApiRequest>>,
        propertyName: String,
    ): ConstraintViolation<CollectionUpdateApiRequest>? {
        return violations.find { it.propertyPath.toString() == propertyName }
    }
}
