package com.albert.realmoneyrealtaste.application.post.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostCreateException
import com.albert.realmoneyrealtaste.application.post.provided.PostCreator
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.Post
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostCreationService(
    private val postRepository: PostRepository,
    private val memberReader: MemberReader,
    private val domainEventPublisher: DomainEventPublisher,
) : PostCreator {

    companion object {
        const val ERROR_POST_CREATE = "포스트 생성에 실패했습니다."
    }

    override fun createPost(memberId: Long, request: PostCreateRequest): Post {
        try {
            val member = memberReader.readActiveMemberById(memberId)

            val post = Post.create(
                memberId,
                member.nickname.value,
                request.restaurant,
                request.content,
                request.images,
                member.detail.introduction?.value ?: "",
                member.profileImageId
            )

            val savedPost = postRepository.save(post)

            // 도메인 이벤트 발행
            domainEventPublisher.publishFrom(savedPost)

            return savedPost
        } catch (e: IllegalArgumentException) {
            throw PostCreateException(ERROR_POST_CREATE, e)
        }
    }
}
