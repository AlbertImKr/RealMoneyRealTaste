package com.albert.realmoneyrealtaste.domain.post.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PostContentTest {

    @Test
    fun `create - success - creates content with valid parameters`() {
        val content = PostContent("맛있어요!", 5)

        assertEquals("맛있어요!", content.text)
        assertEquals(5, content.rating)
    }

    @Test
    fun `create - failure - throws exception when text is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PostContent("", 5)
        }.let {
            assertEquals(PostContent.ERROR_TEXT_BLANK, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when text exceeds max length`() {
        val longText = "a".repeat(2001)

        assertFailsWith<IllegalArgumentException> {
            PostContent(longText, 5)
        }.let {
            assertEquals(PostContent.ERROR_TEXT_LENGTH, it.message)
        }
    }

    @Test
    fun `create - success - accepts text at max length`() {
        val maxLengthText = "a".repeat(2000)

        val content = PostContent(maxLengthText, 5)

        assertEquals(2000, content.text.length)
    }

    @Test
    fun `create - failure - throws exception when rating is less than min`() {
        assertFailsWith<IllegalArgumentException> {
            PostContent("내용", 0)
        }.let {
            assertEquals(PostContent.ERROR_RATING_RANGE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when rating is greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            PostContent("내용", 6)
        }.let {
            assertEquals(PostContent.ERROR_RATING_RANGE, it.message)
        }
    }

    @Test
    fun `create - success - accepts all valid ratings`() {
        for (rating in 1..5) {
            val content = PostContent("내용", rating)
            assertEquals(rating, content.rating)
        }
    }

    @Test
    fun `constants - success - has correct values`() {
        assertEquals(2000, PostContent.MAX_LENGTH)
        assertEquals(1, PostContent.MIN_RATING)
        assertEquals(5, PostContent.MAX_RATING)
    }
}
