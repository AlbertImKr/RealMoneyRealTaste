package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.post.exception.PostAddHeartException
import com.albert.realmoneyrealtaste.application.post.exception.PostRemoveHeartException
import com.albert.realmoneyrealtaste.application.post.provided.PostHeartManager
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.application.post.required.PostHeartRepository
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartAddedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartRemovedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PostHeartService(
    private val postReader: PostReader,
    private val postHeartRepository: PostHeartRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PostHeartManager {

    companion object {
        const val ERROR_POST_ADD_HEART = "게시글 좋아요 추가에 실패했습니다."
        const val ERROR_POST_REMOVE_HEART = "게시글 좋아요 제거에 실패했습니다."
    }

    override fun addHeart(postId: Long, memberId: Long) {
        try {
            postReader.readPostById(memberId, postId)

            if (postHeartRepository.existsByPostIdAndMemberId(postId, memberId)) return

            postHeartRepository.save(PostHeart.create(postId, memberId))

            eventPublisher.publishEvent(PostHeartAddedEvent(postId, memberId))
        } catch (e: IllegalArgumentException) {
            throw PostAddHeartException(ERROR_POST_ADD_HEART, e)
        }
    }

    override fun removeHeart(postId: Long, memberId: Long) {
        try {
            postReader.readPostById(memberId, postId)

            if (!postHeartRepository.existsByPostIdAndMemberId(postId, memberId)) return

            postHeartRepository.deleteByPostIdAndMemberId(postId, memberId)

            eventPublisher.publishEvent(PostHeartRemovedEvent(postId, memberId))
        } catch (e: IllegalArgumentException) {
            throw PostRemoveHeartException(ERROR_POST_REMOVE_HEART, e)
        }
    }
}
