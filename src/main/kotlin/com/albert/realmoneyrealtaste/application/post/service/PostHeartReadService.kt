package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.post.provided.PostHeartReader
import com.albert.realmoneyrealtaste.application.post.required.PostHeartRepository
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import org.springframework.stereotype.Service

@Service
class PostHeartReadService(
    private val postHeartRepository: PostHeartRepository,
) : PostHeartReader {

    override fun findByMemberIdAndPostIds(memberId: Long, postIds: List<Long>): List<PostHeart> {
        return postHeartRepository.findByMemberIdAndPostIdIn(memberId, postIds)
    }
}
