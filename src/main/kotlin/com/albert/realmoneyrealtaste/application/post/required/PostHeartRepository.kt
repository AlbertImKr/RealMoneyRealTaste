package com.albert.realmoneyrealtaste.application.post.required

import com.albert.realmoneyrealtaste.domain.post.PostHeart
import org.springframework.data.repository.Repository

/**
 * 게시글 좋아요 관리를 위한 리포지토리
 */
interface PostHeartRepository : Repository<PostHeart, Long> {

    /**
     * PostHeart를 저장합니다.
     *
     * @param postHeart 저장할 PostHeart 객체
     * @return 저장된 PostHeart 객체
     */
    fun save(postHeart: PostHeart): PostHeart

    /**
     * 게시글 ID와 회원 ID로 좋아요를 조회합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     * @return 조회된 PostHeart 객체, 없으면 null
     */
    fun findByPostIdAndMemberId(postId: Long, memberId: Long): PostHeart?

    /**
     * 게시글 ID와 회원 ID로 좋아요를 삭제합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     */
    fun deleteByPostIdAndMemberId(postId: Long, memberId: Long)

    /**
     * 게시글 ID와 회원 ID로 좋아요가 존재하는지 확인합니다.
     *
     * @param postId 게시글 ID
     * @param memberId 회원 ID
     * @return 좋아요가 존재하면 true, 없으면 false
     */
    fun existsByPostIdAndMemberId(postId: Long, memberId: Long): Boolean

    /**
     * 게시글에 속한 모든 좋아요를 삭제합니다.
     *
     * @param postId 게시글 ID
     */
    fun deleteAllByPostId(postId: Long)

    /**
     * 회원이 누른 모든 좋아요를 삭제합니다.
     *
     * @param memberId 회원 ID
     */
    fun deleteAllByMemberId(memberId: Long)

    /**
     * 특정 게시글에 달린 좋아요 수를 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    fun countByPostId(postId: Long): Long

    /**
     * 회원이 좋아요를 누른 게시글 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param postIds 조회할 게시글 ID 목록
     * @return 좋아요를 누른 게시글 ID 목록
     */
    fun findByMemberIdAndPostIdIn(memberId: Long, postIds: List<Long>): List<PostHeart>
}
