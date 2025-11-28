package com.albert.realmoneyrealtaste.domain.post.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostImagesTest {

    @Test
    fun `create - success - creates images with valid urls`() {
        val urls = listOf(1L, 2)
        val images = PostImages(urls)

        assertEquals(2, images.size())
        assertEquals(urls, images.imageIds)
    }

    @Test
    fun `create - success - creates empty images`() {
        val images = PostImages.empty()

        assertTrue(images.isEmpty())
        assertEquals(0, images.size())
    }

    @Test
    fun `create - failure - throws exception when exceeds max count`() {
        val urls: List<Long> = (1..6).map { it.toLong() }

        assertFailsWith<IllegalArgumentException> {
            PostImages(urls)
        }.let {
            assertEquals(PostImages.ERROR_MAX_IMAGE_COUNT, it.message)
        }
    }

    @Test
    fun `create - success - accepts exactly max count`() {
        val urls = (1..5).map { it.toLong() }

        val images = PostImages(urls)

        assertEquals(5, images.size())
    }

    @Test
    fun `isEmpty - success - returns true for empty images`() {
        val images = PostImages.empty()

        assertTrue(images.isEmpty())
        assertFalse(images.isNotEmpty())
    }

    @Test
    fun `isEmpty - success - returns false for non-empty images`() {
        val images = PostImages(listOf(1))

        assertFalse(images.isEmpty())
        assertTrue(images.isNotEmpty())
    }

    @Test
    fun `of - success - creates images from varargs`() {
        val images = PostImages.of(
            1, 2
        )

        assertEquals(2, images.size())
    }

    @Test
    fun `constants - success - has correct max count`() {
        assertEquals(5, PostImages.MAX_IMAGE_COUNT)
    }

    @Test
    fun `getFirst - success - returns first url when not empty`() {
        val firstUrl = 1L
        val images = PostImages(listOf(1, 2))

        assertEquals(firstUrl, images.getFirst())
    }

    @Test
    fun `getFirst - success - returns null when empty`() {
        val images = PostImages.empty()

        assertEquals(null, images.getFirst())
    }
}
