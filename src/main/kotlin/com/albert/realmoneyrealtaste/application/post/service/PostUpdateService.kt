package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostDeleteException
import com.albert.realmoneyrealtaste.application.post.exception.PostUpdateException
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.application.post.provided.PostUpdater
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.Post
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PostUpdateService(
    private val postRepository: PostRepository,
    private val domainEventPublisher: DomainEventPublisher,
    private val postReader: PostReader,
) : PostUpdater {

    companion object {
        const val ERROR_POST_UPDATE = "포스트 수정에 실패했습니다. postId: %d, memberId: %d"
        const val ERROR_POST_DELETE = "포스트 삭제에 실패했습니다. postId: %d, memberId: %d"
    }

    override fun updatePost(postId: Long, memberId: Long, request: PostUpdateRequest): Post {
        try {
            val post = postReader.readPostById(memberId, postId)

            post.update(memberId, request.content, request.images, request.restaurant)

            return post
        } catch (e: IllegalArgumentException) {
            throw PostUpdateException(ERROR_POST_UPDATE.format(postId, memberId), e)
        }
    }

    override fun deletePost(postId: Long, memberId: Long) {
        try {
            val post = postReader.readPostById(memberId, postId)

            post.delete(memberId)

            // 도메인 이벤트 발행
            domainEventPublisher.publishFrom(post)
        } catch (e: IllegalArgumentException) {
            throw PostDeleteException(ERROR_POST_DELETE.format(postId, memberId), e)
        }
    }

    override fun incrementHeartCount(postId: Long) {
        postRepository.incrementHeartCount(postId)
    }

    override fun decrementHeartCount(postId: Long) {
        postRepository.decrementHeartCount(postId)
    }

    override fun incrementViewCount(postId: Long) {
        postRepository.incrementViewCount(postId)
    }
}
