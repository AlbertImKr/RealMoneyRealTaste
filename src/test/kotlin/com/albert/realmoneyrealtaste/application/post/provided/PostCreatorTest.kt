package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.post.PostStatus
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class PostCreatorTest(
    private val postCreator: PostCreator,
    private val postRepository: PostRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `createPost - success - creates post with valid parameters`() {
        val member = testMemberHelper.createActivatedMember()
        val request = createPostRequest()

        val result = postCreator.createPost(member.requireId(), request)

        assertNotNull(result.id)
        assertEquals(member.id, result.author.memberId)
        assertEquals(member.nickname.value, result.author.nickname)
        assertEquals(request.restaurant.name, result.restaurant.name)
        assertEquals(request.content.text, result.content.text)
        assertEquals(request.content.rating, result.content.rating)
        assertEquals(request.images.urls, result.images.urls)
        assertEquals(PostStatus.PUBLISHED, result.status)
        assertEquals(0, result.heartCount)
        assertEquals(0, result.viewCount)
    }

    @Test
    fun `createPost - success - saves post to repository`() {
        val member = testMemberHelper.createActivatedMember()
        val request = createPostRequest()

        val result = postCreator.createPost(member.requireId(), request)

        val savedPost = postRepository.findById(result.requireId())
        assertNotNull(savedPost)
        assertEquals(result.id, savedPost.id)
        assertEquals(result.author.memberId, savedPost.author.memberId)
    }

    @Test
    fun `createPost - success - creates post without images`() {
        val member = testMemberHelper.createActivatedMember()
        val request = createPostRequest(images = PostImages.empty())

        val result = postCreator.createPost(member.requireId(), request)

        assertTrue(result.images.isEmpty())
        assertEquals(0, result.images.size())
    }

    @Test
    fun `createPost - success - creates post with multiple images`() {
        val member = testMemberHelper.createActivatedMember()
        val imageUrls = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        )
        val request = createPostRequest(images = PostImages(imageUrls))

        val result = postCreator.createPost(member.requireId(), request)

        assertEquals(3, result.images.size())
        assertEquals(imageUrls, result.images.urls)
    }

    @Test
    fun `createPost - success - publishes PostCreatedEvent`() {
        val member = testMemberHelper.createActivatedMember()
        val request = createPostRequest()

        val result = postCreator.createPost(member.requireId(), request)

        val events = applicationEvents.stream(PostCreatedEvent::class.java).toList()
        assertEquals(1, events.size)

        val event = events.first()
        assertEquals(result.id, event.postId)
        assertEquals(member.id, event.authorMemberId)
        assertEquals(request.restaurant.name, event.restaurantName)
    }

    @Test
    fun `createPost - success - uses member nickname as author nickname`() {
        val member = testMemberHelper.createActivatedMember(
            nickname = Nickname("테스트유저")
        )
        val request = createPostRequest()

        val result = postCreator.createPost(member.requireId(), request)

        assertEquals("테스트유저", result.author.nickname)
    }

    @Test
    fun `createPost - failure - throws exception when member not found`() {
        val request = createPostRequest()

        assertFailsWith<MemberNotFoundException> {
            postCreator.createPost(999L, request)
        }.let {
            assertTrue(it.message!!.contains("회원을 찾을 수 없습니다"))
        }
    }

    @Test
    fun `createPost - failure - does not save post when member not found`() {
        val request = createPostRequest()

        assertFailsWith<MemberNotFoundException> {
            postCreator.createPost(999L, request)
        }

        assertTrue {
            postRepository.findByAuthorMemberIdAndStatusNot(999L, PageRequest.of(0, 10)).isEmpty
        }
    }

    @Test
    fun `createPost - failure - does not publish event when member not found`() {
        val request = createPostRequest()

        assertFailsWith<MemberNotFoundException> {
            postCreator.createPost(999L, request)
        }

        val events = applicationEvents.stream(PostCreatedEvent::class.java).toList()
        assertEquals(0, events.size)
    }

    @Test
    fun `createPost - success - creates multiple posts for same member`() {
        val member = testMemberHelper.createActivatedMember()
        val request1 = createPostRequest(
            content = PostContent("첫 번째 게시글", 5)
        )
        val request2 = createPostRequest(
            content = PostContent("두 번째 게시글", 4)
        )

        val post1 = postCreator.createPost(member.requireId(), request1)
        val post2 = postCreator.createPost(member.requireId(), request2)

        assertNotNull(post1.id)
        assertNotNull(post2.id)
        assertTrue(post1.id != post2.id)
        assertEquals("첫 번째 게시글", post1.content.text)
        assertEquals("두 번째 게시글", post2.content.text)
    }

    @Test
    fun `createPost - success - creates posts for different members`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = Email("member1@test.com"),
            nickname = Nickname("회원1"),
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = Email("member2@test.com"),
            nickname = Nickname("회원2"),
        )

        val request = createPostRequest()

        val post1 = postCreator.createPost(member1.requireId(), request)
        val post2 = postCreator.createPost(member2.requireId(), request)

        assertEquals(member1.id, post1.author.memberId)
        assertEquals(member2.id, post2.author.memberId)
        assertEquals("회원1", post1.author.nickname)
        assertEquals("회원2", post2.author.nickname)
    }

    private fun createPostRequest(
        restaurant: Restaurant = Restaurant(
            name = "테스트 맛집",
            address = "서울시 강남구 테스트로 123",
            latitude = 37.5665,
            longitude = 126.9780
        ),
        content: PostContent = PostContent(
            text = "정말 맛있는 맛집입니다!",
            rating = 5
        ),
        images: PostImages = PostImages(
            listOf(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg"
            )
        ),
    ): PostCreateRequest {
        return PostCreateRequest(
            restaurant = restaurant,
            content = content,
            images = images
        )
    }
}
