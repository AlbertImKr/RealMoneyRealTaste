package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.util.IntegrationConcurrencyTestBase
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostUpdaterConcurrencyTest(
    private val postUpdater: PostUpdater,
    private val postRepository: PostRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationConcurrencyTestBase() {

    @Test
    fun `incrementHeartCount - success - increments heart count correctly under high concurrency`() = runBlocking {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        val concurrentCount = 100

        val jobs = (1..concurrentCount).map {
            async(Dispatchers.IO) {
                postUpdater.incrementHeartCount(post.requireId())
            }
        }
        jobs.awaitAll()

        val updated = postRepository.findById(post.requireId())
        assertNotNull(updated)
        assertEquals(concurrentCount, updated.heartCount)
    }

    @Test
    fun `incrementViewCount - success - increments view count correctly under high concurrency`() = runBlocking {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        val concurrentCount = 100

        val jobs = (1..concurrentCount).map {
            async(Dispatchers.IO) {
                postUpdater.incrementViewCount(post.requireId())
            }
        }
        jobs.awaitAll()

        val updated = postRepository.findById(post.requireId())
        assertNotNull(updated)
        assertEquals(concurrentCount, updated.viewCount)
    }

    @Test
    fun `decrementHeartCount - success - decrements heart count correctly under high concurrency`() = runBlocking {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
            )
        )
        repeat(200) {
            postUpdater.incrementHeartCount(post.requireId())
        }

        val concurrentCount = 100

        val jobs = (1..concurrentCount).map {
            async(Dispatchers.IO) {
                postUpdater.decrementHeartCount(post.requireId())
            }
        }
        jobs.awaitAll()

        val updated = postRepository.findById(post.requireId())
        assertNotNull(updated)
        assertEquals(100, updated.heartCount)
    }
}

