package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.domain.post.PostHeart

/**
 * 게시글 좋아요 조회 기능을 제공하는 인터페이스
 */
fun interface PostHeartReader {

    /**
     * 회원이 좋아요를 누른 게시글 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param postIds 조회할 게시글 ID 목록
     * @return 좋아요를 누른 게시글 ID 목록
     */
    fun findByMemberIdAndPostIds(memberId: Long, postIds: List<Long>): List<PostHeart>
}
