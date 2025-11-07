package com.albert.realmoneyrealtaste.domain.collection.value

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionPostsTest {

    @Test
    fun `create - success - with empty list`() {
        val posts = CollectionPosts.empty()

        assertTrue(posts.isEmpty())
        assertFalse(posts.isNotEmpty())
        assertEquals(0, posts.size())
    }

    @Test
    fun `create - success - with valid post ids`() {
        val posts = CollectionPosts.of(1L, 2L, 3L)

        assertEquals(3, posts.size())
        assertTrue(posts.contains(1L))
        assertTrue(posts.contains(2L))
        assertTrue(posts.contains(3L))
        assertFalse(posts.isEmpty())
    }

    @Test
    fun `create - failure - with duplicate post ids`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionPosts(listOf(1L, 2L, 1L))
        }
    }

    @Test
    fun `create - failure - with invalid post id`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionPosts(listOf(0L))
        }

        assertFailsWith<IllegalArgumentException> {
            CollectionPosts(listOf(-1L))
        }
    }

    @Test
    fun `create - failure - exceeds max post count`() {
        val tooManyPosts = (1L..CollectionPosts.MAX_POST_COUNT + 1).toList()

        assertFailsWith<IllegalArgumentException> {
            CollectionPosts(tooManyPosts)
        }
    }

    @Test
    fun `create - success - at max post count`() {
        val maxPosts = (1L..CollectionPosts.MAX_POST_COUNT).toList()

        val posts = CollectionPosts(maxPosts)

        assertEquals(CollectionPosts.MAX_POST_COUNT, posts.size())
    }

    @Test
    fun `add - success - adds new post`() {
        val posts = CollectionPosts.of(1L, 2L)

        val newPosts = posts.add(3L)

        assertEquals(3, newPosts.size())
        assertTrue(newPosts.contains(3L))
        // 원본은 변경되지 않음
        assertEquals(2, posts.size())
        assertFalse(posts.contains(3L))
    }

    @Test
    fun `add - failure - duplicate post`() {
        val posts = CollectionPosts.of(1L, 2L)

        assertFailsWith<IllegalArgumentException> {
            posts.add(1L)
        }
    }

    @Test
    fun `add - failure - invalid post id`() {
        val posts = CollectionPosts.empty()

        assertFailsWith<IllegalArgumentException> {
            posts.add(0L)
        }

        assertFailsWith<IllegalArgumentException> {
            posts.add(-1L)
        }
    }

    @Test
    fun `add - failure - exceeds max limit`() {
        val maxPosts = (1L..CollectionPosts.MAX_POST_COUNT).toList()
        val posts = CollectionPosts(maxPosts)

        assertFailsWith<IllegalArgumentException> {
            posts.add(1000L)
        }
    }

    @Test
    fun `remove - success - removes existing post`() {
        val posts = CollectionPosts.of(1L, 2L, 3L)

        val newPosts = posts.remove(2L)

        assertEquals(2, newPosts.size())
        assertFalse(newPosts.contains(2L))
        assertTrue(newPosts.contains(1L))
        assertTrue(newPosts.contains(3L))
        // 원본은 변경되지 않음
        assertEquals(3, posts.size())
        assertTrue(posts.contains(2L))
    }

    @Test
    fun `remove - failure - non-existing post`() {
        val posts = CollectionPosts.of(1L, 2L)

        assertFailsWith<IllegalArgumentException> {
            posts.remove(3L)
        }
    }

    @Test
    fun `remove - success - from single post collection`() {
        val posts = CollectionPosts.of(1L)

        val newPosts = posts.remove(1L)

        assertTrue(newPosts.isEmpty())
    }

    @Test
    fun `contains - success - returns correct result`() {
        val posts = CollectionPosts.of(1L, 2L, 3L)

        assertTrue(posts.contains(1L))
        assertTrue(posts.contains(2L))
        assertTrue(posts.contains(3L))
        assertFalse(posts.contains(4L))
    }

    @Test
    fun `postIds - success - maintains order`() {
        val posts = CollectionPosts(listOf(3L, 1L, 2L))

        assertEquals(listOf(3L, 1L, 2L), posts.postIds)
    }

    @Test
    fun `add - success - maintains order`() {
        val posts = CollectionPosts(listOf(3L, 1L))

        val newPosts = posts.add(2L)

        assertEquals(listOf(3L, 1L, 2L), newPosts.postIds)
    }

    @Test
    fun `isNotEmpty - success - returns correct result`() {
        val emptyPosts = CollectionPosts.empty()
        val nonEmptyPosts = CollectionPosts.of(1L)

        assertFalse(emptyPosts.isNotEmpty())
        assertTrue(nonEmptyPosts.isNotEmpty())
    }
}
