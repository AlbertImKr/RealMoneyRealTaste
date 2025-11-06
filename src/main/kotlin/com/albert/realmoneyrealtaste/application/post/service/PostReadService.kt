package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.dto.PostSearchCondition
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.PostStatus
import com.albert.realmoneyrealtaste.domain.post.event.PostViewedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostReadService(
    private val postRepository: PostRepository,
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
) : PostReader {

    override fun readPostById(memberId: Long, postId: Long): Post {
        memberReader.readMemberById(memberId)

        val post = findPostByIdOrThrow(postId)

        validatePostIsNotDeleted(post)

        publishViewEventIfNeeded(memberId, post)

        return post
    }

    override fun readPostByAuthorAndId(authorId: Long, postId: Long): Post {
        memberReader.readMemberById(authorId)

        val post = findPostByIdOrThrow(postId)

        validatePostIsNotDeleted(post)

        if (post.author.memberId != authorId) {
            throw PostNotFoundException("작성자가 아닌 사용자는 해당 게시글을 조회할 수 없습니다: $postId")
        }

        return post
    }

    override fun readPostsByMember(memberId: Long, pageable: Pageable): Page<Post> {
        memberReader.readMemberById(memberId)

        return postRepository.findByAuthorMemberIdAndStatusNot(memberId, pageable)
    }

    override fun existById(postId: Long): Boolean {
        return postRepository.existsByIdAndStatusNot(postId)
    }

    override fun searchPostsByRestaurantName(restaurantName: String, pageable: Pageable): Page<Post> {
        return postRepository.searchByRestaurantNameContainingAndStatusNot(restaurantName, pageable)
    }

    override fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post> {
        return postRepository.searchByCondition(condition, pageable)
    }

    override fun readAllPosts(pageable: Pageable): Page<Post> {
        return postRepository.findAllByStatusNot(PostStatus.DELETED, pageable)
    }

    override fun existsPublishedPostById(postId: Long): Boolean {
        return postRepository.existsByIdAndStatus(postId, PostStatus.PUBLISHED)
    }

    /**
     * 게시글을 조회하고 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param postId 게시글 ID
     * @return 조회된 게시글
     * @throws PostNotFoundException 게시글이 존재하지 않는 경우
     */
    private fun findPostByIdOrThrow(postId: Long): Post {
        return postRepository.findById(postId)
            ?: throw PostNotFoundException("게시글을 찾을 수 없습니다: $postId")
    }

    /**
     * 게시글이 삭제 상태가 아닌지 검증합니다.
     *
     * @param post 게시글
     * @throws PostNotFoundException 게시글이 삭제된 경우
     */
    private fun validatePostIsNotDeleted(post: Post) {
        if (post.status == PostStatus.DELETED) {
            throw PostNotFoundException("삭제된 게시글입니다: ${post.requireId()}")
        }
    }

    /**
     * 작성자가 아닌 경우 게시글 조회 이벤트를 발행합니다.
     *
     * @param memberId 조회자 회원 ID
     * @param post 조회된 게시글
     */
    private fun publishViewEventIfNeeded(memberId: Long, post: Post) {
        if (isNotAuthor(memberId, post)) {
            eventPublisher.publishEvent(
                PostViewedEvent(
                    postId = post.requireId(),
                    viewerMemberId = memberId,
                    authorMemberId = post.author.memberId,
                ),
            )
        }
    }

    /**
     * 조회자가 게시글 작성자가 아닌지 확인합니다.
     *
     * @param memberId 조회자 회원 ID
     * @param post 게시글
     * @return 작성자가 아니면 true, 작성자면 false
     */
    private fun isNotAuthor(memberId: Long, post: Post): Boolean {
        return post.author.memberId != memberId
    }
}
