package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.application.post.dto.PostSearchCondition
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.domain.post.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 게시글 조회 기능을 제공하는 인터페이스
 */
interface PostReader {

    /**
     * 게시글 ID로 게시글을 조회합니다.
     *
     * @param memberId 조회하는 회원 ID (View 증가용)
     * @param postId 조회할 게시글 ID
     * @return 조회된 게시글
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    fun readPostById(memberId: Long, postId: Long): Post

    /**
     * 특정 회원이 작성한 게시글 목록을 조회합니다.
     *
     * @param memberId 작성자 회원 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    fun readPostsByMember(memberId: Long, pageable: Pageable): Page<Post>

    /**
     * 게시글 ID로 게시글 존재 여부를 확인합니다.
     *
     * @param postId 조회할 게시글 ID
     * @return 게시글 존재 여부
     */
    fun existById(postId: Long): Boolean

    /**
     * 맛집 이름으로 게시글을 검색합니다.
     *
     * @param restaurantName 맛집 이름
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    fun searchPostsByRestaurantName(restaurantName: String, pageable: Pageable): Page<Post>

    /**
     * 조건에 따라 게시글을 검색합니다.
     *
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    fun searchPosts(condition: PostSearchCondition, pageable: Pageable): Page<Post>

    /**
     * 모든 게시글을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 모든 게시글 목록 (페이징)
     */
    fun readAllPosts(pageable: Pageable): Page<Post>
}
