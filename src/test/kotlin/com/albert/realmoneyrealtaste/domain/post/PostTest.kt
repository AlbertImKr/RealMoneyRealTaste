package com.albert.realmoneyrealtaste.domain.post

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidPostStatusException
import com.albert.realmoneyrealtaste.domain.post.exceptions.UnauthorizedPostOperationException
import com.albert.realmoneyrealtaste.domain.post.value.Author
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.PostFixture
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PostTest {

    @Test
    fun `create - success - creates post with valid parameters`() {
        val post = PostFixture.createPost()

        assertEquals(PostFixture.DEFAULT_AUTHOR_MEMBER_ID, post.author.memberId)
        assertEquals(PostFixture.DEFAULT_AUTHOR_NICKNAME, post.author.nickname)
        assertEquals(PostFixture.DEFAULT_RESTAURANT_NAME, post.restaurant.name)
        assertEquals(PostFixture.DEFAULT_CONTENT_TEXT, post.content.text)
        assertEquals(PostFixture.DEFAULT_RATING, post.content.rating)
        assertEquals(PostStatus.PUBLISHED, post.status)
        assertEquals(0, post.heartCount)
        assertNotNull(post.createdAt)
        assertNotNull(post.updatedAt)
    }

    @Test
    fun `create - success - creates post without images`() {
        val post = PostFixture.createPostWithoutImages()

        assertTrue(post.images.isEmpty())
        assertEquals(0, post.images.size())
    }

    @Test
    fun `create - success - creates post with images`() {
        val post = PostFixture.createPost()

        assertTrue(post.images.isNotEmpty())
        assertEquals(2, post.images.size())
        assertEquals(PostFixture.DEFAULT_IMAGE_URLS, post.images.urls)
    }

    @Test
    fun `update - success - updates content and images`() {
        val post = PostFixture.createPost()
        val newContent = PostContent("새로운 내용입니다!", 4)
        val newImages = PostImages(listOf("https://example.com/new.jpg"))
        val newRestaurant = Restaurant(
            name = "새로운 맛집",
            address = "서울시 마포구 새로운길 456",
            latitude = 37.5555,
            longitude = 126.9666
        )
        val beforeUpdateAt = post.updatedAt

        post.update(PostFixture.DEFAULT_AUTHOR_MEMBER_ID, newContent, newImages, newRestaurant)

        assertEquals("새로운 내용입니다!", post.content.text)
        assertEquals(4, post.content.rating)
        assertEquals(1, post.images.size())
        assertEquals(newImages, post.images)
        assertEquals(newRestaurant, post.restaurant)
        assertTrue(beforeUpdateAt < post.updatedAt)
    }

    @Test
    fun `update - failure - throws exception when post is deleted`() {
        val post = PostFixture.createPost()
        post.delete(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)
        val newContent = PostContent("새로운 내용", 4)
        val newImages = PostImages.empty()

        assertFailsWith<InvalidPostStatusException> {
            post.update(PostFixture.DEFAULT_AUTHOR_MEMBER_ID, newContent, newImages, post.restaurant)
        }.let {
            assertTrue(it.message!!.contains("게시글이 공개 상태가 아닙니다"))
        }
    }

    @Test
    fun `update - failure - throws exception when member is not author`() {
        val post = PostFixture.createPost()
        val newContent = PostContent("새로운 내용", 4)
        val newImages = PostImages.empty()

        assertFailsWith<UnauthorizedPostOperationException> {
            post.update(999L, newContent, newImages, post.restaurant)
        }.let {
            assertEquals("게시글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `delete - success - changes status to deleted`() {
        val post = PostFixture.createPost()
        val beforeUpdateAt = post.updatedAt

        Thread.sleep(10)
        post.delete(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)

        assertEquals(PostStatus.DELETED, post.status)
        assertTrue(beforeUpdateAt < post.updatedAt)
    }

    @Test
    fun `delete - failure - throws exception when already deleted`() {
        val post = PostFixture.createPost()
        post.delete(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)

        assertFailsWith<InvalidPostStatusException> {
            post.delete(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)
        }.let {
            assertTrue(it.message!!.contains("게시글이 공개 상태가 아닙니다"))
        }
    }

    @Test
    fun `delete - failure - throws exception when member is not author`() {
        val post = PostFixture.createPost()

        assertFailsWith<UnauthorizedPostOperationException> {
            post.delete(999L)
        }.let {
            assertEquals("게시글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `canEditBy - success - returns true for post author`() {
        val post = PostFixture.createPost()

        assertTrue(post.canEditBy(PostFixture.DEFAULT_AUTHOR_MEMBER_ID))
    }

    @Test
    fun `canEditBy - success - returns false for different member`() {
        val post = PostFixture.createPost()

        assertFalse(post.canEditBy(999L))
    }

    @Test
    fun `ensureCanEditBy - success - does not throw for post author`() {
        val post = PostFixture.createPost()

        post.ensureCanEditBy(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)
    }

    @Test
    fun `ensureCanEditBy - failure - throws exception for different member`() {
        val post = PostFixture.createPost()

        assertFailsWith<UnauthorizedPostOperationException> {
            post.ensureCanEditBy(999L)
        }.let {
            assertEquals("게시글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `heartCount - success - starts at zero`() {
        val post = PostFixture.createPost()

        assertEquals(0, post.heartCount)
    }

    @Test
    fun `requireId - failure - throws exception when id is null`() {
        val post = PostFixture.createPost()

        assertFailsWith<IllegalStateException> {
            post.requireId()
        }.let {
            assertTrue(it.message!!.contains("ID가 설정되지 않았습니다"))
        }
    }

    @Test
    fun `isDeleted - success - returns false for published post`() {
        val post = PostFixture.createPost()

        assertFalse(post.isDeleted())
    }

    @Test
    fun `isDeleted - success - returns true for deleted post`() {
        val post = PostFixture.createPost()
        post.delete(PostFixture.DEFAULT_AUTHOR_MEMBER_ID)

        assertTrue(post.isDeleted())
    }

    @Test
    fun `requireId - success - returns id when set`() {
        val post = PostFixture.createPost()
        PostFixture.setId(post, 123L)

        assertEquals(123L, post.requireId())
    }

    @Test
    fun `setter - success - for covering frameworks`() {
        val post = TestPost()

        val newAuthor = Author(999L, "새로운닉네임")
        post.setAuthorForTest(newAuthor)
        assertEquals(999L, post.author.memberId)

        post.setHeartCountForTest(42)
        assertEquals(42, post.heartCount)

        val newTime = LocalDateTime.now().minusDays(1)
        post.setCreatedAtForTest(newTime)
        assertEquals(newTime, post.createdAt)

        post.setViewCountForTest(100)
        assertEquals(100, post.viewCount)
    }

    private class TestPost : Post(
        author = PostFixture.DEFAULT_AUTHOR,
        restaurant = PostFixture.DEFAULT_RESTAURANT,
        content = PostFixture.DEFAULT_CONTENT,
        images = PostFixture.DEFAULT_IMAGES,
        status = PostStatus.PUBLISHED,
        heartCount = 0,
        viewCount = 0,
        createdAt = LocalDateTime.now().minusMinutes(10),
        updatedAt = LocalDateTime.now()
    ) {
        fun setAuthorForTest(author: Author) {
            this.author = author
        }

        fun setHeartCountForTest(count: Int) {
            this.heartCount = count
        }

        fun setCreatedAtForTest(time: LocalDateTime) {
            this.createdAt = time
        }

        fun setViewCountForTest(count: Int) {
            this.viewCount = count
        }
    }
}
