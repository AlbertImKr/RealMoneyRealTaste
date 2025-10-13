package com.albert.realmoneyrealtaste.application.post.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.dto.PostSearchCondition
import com.albert.realmoneyrealtaste.application.post.exception.PostNotFoundException
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RecordApplicationEvents
class PostReaderTest(
    private val postReader: PostReader,
    private val postRepository: PostRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `readPostById - success - author reads their own post`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value
            )
        )
        applicationEvents.clear()

        val result = postReader.readPostById(authorMemberId, post.requireId())

        assertAll(
            { assertEquals(post.requireId(), result.requireId()) },
            { assertEquals(post.author.memberId, result.author.memberId) },
            { assertEquals(post.author.nickname, result.author.nickname) },
            { assertEquals(post.restaurant.name, result.restaurant.name) },
            { assertEquals(post.content.text, result.content.text) },
            { assertEquals(post.content.rating, result.content.rating) },
            { assertEquals(post.images.urls, result.images.urls) },
            { assertEquals(post.status, result.status) },
            { assertEquals(post.heartCount, result.heartCount) },
            { assertEquals(post.viewCount, result.viewCount) },
            { assertEquals(0, applicationEvents.stream().count()) }
        )
    }

    @Test
    fun `readPostById - success - non-author reads the post`() {
        val author = testMemberHelper.createActivatedMember()
        val nonAuthor = testMemberHelper.createActivatedMember(
            email = MemberFixture.OTHER_EMAIL,
            nickname = MemberFixture.OTHER_NICKNAME
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        applicationEvents.clear()

        val result = postReader.readPostById(nonAuthor.requireId(), post.requireId())

        assertAll(
            { assertEquals(post.requireId(), result.requireId()) },
            { assertEquals(post.author.memberId, result.author.memberId) },
            { assertEquals(post.author.nickname, result.author.nickname) },
            { assertEquals(post.restaurant.name, result.restaurant.name) },
            { assertEquals(post.content.text, result.content.text) },
            { assertEquals(post.content.rating, result.content.rating) },
            { assertEquals(post.images.urls, result.images.urls) },
            { assertEquals(post.status, result.status) },
            { assertEquals(post.heartCount, result.heartCount) },
            { assertEquals(post.viewCount, result.viewCount) },
            { assertEquals(1, applicationEvents.stream().count()) }
        )
    }

    @Test
    fun `readPostById - failure - post not found`() {
        val member = testMemberHelper.createActivatedMember()

        assertFailsWith<PostNotFoundException> {
            postReader.readPostById(member.requireId(), 999L)
        }.let {
            assertTrue(it.message!!.contains("게시글을 찾을 수 없습니다"))
        }
    }

    @Test
    fun `readPostById - failure - post is deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
            )
        )
        post.delete(member.requireId())
        flushAndClear()

        assertFailsWith<PostNotFoundException> {
            postReader.readPostById(member.requireId(), post.requireId())
        }.let {
            assertTrue(it.message!!.contains("삭제된 게시글입니다"))
        }
    }

    @Test
    fun `readPostsByMember - success - reads posts by member`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        val posts = (1..3).map {
            postRepository.save(
                PostFixture.createPost(
                    authorMemberId = authorMemberId,
                    authorNickname = member.nickname.value,
                    images = PostImages.of("https://example.com/${it}1.jpg", "https://example.com/${it}2.jpg")
                )
            )
        }
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        flushAndClear()

        val result = postReader.readPostsByMember(authorMemberId, pageRequest)

        assertFalse(result.isEmpty)
        assertEquals(3, result.totalElements)
        result.forEachIndexed { index, post ->
            assertEquals(posts[2 - index].requireId(), post.requireId())
            assertEquals(posts[2 - index].author.memberId, post.author.memberId)
            assertEquals(posts[2 - index].author.nickname, post.author.nickname)
            assertEquals(posts[2 - index].restaurant.name, post.restaurant.name)
            assertEquals(posts[2 - index].content.text, post.content.text)
            assertEquals(posts[2 - index].content.rating, post.content.rating)
            assertEquals(posts[2 - index].images.urls, post.images.urls)
            assertEquals(posts[2 - index].status, post.status)
            assertEquals(posts[2 - index].heartCount, post.heartCount)
            assertEquals(posts[2 - index].viewCount, post.viewCount)
        }
    }

    @Test
    fun `readPostsByMember - success - returns empty list when member has no posts`() {
        val member = testMemberHelper.createActivatedMember()

        val result =
            postReader.readPostsByMember(member.requireId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))

        assertTrue(result.isEmpty)
    }

    @Test
    fun `readPostsByMember - success - excludes deleted posts`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        val post1 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                images = PostImages.of("https://example.com/1.jpg", "https://example.com/2.jpg"),
            )
        )
        val post2 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                images = PostImages.of("https://example.com/1.jpg", "https://example.com/2.jpg"),
            )
        )
        post2.delete(authorMemberId)
        flushAndClear()

        val result =
            postReader.readPostsByMember(authorMemberId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))

        assertFalse(result.isEmpty)
        assertEquals(1, result.totalElements)
        assertEquals(post1.requireId(), result.content[0].requireId())
    }

    @Test
    fun `existById - success - returns true if post exists and is not deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
            )
        )
        flushAndClear()

        val exists = postReader.existById(post.requireId())

        assertTrue(exists)
    }

    @Test
    fun `existById - success - returns false if post does not exist`() {
        val exists = postReader.existById(999L)

        assertFalse(exists)
    }

    @Test
    fun `existById - success - returns false if post is deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
            )
        )
        post.delete(member.requireId())
        flushAndClear()

        val exists = postReader.existById(post.requireId())

        assertFalse(exists)
    }

    @Test
    fun `searchPostsByRestaurantName - success - finds posts by restaurant name containing keyword`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        val post1 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780)
            )
        )
        val post2 = postRepository.save(
            PostFixture.createPost(
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                images = PostFixture.createImages(2)
            )
        )
        val post3 = postRepository.save(
            PostFixture.createPost(
                restaurant = Restaurant("별로인집", "서울시 강남구", 37.5665, 126.9780),
                images = PostFixture.createImages(2)
            )
        )
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        flushAndClear()

        val result = postReader.searchPostsByRestaurantName("맛있는", pageRequest)

        val foundPostIds = result.content.map { it.requireId() }.toSet()
        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertTrue(foundPostIds.contains(post1.requireId())) },
            { assertTrue(foundPostIds.contains(post2.requireId())) },
            { assertFalse(foundPostIds.contains(post3.requireId())) }
        )
    }

    @Test
    fun `searchPostsByRestaurantName - success - returns empty when no posts match`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                images = PostFixture.createImages(2)
            )
        )
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        flushAndClear()

        val result = postReader.searchPostsByRestaurantName("없는식당", pageRequest)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `searchPostsByRestaurantName - success - excludes deleted posts`() {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        val post1 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                images = PostFixture.createImages(2)
            )
        )
        val post2 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                images = PostFixture.createImages(2)
            )
        )
        post2.delete(authorMemberId)
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        flushAndClear()

        val result = postReader.searchPostsByRestaurantName("맛있는", pageRequest)

        assertFalse(result.isEmpty)
        assertEquals(1, result.totalElements)
        assertEquals(post1.requireId(), result.content[0].requireId())
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "집, null, null, null, 3", // 1. restaurantName contains "집"
            "맛있는, null, null, null, 2", // 2. restaurantName contains "맛있는"
            "맛있는, defaultNick, null, null, 2", // 3. restaurantName contains "맛있는" and authorNickname is "defaultNick"
            "집, defaultNick, null, null, 3", // 4. restaurantName contains "집" and authorNickname is "defaultNick"
            "집, 다른작성자, null, null, 0", // 5. restaurantName contains "집" and authorNickname is "다른작성자"
            "null, defaultNick, null, null, 3", // 6. authorNickname is "defaultNick"
            "null, 다른작성자, null, null, 0", // 7. authorNickname is "다른작성자"
            "null, null, 4, null, 2", // 8. minRating >= 4
            "null, null, null, 3, 1", // 9. maxRating <= 3
            "null, null, 3, 4, 1", // 10. 3 <= rating <= 4
            "집, defaultNick, 4, null, 2", // 11. restaurantName contains "집" and authorNickname is "defaultNick" and minRating >= 4
            "집, defaultNick, null, 4, 2", // 12. restaurantName contains "집" and authorNickname is "defaultNick" and maxRating <= 4
            "집, defaultNick, 3, 5, 2", // 13. restaurantName contains "집" and authorNickname is "defaultNick" and 3 <= rating <= 5
        ],
        nullValues = ["null"]
    )
    fun `searchByCondition - success - finds posts by various conditions`(
        restaurantName: String?,
        authorNickname: String?,
        minRating: Int?,
        maxRating: Int?,
        expectedCount: Long,
    ) {
        val member = testMemberHelper.createActivatedMember()
        val authorMemberId = member.requireId()
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                content = PostFixture.createContent("맛있어요", 5),
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("맛있는집", "서울시 강남구", 37.5665, 126.9780),
                content = PostFixture.createContent("괜찮아요", 4),
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = authorMemberId,
                authorNickname = member.nickname.value,
                restaurant = Restaurant("별로인집", "서울시 강남구", 37.5665, 126.9780),
                content = PostFixture.createContent("별로에요", 2),
                images = PostFixture.createImages(2)
            )
        )
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        flushAndClear()
        val targetCondition = PostSearchCondition(
            restaurantName = restaurantName,
            authorNickname = authorNickname,
            minRating = minRating,
            maxRating = maxRating,
        )

        val result = postReader.searchPosts(targetCondition, pageRequest)

        assertEquals(expectedCount, result.totalElements)
    }
}
