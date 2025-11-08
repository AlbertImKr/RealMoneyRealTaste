package com.albert.realmoneyrealtaste.adapter.webapi.collection.request

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateCollectionApiRequestTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `toCommand - success - converts to CollectionCreateCommand with valid parameters`() {
        val request = CollectionCreateApiRequest(
            name = "테스트 컬렉션",
            description = "테스트 설명",
            visibility = "PUBLIC",
            coverImageUrl = "https://example.com/cover.jpg"
        )

        val command = request.toCommand(ownerMemberId = 123L)

        assertAll(
            { assertEquals(123L, command.ownerMemberId) },
            { assertEquals("테스트 컬렉션", command.name) },
            { assertEquals("테스트 설명", command.description) },
            { assertEquals(CollectionPrivacy.PUBLIC, command.privacy) },
            { assertEquals("https://example.com/cover.jpg", command.coverImageUrl) }
        )
    }

    @Test
    fun `toCommand - success - converts with default values`() {
        val request = CollectionCreateApiRequest(
            name = "기본 컬렉션"
        )

        val command = request.toCommand(ownerMemberId = 456L)

        assertAll(
            { assertEquals(456L, command.ownerMemberId) },
            { assertEquals("기본 컬렉션", command.name) },
            { assertEquals("", command.description) },
            { assertEquals(CollectionPrivacy.PRIVATE, command.privacy) },
            { assertNull(command.coverImageUrl) }
        )
    }

    @Test
    fun `toCommand - success - trims whitespace from inputs`() {
        val request = CollectionCreateApiRequest(
            name = "  컬렉션 이름  ",
            description = "  설명  ",
            visibility = "PUBLIC",
            coverImageUrl = "  https://example.com/image.jpg  "
        )

        val command = request.toCommand(ownerMemberId = 789L)

        assertAll(
            { assertEquals("컬렉션 이름", command.name) },
            { assertEquals("설명", command.description) },
            { assertEquals("https://example.com/image.jpg", command.coverImageUrl) }
        )
    }

    @Test
    fun `toCommand - success - converts visibility to privacy correctly`() {
        val publicRequest = CollectionCreateApiRequest(
            name = "공개 컬렉션",
            visibility = "PUBLIC"
        )
        val privateRequest = CollectionCreateApiRequest(
            name = "비공개 컬렉션",
            visibility = "PRIVATE"
        )
        val invalidRequest = CollectionCreateApiRequest(
            name = "잘못된 컬렉션",
            visibility = "INVALID"
        )

        assertAll(
            { assertEquals(CollectionPrivacy.PUBLIC, publicRequest.toCommand(1L).privacy) },
            { assertEquals(CollectionPrivacy.PRIVATE, privateRequest.toCommand(1L).privacy) },
            { assertEquals(CollectionPrivacy.PRIVATE, invalidRequest.toCommand(1L).privacy) } // 기본값
        )
    }

    @Test
    fun `toCommand - success - handles empty cover image url`() {
        val requestWithEmpty = CollectionCreateApiRequest(
            name = "컬렉션",
            coverImageUrl = ""
        )
        val requestWithBlank = CollectionCreateApiRequest(
            name = "컬렉션",
            coverImageUrl = "   "
        )

        assertAll(
            { assertNull(requestWithEmpty.toCommand(1L).coverImageUrl) },
            { assertNull(requestWithBlank.toCommand(1L).coverImageUrl) }
        )
    }

    @Test
    fun `validation - success - passes with valid inputs`() {
        val request = CollectionCreateApiRequest(
            name = "유효한 컬렉션",
            description = "유효한 설명",
            visibility = "PUBLIC",
            coverImageUrl = "https://example.com/valid.jpg"
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - success - passes with minimal required fields`() {
        val request = CollectionCreateApiRequest(
            name = "최소 컬렉션"
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects blank name`() {
        val request = CollectionCreateApiRequest(
            name = ""
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
        val request = CollectionCreateApiRequest(
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
        val request = CollectionCreateApiRequest(
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
        val request = CollectionCreateApiRequest(
            name = "a".repeat(100) // 정확히 100자
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects description exceeding max length`() {
        val request = CollectionCreateApiRequest(
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
        val request = CollectionCreateApiRequest(
            name = "컬렉션",
            description = "a".repeat(500) // 정확히 500자
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - success - accepts empty description`() {
        val request = CollectionCreateApiRequest(
            name = "컬렉션",
            description = ""
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation - failure - detects invalid visibility`() {
        val request = CollectionCreateApiRequest(
            name = "컬렉션",
            visibility = "INVALID"
        )

        val violations = validator.validate(request)
        val visibilityViolation = findViolationByProperty(violations, "visibility")

        assertAll(
            { assertTrue(violations.isNotEmpty()) },
            { assertNotNull(visibilityViolation) },
            { assertEquals("공개 설정은 PUBLIC 또는 PRIVATE만 가능합니다.", visibilityViolation?.message) }
        )
    }

    @Test
    fun `validation - success - accepts valid visibility values`() {
        val publicRequest = CollectionCreateApiRequest(
            name = "공개 컬렉션",
            visibility = "PUBLIC"
        )
        val privateRequest = CollectionCreateApiRequest(
            name = "비공개 컬렉션",
            visibility = "PRIVATE"
        )

        assertAll(
            { assertTrue(validator.validate(publicRequest).isEmpty()) },
            { assertTrue(validator.validate(privateRequest).isEmpty()) }
        )
    }

    @Test
    fun `validation - failure - detects cover image url exceeding max length`() {
        val longUrl = "https://example.com/" + "a".repeat(485) + ".jpg" // 총 길이 501자
        val request = CollectionCreateApiRequest(
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
            val request = CollectionCreateApiRequest(
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
            val request = CollectionCreateApiRequest(
                name = "컬렉션",
                coverImageUrl = url
            )

            val violations = validator.validate(request)
            assertTrue(violations.isEmpty(), "Should pass for URL: $url")
        }
    }

    @Test
    fun `validation - success - accepts null and empty cover image url`() {
        val requestWithNull = CollectionCreateApiRequest(
            name = "컬렉션",
            coverImageUrl = null
        )
        val requestWithEmpty = CollectionCreateApiRequest(
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
        val request = CollectionCreateApiRequest(
            name = "", // 빈 이름
            description = "a".repeat(501), // 설명 길이 초과
            visibility = "INVALID", // 잘못된 공개 설정
            coverImageUrl = "invalid-url" // 잘못된 URL
        )

        val violations = validator.validate(request)

        assertAll(
            { assertEquals(4, violations.size) },
            { assertNotNull(findViolationByProperty(violations, "name")) },
            { assertNotNull(findViolationByProperty(violations, "description")) },
            { assertNotNull(findViolationByProperty(violations, "visibility")) },
            { assertNotNull(findViolationByProperty(violations, "coverImageUrl")) }
        )
    }

    @Test
    fun `validation - success - case insensitive visibility conversion in toCommand`() {
        val mixedCaseRequest = CollectionCreateApiRequest(
            name = "컬렉션",
            visibility = "public"
        )

        val command = mixedCaseRequest.toCommand(1L)
        assertEquals(CollectionPrivacy.PUBLIC, command.privacy)
    }

    private fun findViolationByProperty(
        violations: Set<ConstraintViolation<CollectionCreateApiRequest>>,
        propertyName: String,
    ): ConstraintViolation<CollectionCreateApiRequest>? {
        return violations.find { it.propertyPath.toString() == propertyName }
    }
}
