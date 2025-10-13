package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.application.post.provided.PostUpdater
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PostUpdateService(
    private val postRepository: PostRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PostUpdater {

    override fun updatePost(postId: Long, memberId: Long, request: PostUpdateRequest): Post {
        val post = findPostByIdOrThrow(postId)

        post.update(memberId, request.content, request.images, request.restaurant)

        return post
    }

    override fun deletePost(postId: Long, memberId: Long) {
        val post = findPostByIdOrThrow(postId)

        post.delete(memberId)

        publishPostDeletedEvent(postId, memberId)
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

    /**
     * 게시글을 조회하거나 예외를 던집니다.
     *
     * @param postId 게시글 ID
     * @return 조회된 게시글
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    private fun findPostByIdOrThrow(postId: Long): Post {
        return postRepository.findById(postId)
            ?: throw PostNotFoundException("게시글을 찾을 수 없습니다: $postId")
    }

    /**
     * 게시글 삭제 이벤트를 발행합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     */
    private fun publishPostDeletedEvent(postId: Long, memberId: Long) {
        eventPublisher.publishEvent(
            PostDeletedEvent(
                postId = postId,
                authorMemberId = memberId
            )
        )
    }
}
