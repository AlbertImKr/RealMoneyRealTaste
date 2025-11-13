package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.domain.post.Post

/**
 * 게시글 수정 기능을 제공하는 인터페이스
 */
interface PostUpdater {

    /**
     * 게시글을 수정합니다.
     *
     * @param postId 수정할 게시글 ID
     * @param memberId 요청한 회원 ID
     * @param request 수정 요청 데이터
     * @return 수정된 게시글
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    fun updatePost(postId: Long, memberId: Long, request: PostUpdateRequest): Post

    /**
     * 게시글을 삭제합니다 (Soft Delete).
     *
     * @param postId 삭제할 게시글 ID
     * @param memberId 요청한 회원 ID
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    fun deletePost(postId: Long, memberId: Long)

    /**
     * 게시글의 좋아요 수를 1 증가시킵니다.
     *
     * @param postId 좋아요 수를 증가시킬 게시글 ID
     */
    fun incrementHeartCount(postId: Long)

    /**
     * 게시글의 좋아요 수를 1 감소시킵니다.
     *
     * @param postId 좋아요 수를 감소시킬 게시글 ID
     */
    fun decrementHeartCount(postId: Long)

    /**
     * 게시글의 조회 수를 1 증가시킵니다.
     *
     * @param postId 조회 수를 증가시킬 게시글 ID
     */
    fun incrementViewCount(postId: Long)
}
