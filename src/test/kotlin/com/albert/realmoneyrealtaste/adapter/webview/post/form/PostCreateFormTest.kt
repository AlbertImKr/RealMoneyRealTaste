package com.albert.realmoneyrealtaste.adapter.webview.post.form

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostCreateFormTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `constructor - success - all fields provided`() {
        val form = PostCreateForm(
            restaurantName = "Test Restaurant",
            restaurantAddress = "123 Test St",
            restaurantLatitude = 37.7749,
            restaurantLongitude = -122.4194,
            contentText = "This is a test post content.",
            contentRating = 5,
            imageIds = listOf(1, 2),
        )
        assert(form.restaurantName == "Test Restaurant")
        assert(form.restaurantAddress == "123 Test St")
        assert(form.restaurantLatitude == 37.7749)
        assert(form.restaurantLongitude == -122.4194)
        assert(form.contentText == "This is a test post content.")
        assert(form.contentRating == 5)
        assert(form.imageIds.size == 2)
    }

    @Test
    fun `validation - success - valid form passes all validations`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isEmpty(), "Valid form should have no violations")
    }

    @Test
    fun `validation - failure - blank restaurant name`() {
        val form = PostCreateForm(
            restaurantName = "",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Blank restaurant name should have violations")
        assertTrue(violations.any { it.message == "맛집 이름을 입력해주세요." })
    }

    @Test
    fun `validation - failure - blank restaurant address`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Blank restaurant address should have violations")
        assertTrue(violations.any { it.message == "맛집 주소를 입력해주세요." })
    }

    @Test
    fun `validation - failure - latitude out of range`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 91.0,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Latitude out of range should have violations")
        assertTrue(violations.any { it.message == "유효한 위도 값을 입력해주세요." })
    }

    @Test
    fun `validation - failure - longitude out of range`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 181.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Longitude out of range should have violations")
        assertTrue(violations.any { it.message == "유효한 경도 값을 입력해주세요." })
    }

    @Test
    fun `validation - failure - blank content text`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Blank content text should have violations")
        assertTrue(violations.any { it.message == "내용을 입력해주세요." })
    }

    @Test
    fun `validation - failure - rating below minimum`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 0,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Rating below minimum should have violations")
        assertTrue(violations.any { it.message == "평점은 1점 이상 5점 이하로 입력해주세요." })
    }

    @Test
    fun `validation - failure - rating above maximum`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 6,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Rating above maximum should have violations")
        assertTrue(violations.any { it.message == "평점은 1점 이상 5점 이하로 입력해주세요." })
    }

    @Test
    fun `validation - failure - too many images`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(
                1, 2, 3, 4, 5, 6
            ),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Too many images should have violations")
        assertTrue(violations.any { it.message == "이미지는 최대 5장까지 업로드할 수 있습니다." })
    }

    @Test
    fun `validation - failure - empty images list`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = emptyList(),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(violations.isNotEmpty(), "Empty images list should have violations")
        assertTrue(violations.any { it.message == "이미지는 최대 5장까지 업로드할 수 있습니다." })
    }

    @Test
    fun `toPostCreateRequest - success - converts form to PostCreateRequest correctly`() {
        val form = PostCreateForm(
            restaurantName = "Test Restaurant",
            restaurantAddress = "123 Test St",
            restaurantLatitude = 37.7749,
            restaurantLongitude = -122.4194,
            contentText = "This is a test post content.",
            contentRating = 5,
            imageIds = listOf(1, 2),
        )

        val request = form.toPostCreateRequest()

        assert(request.restaurant.name == "Test Restaurant")
        assert(request.restaurant.address == "123 Test St")
        assert(request.restaurant.latitude == 37.7749)
        assert(request.restaurant.longitude == -122.4194)
        assert(request.content.text == "This is a test post content.")
        assert(request.content.rating == 5)
        assert(request.images.imageIds.size == 2)
        assertEquals(1, request.images.imageIds[0])
        assertEquals(2, request.images.imageIds[1])
    }

    @Test
    fun `toPostCreateRequest - success - converts form with single image`() {
        val form = PostCreateForm(
            restaurantName = "Single Image Restaurant",
            restaurantAddress = "456 Single St",
            restaurantLatitude = 37.1234,
            restaurantLongitude = -127.5678,
            contentText = "Single image test content.",
            contentRating = 3,
            imageIds = listOf(1),
        )

        val request = form.toPostCreateRequest()

        assert(request.restaurant.name == "Single Image Restaurant")
        assert(request.content.text == "Single image test content.")
        assert(request.content.rating == 3)
        assert(request.images.imageIds.size == 1)
        assertEquals(1, request.images.imageIds[0])
    }

    @Test
    fun `constructor - success - uses default values`() {
        val form = PostCreateForm()

        assert(form.restaurantName == "")
        assert(form.restaurantAddress == "")
        assert(form.restaurantLatitude == 0.0)
        assert(form.restaurantLongitude == 0.0)
        assert(form.contentText == "")
        assert(form.contentRating == 0)
        assert(form.imageIds.isEmpty())
    }

    @Test
    fun `validation - boundary - minimum valid latitude`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = -90.0,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "restaurantLatitude" },
            "Minimum valid latitude should pass"
        )
    }

    @Test
    fun `validation - boundary - maximum valid latitude`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 90.0,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "restaurantLatitude" },
            "Maximum valid latitude should pass"
        )
    }

    @Test
    fun `validation - boundary - minimum valid longitude`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = -180.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "restaurantLongitude" },
            "Minimum valid longitude should pass"
        )
    }

    @Test
    fun `validation - boundary - maximum valid longitude`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 180.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "restaurantLongitude" },
            "Maximum valid longitude should pass"
        )
    }

    @Test
    fun `validation - boundary - minimum valid rating`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 1,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "contentRating" },
            "Minimum valid rating should pass"
        )
    }

    @Test
    fun `validation - boundary - maximum valid rating`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 5,
            imageIds = listOf(1),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "contentRating" },
            "Maximum valid rating should pass"
        )
    }

    @Test
    fun `validation - boundary - maximum valid images count`() {
        val form = PostCreateForm(
            restaurantName = "Valid Restaurant",
            restaurantAddress = "Valid Address",
            restaurantLatitude = 37.5,
            restaurantLongitude = 127.0,
            contentText = "Valid content",
            contentRating = 4,
            imageIds = listOf(
                1, 2, 3, 4, 5
            ),
        )

        val violations: Set<ConstraintViolation<PostCreateForm>> = validator.validate(form)
        assertTrue(
            violations.none { it.propertyPath.toString() == "imagesUrls" },
            "Maximum valid images count should pass"
        )
    }
}
