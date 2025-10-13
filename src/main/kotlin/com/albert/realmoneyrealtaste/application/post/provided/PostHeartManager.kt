package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException

/**
 * 게시글 좋아요 관리 기능을 제공하는 인터페이스
 */
interface PostHeartManager {

    /**
     * 게시글에 좋아요를 추가합니다.
     * 동시성 제어를 위해 DB 레벨 업데이트를 사용합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    fun addHeart(postId: Long, memberId: Long)

    /**
     * 게시글에서 좋아요를 제거합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    fun removeHeart(postId: Long, memberId: Long)

    /**
     * 회원이 해당 게시글에 좋아요를 눌렀는지 확인합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     * @return 좋아요 여부
     */
    fun hasHeart(postId: Long, memberId: Long): Boolean
}
