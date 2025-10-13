package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
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

    override fun addHeart(postId: Long, memberId: Long) {
        if (!postReader.existById(postId)) throw PostNotFoundException("게시글을 찾을 수 없습니다: $postId")

        if (postHeartRepository.existsByPostIdAndMemberId(postId, memberId)) return

        postHeartRepository.save(PostHeart.create(postId, memberId))

        eventPublisher.publishEvent(PostHeartAddedEvent(postId, memberId))
    }

    override fun removeHeart(postId: Long, memberId: Long) {
        if (!postReader.existById(postId)) throw PostNotFoundException("게시글을 찾을 수 없습니다: $postId")

        if (!postHeartRepository.existsByPostIdAndMemberId(postId, memberId)) return

        postHeartRepository.deleteByPostIdAndMemberId(postId, memberId)

        eventPublisher.publishEvent(PostHeartRemovedEvent(postId, memberId))
    }

    @Transactional(readOnly = true)
    override fun hasHeart(postId: Long, memberId: Long): Boolean {
        return postHeartRepository.existsByPostIdAndMemberId(postId, memberId)
    }
}
