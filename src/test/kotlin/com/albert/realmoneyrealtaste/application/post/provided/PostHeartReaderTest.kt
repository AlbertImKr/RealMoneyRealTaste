package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.required.PostHeartRepository
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import kotlin.test.Test

class PostHeartReaderTest(
    private val postHeartReader: PostHeartReader,
    private val postHeartRepository: PostHeartRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Test
    fun `findByMemberIdAndPostIds - success - retrieves hearts for given member and post IDs`() {
        // given
        val member = testMemberHelper.createActivatedMember()
        val memberId = member.requireId()
        val postIds = listOf(1L, 2L, 3L).apply {
            forEach { postId ->
                postHeartRepository.save(PostHeart.create(memberId = memberId, postId = postId))
            }
        }

        // when
        val result = postHeartReader.findByMemberIdAndPostIds(memberId, postIds)

        // then
        assert(result.size == 3)
        assert(result.map { it.postId }.toSet() == postIds.toSet())
    }

    @Test
    fun `findByMemberIdAndPostIds - success - returns empty list when no hearts found`() {
        // given
        val member = testMemberHelper.createActivatedMember()
        val memberId = member.requireId()
        val postIds = listOf(1L, 2L, 3L)

        // when
        val result = postHeartReader.findByMemberIdAndPostIds(memberId, postIds)

        // then
        assert(result.isEmpty())
    }

    @Test
    fun `findByMemberIdAndPostIds - success - returns empty list when postIds is empty`() {
        // given
        val member = testMemberHelper.createActivatedMember()
        val memberId = member.requireId()
        val postIds = emptyList<Long>()

        // when
        val result = postHeartReader.findByMemberIdAndPostIds(memberId, postIds)

        // then
        assert(result.isEmpty())
    }

    @Test
    fun `hasHeart - success - returns true when heart exists`() {
        // given
        val member = testMemberHelper.createActivatedMember()
        val memberId = member.requireId()
        val postId = 1L
        postHeartRepository.save(PostHeart.create(memberId = memberId, postId = postId))

        // when
        val result = postHeartReader.hasHeart(postId, memberId)

        // then
        assert(result)
    }
}
