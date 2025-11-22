package com.albert.realmoneyrealtaste.application.post.required

import com.albert.realmoneyrealtaste.application.post.dto.PostSearchCondition
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.PostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

/**
 * 게시글 관리를 위한 리포지토리 인터페이스
 */
interface PostRepository : Repository<Post, Long> {

    /**
     * 게시글을 저장합니다.
     *
     * @param post 저장할 게시글
     * @return 저장된 게시글
     */
    fun save(post: Post): Post

    /**
     * ID로 게시글을 조회합니다.
     *
     * @param id 조회할 게시글 ID
     * @return 조회된 게시글 또는 null
     */
    fun findById(id: Long): Post?

    /**
     * 특정 회원이 작성한 게시글 목록을 조회합니다.
     *
     * @param memberId 작성자 회원 ID
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    fun findByAuthorMemberIdAndStatusNot(
        memberId: Long,
        pageable: Pageable,
        status: PostStatus = PostStatus.DELETED,
    ): Page<Post>

    /**
     * 맛집 이름으로 게시글을 검색합니다.
     *
     * @param name 맛집 이름
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    fun searchByRestaurantNameContainingAndStatusNot(
        name: String,
        pageable: Pageable,
        status: PostStatus = PostStatus.DELETED,
    ): Page<Post>

    /**
     * 조건에 따라 게시글을 검색합니다.
     *
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    @Query(
        """
        SELECT p FROM Post p
        WHERE (:#{#condition.restaurantName} IS NULL OR p.restaurant.name LIKE %:#{#condition.restaurantName}%)
          AND (:#{#condition.authorNickname} IS NULL OR p.author.nickname = :#{#condition.authorNickname})
          AND (:#{#condition.minRating} IS NULL OR p.content.rating >= :#{#condition.minRating})
          AND (:#{#condition.maxRating} IS NULL OR p.content.rating <= :#{#condition.maxRating})
        """
    )
    fun searchByCondition(condition: PostSearchCondition, pageable: Pageable): Page<Post>

    /**
     * ID로 게시글 존재 여부를 확인합니다.
     *
     * @param id 조회할 게시글 ID
     * @param status 제외할 게시글 상태 (기본값: DELETED)
     * @return 게시글 존재 여부
     */
    fun existsByIdAndStatusNot(id: Long, status: PostStatus = PostStatus.DELETED): Boolean

    /**
     * 좋아요 수를 증가시킵니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE Post p SET p.heartCount = p.heartCount + 1 WHERE p.id = :postId")
    fun incrementHeartCount(postId: Long)

    /**
     * 좋아요 수를 감소시킵니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE Post p SET p.heartCount = p.heartCount - 1 WHERE p.id = :postId AND p.heartCount > 0")
    fun decrementHeartCount(postId: Long)

    /**
     * 조회 수를 증가시킵니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    fun incrementViewCount(postId: Long)

    /**
     * 모든 게시글을 조회합니다.
     *
     * @param status 제외할 게시글 상태 (기본값: DELETED)
     * @param pageable 페이징 정보
     * @return 모든 게시글 목록 (페이징)
     */
    fun findAllByStatusNot(status: PostStatus, pageable: Pageable): Page<Post>

    /**
     * 특정 상태의 게시글 존재 여부를 확인합니다.
     *
     * @param id 조회할 게시글 ID
     * @param status 게시글 상태
     * @return 게시글 존재 여부
     */
    fun existsByIdAndStatus(id: Long, status: PostStatus): Boolean

    /**
     * 특정 회원이 작성한 게시글 수를 계산합니다.
     *
     * @param memberId 작성자 회원 ID
     * @param status 제외할 게시글 상태 (기본값: DELETED)
     * @return 게시글 수
     */
    fun countByAuthorMemberIdAndStatusNot(memberId: Long, status: PostStatus = PostStatus.DELETED): Long

    /**
     * 게시글 ID 목록으로 게시글들을 조회합니다.
     *
     * @param ids 게시글 ID 목록
     * @return 조회된 게시글 목록
     */
    fun findAllByStatusAndIdIn(status: PostStatus = PostStatus.PUBLISHED, ids: List<Long>): List<Post>
}
