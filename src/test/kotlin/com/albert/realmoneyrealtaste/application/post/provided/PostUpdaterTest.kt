package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostDeleteException
import com.albert.realmoneyrealtaste.application.post.exception.PostUpdateException
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
        val newImages = PostImages(listOf(1))
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
        val notExistsPostId = 999L

        assertFailsWith<PostUpdateException> {
            postUpdater.updatePost(notExistsPostId, member.requireId(), request)
        }.let {
            assertEquals("포스트 수정에 실패했습니다. postId: ${notExistsPostId}, memberId: ${member.requireId()}", it.message)
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

        assertFailsWith<PostUpdateException> {
            postUpdater.updatePost(post.requireId(), other.requireId(), request)
        }.let {
            assertEquals("포스트 수정에 실패했습니다. postId: ${post.requireId()}, memberId: ${other.requireId()}", it.message)
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

        assertFailsWith<PostUpdateException> {
            postUpdater.updatePost(post.requireId(), member.requireId(), request)
        }.let {
            assertEquals("포스트 수정에 실패했습니다. postId: ${post.requireId()}, memberId: ${member.requireId()}", it.message)
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
        val notExistsPostId = 999L

        assertFailsWith<PostDeleteException> {
            postUpdater.deletePost(notExistsPostId, member.requireId())
        }.let {
            assertEquals("포스트 삭제에 실패했습니다. postId: ${notExistsPostId}, memberId: ${member.requireId()}", it.message)
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

        assertFailsWith<PostDeleteException> {
            postUpdater.deletePost(post.requireId(), other.requireId())
        }.let {
            assertEquals("포스트 삭제에 실패했습니다. postId: ${post.requireId()}, memberId: ${other.requireId()}", it.message)
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

        assertFailsWith<PostDeleteException> {
            postUpdater.deletePost(post.requireId(), member.requireId())
        }.let {
            assertEquals("포스트 삭제에 실패했습니다. postId: ${post.requireId()}, memberId: ${member.requireId()}", it.message)
        }
    }
}
