package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.PostStatus
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class PostUpdaterTest(
    private val postUpdater: PostUpdater,
    private val postRepository: PostRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `updatePost - success - updates post content`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        val newContent = PostContent("수정된 내용입니다!", 4)
        val newImages = PostImages(listOf("https://example.com/new.jpg"))
        val newRestaurant = Restaurant(
            name = "새로운 맛집",
            address = "서울시 마포구 새로운길 456",
            latitude = 37.5555,
            longitude = 126.9666
        )
        val request = PostUpdateRequest(newContent, newImages, newRestaurant)

        val result = postUpdater.updatePost(post.requireId(), member.requireId(), request)

        assertEquals("수정된 내용입니다!", result.content.text)
        assertEquals(4, result.content.rating)
        assertEquals(1, result.images.size())
        assertEquals("새로운 맛집", result.restaurant.name)
    }

    @Test
    fun `updatePost - success - persists changes to database`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        val request = PostUpdateRequest(
            content = PostContent("수정된 내용", 3),
            images = PostImages.empty(),
            restaurant = post.restaurant
        )

        postUpdater.updatePost(post.requireId(), member.requireId(), request)

        val updated = postRepository.findById(post.requireId())
        assertNotNull(updated)
        assertEquals("수정된 내용", updated.content.text)
        assertEquals(3, updated.content.rating)
    }

    @Test
    fun `updatePost - failure - throws exception when post not found`() {
        val member = testMemberHelper.createActivatedMember()
        val request = PostUpdateRequest(
            content = PostContent("내용", 5),
            images = PostImages.empty(),
            restaurant = PostFixture.DEFAULT_RESTAURANT
        )

        assertFailsWith<PostNotFoundException> {
            postUpdater.updatePost(999L, member.requireId(), request)
        }.let {
            assertTrue(it.message!!.contains("게시글을 찾을 수 없습니다"))
        }
    }

    @Test
    fun `updatePost - failure - throws exception when not author`() {
        val author = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람",
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        val request = PostUpdateRequest(
            content = PostContent("수정 시도", 5),
            images = PostImages.empty(),
            restaurant = post.restaurant
        )

        assertFailsWith<IllegalArgumentException> {
            postUpdater.updatePost(post.requireId(), other.requireId(), request)
        }.let {
            assertEquals("게시글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `updatePost - failure - throws exception when post is deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        post.delete(member.requireId())

        val request = PostUpdateRequest(
            content = PostContent("수정 시도", 5),
            images = PostImages.empty(),
            restaurant = post.restaurant
        )

        assertFailsWith<IllegalArgumentException> {
            postUpdater.updatePost(post.requireId(), member.requireId(), request)
        }.let {
            assertTrue(it.message!!.contains("게시글이 공개 상태가 아닙니다"))
        }
    }

    @Test
    fun `deletePost - success - changes status to deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        postUpdater.deletePost(post.requireId(), member.requireId())

        val deleted = postRepository.findById(post.requireId())
        assertNotNull(deleted)
        assertEquals(PostStatus.DELETED, deleted.status)
    }

    @Test
    fun `deletePost - success - publishes PostDeletedEvent`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        postUpdater.deletePost(post.requireId(), member.requireId())

        val events = applicationEvents.stream(PostDeletedEvent::class.java).toList()
        assertEquals(1, events.size)

        val event = events.first()
        assertEquals(post.requireId(), event.postId)
        assertEquals(member.requireId(), event.authorMemberId)
    }

    @Test
    fun `deletePost - failure - throws exception when post not found`() {
        val member = testMemberHelper.createActivatedMember()

        assertFailsWith<PostNotFoundException> {
            postUpdater.deletePost(999L, member.requireId())
        }.let {
            assertTrue(it.message!!.contains("게시글을 찾을 수 없습니다"))
        }
    }

    @Test
    fun `deletePost - failure - throws exception when not author`() {
        val author = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람",
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        assertFailsWith<IllegalArgumentException> {
            postUpdater.deletePost(post.requireId(), other.requireId())
        }.let {
            assertEquals("게시글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `deletePost - failure - throws exception when already deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        post.delete(member.requireId())

        assertFailsWith<IllegalArgumentException> {
            postUpdater.deletePost(post.requireId(), member.requireId())
        }.let {
            assertTrue(it.message!!.contains("게시글이 공개 상태가 아닙니다"))
        }
    }
}
