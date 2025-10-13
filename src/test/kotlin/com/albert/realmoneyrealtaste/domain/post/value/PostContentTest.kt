package com.albert.realmoneyrealtaste.domain.post.value

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidPostContentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PostContentTest {

    @Test
    fun `create - success - creates content with valid parameters`() {
        val content = PostContent("맛있어요!", 5)

        assertEquals("맛있어요!", content.text)
        assertEquals(5, content.rating)
    }

    @Test
    fun `create - failure - throws exception when text is blank`() {
        assertFailsWith<InvalidPostContentException> {
            PostContent("", 5)
        }.let {
            assertEquals("게시글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when text exceeds max length`() {
        val longText = "a".repeat(2001)

        assertFailsWith<InvalidPostContentException> {
            PostContent(longText, 5)
        }.let {
            assertEquals("게시글 내용은 2000자를 초과할 수 없습니다.", it.message)
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
        assertFailsWith<InvalidPostContentException> {
            PostContent("내용", 0)
        }.let {
            assertTrue(it.message!!.contains("평점은 1에서 5 사이여야 합니다"))
        }
    }

    @Test
    fun `create - failure - throws exception when rating is greater than max`() {
        assertFailsWith<InvalidPostContentException> {
            PostContent("내용", 6)
        }.let {
            assertTrue(it.message!!.contains("평점은 1에서 5 사이여야 합니다"))
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
