package com.albert.realmoneyrealtaste.domain.post.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostImagesTest {

    @Test
    fun `create - success - creates images with valid urls`() {
        val urls = listOf("https://example.com/1.jpg", "https://example.com/2.jpg")
        val images = PostImages(urls)

        assertEquals(2, images.size())
        assertEquals(urls, images.urls)
    }

    @Test
    fun `create - success - creates empty images`() {
        val images = PostImages.empty()

        assertTrue(images.isEmpty())
        assertEquals(0, images.size())
    }

    @Test
    fun `create - failure - throws exception when exceeds max count`() {
        val urls = (1..6).map { "https://example.com/$it.jpg" }

        assertFailsWith<IllegalArgumentException> {
            PostImages(urls)
        }.let {
            assertEquals(PostImages.ERROR_MAX_IMAGE_COUNT, it.message)
        }
    }

    @Test
    fun `create - success - accepts exactly max count`() {
        val urls = (1..5).map { "https://example.com/$it.jpg" }

        val images = PostImages(urls)

        assertEquals(5, images.size())
    }

    @Test
    fun `create - failure - throws exception when url is blank`() {
        val urls = listOf("https://example.com/1.jpg", "")

        assertFailsWith<IllegalArgumentException> {
            PostImages(urls)
        }.let {
            assertEquals("이미지 URL은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when url exceeds max length`() {
        val longUrl = "https://example.com/" + "a".repeat(500)
        val urls = listOf(longUrl)

        assertFailsWith<IllegalArgumentException> {
            PostImages(urls)
        }.let {
            assertEquals(PostImages.ERROR_URL_LENGTH, it.message)
        }
    }

    @Test
    fun `isEmpty - success - returns true for empty images`() {
        val images = PostImages.empty()

        assertTrue(images.isEmpty())
        assertFalse(images.isNotEmpty())
    }

    @Test
    fun `isEmpty - success - returns false for non-empty images`() {
        val images = PostImages(listOf("https://example.com/1.jpg"))

        assertFalse(images.isEmpty())
        assertTrue(images.isNotEmpty())
    }

    @Test
    fun `of - success - creates images from varargs`() {
        val images = PostImages.of(
            "https://example.com/1.jpg",
            "https://example.com/2.jpg"
        )

        assertEquals(2, images.size())
    }

    @Test
    fun `constants - success - has correct max count`() {
        assertEquals(5, PostImages.MAX_IMAGE_COUNT)
    }
}
