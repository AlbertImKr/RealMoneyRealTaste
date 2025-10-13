package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.application.post.required.PostHeartRepository
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartAddedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartRemovedEvent
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RecordApplicationEvents
class PostHeartManagerTest(
    private val postHeartManager: PostHeartManager,
    private val postRepository: PostRepository,
    private val postHeartRepository: PostHeartRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    lateinit var author: Member
    lateinit var post: Post
    lateinit var user: Member

    @BeforeEach
    fun setting() {
        author = testMemberHelper.createActivatedMember()
        post = postRepository.save(PostFixture.createPost(author.requireId()))
        user = testMemberHelper.createActivatedMember(
            email = "heartMember@email.com",
            nickname = "heartMember"
        )
    }

    @Test
    fun `addHeart - success - adds a new heart to a post`() {
        postHeartManager.addHeart(post.requireId(), user.requireId())

        val heart = postHeartRepository.findByPostIdAndMemberId(post.requireId(), user.requireId())

        assertNotNull(heart)
        assertNotNull(applicationEvents.stream(PostHeartAddedEvent::class.java).findFirst().orElse(null))
    }

    @Test
    fun `addHeart - failure - post does not exist`() {
        assertFailsWith<PostNotFoundException> {
            postHeartManager.addHeart(9999L, user.requireId())
        }
    }

    @Test
    fun `addHeart - success - does not add duplicate hearts`() {
        postHeartManager.addHeart(post.requireId(), user.requireId())
        val heartAddedEvent = applicationEvents.stream(PostHeartAddedEvent::class.java).findFirst().orElse(null)
        assertNotNull(heartAddedEvent)
        applicationEvents.clear()

        postHeartManager.addHeart(post.requireId(), user.requireId())
        val duplicateHeartEvent = applicationEvents.stream(PostHeartAddedEvent::class.java).findFirst().orElse(null)
        assertNull(duplicateHeartEvent)

        postHeartRepository.findByPostIdAndMemberId(post.requireId(), user.requireId())

        val heartCount = postHeartRepository.countByPostId(post.requireId())
        assertEquals(1, heartCount)
    }

    @Test
    fun `removeHeart - success - removes an existing heart from a post`() {
        postHeartManager.addHeart(post.requireId(), user.requireId())
        applicationEvents.clear()

        postHeartManager.removeHeart(post.requireId(), user.requireId())

        val heartRemovedEvent = applicationEvents.stream(PostHeartRemovedEvent::class.java).findFirst().orElse(null)
        assertNotNull(heartRemovedEvent)
        val removedHeart = postHeartRepository.findByPostIdAndMemberId(post.requireId(), user.requireId())
        assertNull(removedHeart)
    }

    @Test
    fun `removeHeart - failure - post does not exist`() {
        assertFailsWith<PostNotFoundException> {
            postHeartManager.removeHeart(9999L, user.requireId())
        }
    }

    @Test
    fun `removeHeart - success - does nothing if heart does not exist`() {
        applicationEvents.clear()
        postHeartManager.removeHeart(post.requireId(), user.requireId())

        val heartRemovedEvent = applicationEvents.stream(PostHeartRemovedEvent::class.java).findFirst().orElse(null)
        assertNull(heartRemovedEvent)
        val heartCount = postHeartRepository.countByPostId(post.requireId())
        assertEquals(0, heartCount)
    }

    @Test
    fun `hasHeart - success - returns true if member has hearted the post`() {
        postHeartManager.addHeart(post.requireId(), user.requireId())

        val hasHeart = postHeartManager.hasHeart(post.requireId(), user.requireId())
        assertEquals(true, hasHeart)
    }

    @Test
    fun `hasHeart - success - returns false if member has not hearted the post`() {
        val hasHeart = postHeartManager.hasHeart(post.requireId(), user.requireId())

        assertEquals(false, hasHeart)
    }
}
