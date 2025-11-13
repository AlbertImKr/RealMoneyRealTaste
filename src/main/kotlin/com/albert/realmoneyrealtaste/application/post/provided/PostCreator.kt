package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostCreateException
import com.albert.realmoneyrealtaste.domain.post.Post

/**
 * 게시글 생성 기능을 제공하는 인터페이스
 */
fun interface PostCreator {

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param memberId 작성자 회원 ID
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글
     * @throws PostCreateException 게시글 생성에 실패한 경우
     */
    fun createPost(memberId: Long, request: PostCreateRequest): Post
}
