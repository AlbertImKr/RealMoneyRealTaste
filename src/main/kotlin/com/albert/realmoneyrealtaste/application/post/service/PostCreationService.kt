package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.application.post.provided.PostCreator
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostCreationService(
    private val postRepository: PostRepository,
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
) : PostCreator {

    override fun createPost(memberId: Long, request: PostCreateRequest): Post {
        val member = memberReader.readMemberById(memberId)

        val post = Post.create(memberId, member.nickname.value, request.restaurant, request.content, request.images)

        val savedPost = postRepository.save(post)

        publishPostCreatedEvent(savedPost)

        return savedPost
    }

    /**
     * 게시글 생성 이벤트를 발행합니다.
     *
     * @param post 생성된 게시글
     */
    private fun publishPostCreatedEvent(post: Post) {
        eventPublisher.publishEvent(PostCreatedEvent(post.requireId(), post.author.memberId, post.restaurant.name))
    }
}
