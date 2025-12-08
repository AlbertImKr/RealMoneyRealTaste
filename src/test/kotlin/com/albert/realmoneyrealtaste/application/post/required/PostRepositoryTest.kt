package com.albert.realmoneyrealtaste.application.post.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.dto.PostSearchCondition
import com.albert.realmoneyrealtaste.domain.post.PostStatus
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestPostHelper
import org.springframework.data.domain.PageRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostRepositoryTest(
    val postRepository: PostRepository,
    val testPostHelper: TestPostHelper,
) : IntegrationTestBase() {

    @Test
    fun `save - success - saves and returns post with ID`() {
        val post = PostFixture.createPost(
            authorMemberId = 1L,
            authorNickname = "작성자",
            restaurant = Restaurant(
                name = "새로운 맛집",
                address = "서울시 강남구",
                latitude = 37.5,
                longitude = 127.0
            ),
            content = PostContent(
                text = "새로운 게시글",
                rating = 4
            )
        )

        val savedPost = postRepository.save(post)

        assertNotNull(savedPost.id)
        assertTrue(savedPost.requireId() > 0)
        assertEquals("작성자", savedPost.author.nickname)
        assertEquals("새로운 맛집", savedPost.restaurant.name)
        assertEquals("새로운 게시글", savedPost.content.text)
    }

    @Test
    fun `findById - success - returns post when exists`() {
        val createdPost = testPostHelper.createPost(authorMemberId = 1L)

        val foundPost = postRepository.findById(createdPost.requireId())

        assertNotNull(foundPost)
        assertEquals(createdPost.id, foundPost.id)
        assertEquals(createdPost.author.nickname, foundPost.author.nickname)
    }

    @Test
    fun `findById - success - returns null when not exists`() {
        val foundPost = postRepository.findById(99999L)

        assertNull(foundPost)
    }

    @Test
    fun `findByAuthorMemberIdAndStatusNot - success - returns author's posts excluding deleted`() {
        val authorId = 1L

        // 작성자의 게시글 3개 생성
        val post1 = testPostHelper.createPost(authorMemberId = authorId)
        val post2 = testPostHelper.createPost(authorMemberId = authorId)
        val post3 = testPostHelper.createPost(authorMemberId = authorId)

        // 다른 작성자의 게시글
        testPostHelper.createPost(authorMemberId = 2L)

        // 삭제 상태의 게시글
        val deletedPost = testPostHelper.createPost(authorMemberId = authorId)
        deletedPost.delete(authorId)
        postRepository.save(deletedPost)

        // 조회
        val result = postRepository.findByAuthorMemberIdAndStatusNot(
            memberId = authorId,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(10, result.size)
        assertEquals(3, result.totalElements)
        assertEquals(1, result.totalPages)

        val postIds = result.content.map { it.id }
        assertTrue(postIds.contains(post1.id))
        assertTrue(postIds.contains(post2.id))
        assertTrue(postIds.contains(post3.id))
        assertFalse(postIds.contains(deletedPost.id))
    }

    @Test
    fun `searchByRestaurantNameContainingAndStatusNot - success - finds posts by restaurant name`() {
        // 맛집 이름으로 게시글 생성
        val post1 = testPostHelper.createPost(
            authorMemberId = 1L,
            restaurant = Restaurant(
                name = "김치찌집",
                address = "서울시",
                latitude = 37.5,
                longitude = 127.0
            )
        )

        val post2 = testPostHelper.createPost(
            authorMemberId = 2L,
            restaurant = Restaurant(
                name = "김치나라",
                address = "부산시",
                latitude = 35.0,
                longitude = 129.0
            )
        )

        // 다른 이름의 게시글
        testPostHelper.createPost(
            authorMemberId = 3L,
            restaurant = Restaurant(
                name = "된장찌개",
                address = "대구시",
                latitude = 35.8,
                longitude = 128.5
            )
        )

        // "김치"로 검색
        val result = postRepository.searchByRestaurantNameContainingAndStatusNot(
            name = "김치",
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(10, result.size)
        assertEquals(2, result.totalElements)
        val postIds = result.content.map { it.id }
        assertTrue(postIds.contains(post1.id))
        assertTrue(postIds.contains(post2.id))
    }

    @Test
    fun `searchByCondition - success - searches with all conditions`() {
        // 데이터 생성
        val post1 = testPostHelper.createPost(
            authorMemberId = 1L,
            authorNickname = "맛집탐험가",
            restaurant = Restaurant(
                name = "맛있는김치찌개",
                address = "서울시",
                latitude = 37.5,
                longitude = 127.0
            ),
            content = PostContent(text = "최고!", rating = 5)
        )

        val post2 = testPostHelper.createPost(
            authorMemberId = 2L,
            authorNickname = "맛집탐험가",
            restaurant = Restaurant(
                name = "맛있는된장찌개",
                address = "부산시",
                latitude = 35.0,
                longitude = 129.0
            ),
            content = PostContent(text = "좋아요", rating = 3)
        )

        // 조건 검색
        val condition = PostSearchCondition(
            restaurantName = "맛있는",
            authorNickname = "맛집탐험가",
            minRating = 3,
            maxRating = 5
        )

        val result = postRepository.searchByCondition(condition, PageRequest.of(0, 10))

        assertEquals(10, result.size)
        assertEquals(2, result.totalElements)
        val postIds = result.content.map { it.id }
        assertTrue(postIds.contains(post1.id))
        assertTrue(postIds.contains(post2.id))
    }

    @Test
    fun `searchByCondition - success - searches with partial null conditions`() {
        val post1 = testPostHelper.createPost(
            authorMemberId = 1L,
            authorNickname = "작성자A",
            content = PostContent(text = "별로", rating = 2)
        )

        val post2 = testPostHelper.createPost(
            authorMemberId = 2L,
            authorNickname = "작성자B",
            content = PostContent(text = "최고", rating = 5)
        )

        // 최소 평점만 지정
        val condition = PostSearchCondition(
            restaurantName = null,
            authorNickname = null,
            minRating = 4,
            maxRating = null
        )

        val result = postRepository.searchByCondition(condition, PageRequest.of(0, 10))

        assertEquals(10, result.size)
        assertEquals(1, result.totalElements)
        assertEquals(post2.id, result.content[0].id)
    }

    @Test
    fun `existsByIdAndStatusNot - success - returns true for active post`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)

        val exists = postRepository.existsByIdAndStatusNot(post.requireId(), PostStatus.DELETED)

        assertTrue(exists)
    }

    @Test
    fun `existsByIdAndStatusNot - success - returns false for deleted post`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        post.delete(1L)
        val savedPost = postRepository.save(post)

        val exists = postRepository.existsByIdAndStatusNot(savedPost.requireId(), PostStatus.DELETED)

        assertFalse(exists)
    }

    @Test
    fun `incrementHeartCount - success - increases heart count`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        val initialCount = post.heartCount

        postRepository.incrementHeartCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(initialCount + 1, updatedPost.heartCount)
    }

    @Test
    fun `decrementHeartCount - success - decreases heart count`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        // 먼저 증가시킴
        postRepository.incrementHeartCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val postWithHeart = postRepository.findById(post.requireId())
        assertNotNull(postWithHeart)
        val initialCount = postWithHeart.heartCount

        // 감소시킴
        postRepository.decrementHeartCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(initialCount - 1, updatedPost.heartCount)
    }

    @Test
    fun `decrementHeartCount - success - does not go below zero`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)

        // 0일 때 감소 시도
        postRepository.decrementHeartCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(0, updatedPost.heartCount)
    }

    @Test
    fun `incrementViewCount - success - increases view count`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        val initialCount = post.viewCount

        postRepository.incrementViewCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(initialCount + 1, updatedPost.viewCount)
    }

    @Test
    fun `findAllByStatusNot - success - returns all posts excluding deleted`() {
        val post1 = testPostHelper.createPost(authorMemberId = 1L)
        val post2 = testPostHelper.createPost(authorMemberId = 2L)

        val deletedPost = testPostHelper.createPost(authorMemberId = 3L)
        deletedPost.delete(3L)
        postRepository.save(deletedPost)

        val result = postRepository.findAllByStatusNot(
            status = PostStatus.DELETED,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(10, result.size)
        assertEquals(2, result.totalElements)
        val postIds = result.content.map { it.id }
        assertTrue(postIds.contains(post1.id))
        assertTrue(postIds.contains(post2.id))
        assertFalse(postIds.contains(deletedPost.id))
    }

    @Test
    fun `existsByIdAndStatus - success - checks specific status`() {
        val activePost = testPostHelper.createPost(authorMemberId = 1L)
        val deletedPost = testPostHelper.createPost(authorMemberId = 2L)
        deletedPost.delete(2L)
        val savedDeletedPost = postRepository.save(deletedPost)

        assertTrue(postRepository.existsByIdAndStatus(activePost.requireId(), PostStatus.PUBLISHED))
        assertFalse(postRepository.existsByIdAndStatus(activePost.requireId(), PostStatus.DELETED))
        assertTrue(postRepository.existsByIdAndStatus(savedDeletedPost.requireId(), PostStatus.DELETED))
        assertFalse(postRepository.existsByIdAndStatus(savedDeletedPost.requireId(), PostStatus.PUBLISHED))
    }

    @Test
    fun `countByAuthorMemberIdAndStatusNot - success - counts author's posts`() {
        val authorId = 1L

        testPostHelper.createPost(authorMemberId = authorId)
        testPostHelper.createPost(authorMemberId = authorId)
        testPostHelper.createPost(authorMemberId = authorId)

        testPostHelper.createPost(authorMemberId = 2L)

        val deletedPost = testPostHelper.createPost(authorMemberId = authorId)
        deletedPost.delete(authorId)
        postRepository.save(deletedPost)

        val count = postRepository.countByAuthorMemberIdAndStatusNot(authorId, PostStatus.DELETED)

        assertEquals(3, count)
    }

    @Test
    fun `findAllByStatusAndIdIn - success - finds posts by IDs`() {
        val post1 = testPostHelper.createPost(authorMemberId = 1L)
        val post2 = testPostHelper.createPost(authorMemberId = 2L)
        val post3 = testPostHelper.createPost(authorMemberId = 3L)

        val deletedPost = testPostHelper.createPost(authorMemberId = 4L)
        deletedPost.delete(4L)
        postRepository.save(deletedPost)

        val posts = postRepository.findAllByStatusAndIdIn(
            status = PostStatus.PUBLISHED,
            ids = listOf(post1.requireId(), post2.requireId(), post3.requireId(), deletedPost.requireId())
        )

        assertEquals(3, posts.size)
        val postIds = posts.map { it.id }
        assertTrue(postIds.contains(post1.id))
        assertTrue(postIds.contains(post2.id))
        assertTrue(postIds.contains(post3.id))
        assertFalse(postIds.contains(deletedPost.id))
    }

    @Test
    fun `findAllByStatusAndIdIn - success - returns empty list for empty IDs`() {
        val posts = postRepository.findAllByStatusAndIdIn(
            status = PostStatus.PUBLISHED,
            ids = emptyList()
        )

        assertTrue(posts.isEmpty())
        assertEquals(0, posts.size)
    }

    @Test
    fun `incrementCommentCount - success - increases comment count`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        val initialCount = post.commentCount

        postRepository.incrementCommentCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(initialCount + 1, updatedPost.commentCount)
    }

    @Test
    fun `decrementCommentCount - success - decreases comment count`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        // 먼저 증가시킴
        postRepository.incrementCommentCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val postWithComment = postRepository.findById(post.requireId())
        assertNotNull(postWithComment)
        val initialCount = postWithComment.commentCount

        // 감소시킴
        postRepository.decrementCommentCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(initialCount - 1, updatedPost.commentCount)
    }

    @Test
    fun `decrementCommentCount - success - does not go below zero`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)

        // 0일 때 감소 시도
        postRepository.decrementCommentCount(post.requireId())
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals(0, updatedPost.commentCount)
    }

    @Test
    fun `updateAuthorInfo - success - updates all author fields`() {
        val authorId = 1L
        val post1 = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임"
        )
        val post2 = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임"
        )

        // 작성자 정보 업데이트
        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = "새로운닉네임",
            introduction = "새로운소개",
            imageId = 999L
        )
        entityManager.flush()
        entityManager.clear()

        // 확인
        val updatedPost1 = postRepository.findById(post1.requireId())
        val updatedPost2 = postRepository.findById(post2.requireId())

        assertNotNull(updatedPost1)
        assertNotNull(updatedPost2)

        assertEquals("새로운닉네임", updatedPost1.author.nickname)
        assertEquals("새로운소개", updatedPost1.author.introduction)
        assertEquals(999L, updatedPost1.author.imageId)

        assertEquals("새로운닉네임", updatedPost2.author.nickname)
        assertEquals("새로운소개", updatedPost2.author.introduction)
        assertEquals(999L, updatedPost2.author.imageId)
    }

    @Test
    fun `updateAuthorInfo - success - updates only nickname`() {
        val authorId = 1L
        val post = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임"
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = "바뀐닉네임만",
            introduction = null,
            imageId = null
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)

        assertEquals("바뀐닉네임만", updatedPost.author.nickname)
        assertEquals(PostFixture.DEFAULT_AUTHOR_INTRODUCTION, updatedPost.author.introduction)
        assertEquals(1L, updatedPost.author.imageId)
    }

    @Test
    fun `updateAuthorInfo - success - does not affect other authors' posts`() {
        val author1Id = 1L
        val author2Id = 2L

        val post1 = testPostHelper.createPost(
            authorMemberId = author1Id,
            authorNickname = "작성자1"
        )
        val post2 = testPostHelper.createPost(
            authorMemberId = author2Id,
            authorNickname = "작성자2"
        )

        // author1만 업데이트
        postRepository.updateAuthorInfo(
            authorMemberId = author1Id,
            nickname = "수정된작성자1",
            introduction = null,
            imageId = null
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost1 = postRepository.findById(post1.requireId())
        val updatedPost2 = postRepository.findById(post2.requireId())

        assertNotNull(updatedPost1)
        assertNotNull(updatedPost2)

        assertEquals("수정된작성자1", updatedPost1.author.nickname)
        assertEquals("작성자2", updatedPost2.author.nickname)
    }

    @Test
    fun `updateAuthorInfo - success - updates only imageId`() {
        val authorId = 1L
        val post = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = null,
            introduction = null,
            imageId = 999L
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals("기존닉네임", updatedPost.author.nickname) // 닉네임 보존
        assertEquals(999L, updatedPost.author.imageId) // 이미지만 업데이트
    }

    @Test
    fun `updateAuthorInfo - success - updates only introduction`() {
        val authorId = 1L
        val post = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = null,
            introduction = "새로운소개글",
            imageId = null
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals("기존닉네임", updatedPost.author.nickname) // 닉네임 보존
        assertEquals("새로운소개글", updatedPost.author.introduction) // 소개글만 업데이트
        assertEquals(post.author.imageId, updatedPost.author.imageId) // 이미지 ID 보존
    }

    @Test
    fun `updateAuthorInfo - success - all null preserves existing values`() {
        val authorId = 1L
        val originalNickname = "원본닉네임"

        val post = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = originalNickname,
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = null,
            introduction = null,
            imageId = null
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        // 모든 값이 원본 그대로 보존되어야 함
        assertEquals(originalNickname, updatedPost.author.nickname)
        assertEquals(post.author.introduction, updatedPost.author.introduction)
        assertEquals(post.author.imageId, updatedPost.author.imageId)
    }

    @Test
    fun `updateAuthorInfo - success - updates nickname and imageId with null introduction`() {
        val authorId = 1L
        val originalIntroduction = "원본소개"

        val post = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = "새닉네임",
            introduction = null,
            imageId = 777L
        )
        entityManager.flush()
        entityManager.clear()

        val updatedPost = postRepository.findById(post.requireId())
        assertNotNull(updatedPost)
        assertEquals("새닉네임", updatedPost.author.nickname) // 닉네임 업데이트
        assertEquals(post.author.introduction, updatedPost.author.introduction) // 소개는 보존
        assertEquals(777L, updatedPost.author.imageId) // 이미지 ID 업데이트
    }

    @Test
    fun `updateAuthorInfo - success - updates multiple posts of same author`() {
        val authorId = 1L

        val post1 = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )
        val post2 = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )
        val post3 = testPostHelper.createPost(
            authorMemberId = authorId,
            authorNickname = "기존닉네임",
        )

        postRepository.updateAuthorInfo(
            authorMemberId = authorId,
            nickname = "통일닉네임",
            introduction = null,
            imageId = 999L
        )
        entityManager.flush()
        entityManager.clear()

        // 모든 포스트가 업데이트되어야 함
        val updatedPost1 = postRepository.findById(post1.requireId())
        val updatedPost2 = postRepository.findById(post2.requireId())
        val updatedPost3 = postRepository.findById(post3.requireId())

        assertNotNull(updatedPost1)
        assertNotNull(updatedPost2)
        assertNotNull(updatedPost3)

        // 모든 포스트에서 동일한 값 확인
        listOf(updatedPost1, updatedPost2, updatedPost3).forEach { post ->
            assertEquals("통일닉네임", post.author.nickname)
            assertEquals(999L, post.author.imageId)
        }
    }
}
