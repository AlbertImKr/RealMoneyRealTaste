package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.value.Author
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import java.lang.reflect.Field

class PostFixture {

    companion object {
        const val DEFAULT_AUTHOR_MEMBER_ID = 1L
        const val DEFAULT_AUTHOR_NICKNAME = "테스트작성자"
        const val DEFAULT_RESTAURANT_NAME = "맛있는집"
        const val DEFAULT_RESTAURANT_ADDRESS = "서울시 강남구 테헤란로 123"
        const val DEFAULT_LATITUDE = 37.5665
        const val DEFAULT_LONGITUDE = 126.9780
        const val DEFAULT_CONTENT_TEXT = "정말 맛있었어요! 강추합니다."
        const val DEFAULT_RATING = 5
        val DEFAULT_IMAGE_URLS = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        )

        val DEFAULT_AUTHOR = Author(DEFAULT_AUTHOR_MEMBER_ID, DEFAULT_AUTHOR_NICKNAME)

        val DEFAULT_RESTAURANT = Restaurant(
            name = DEFAULT_RESTAURANT_NAME,
            address = DEFAULT_RESTAURANT_ADDRESS,
            latitude = DEFAULT_LATITUDE,
            longitude = DEFAULT_LONGITUDE
        )

        val DEFAULT_CONTENT = PostContent(
            text = DEFAULT_CONTENT_TEXT,
            rating = DEFAULT_RATING
        )

        val DEFAULT_IMAGES = PostImages(DEFAULT_IMAGE_URLS)

        fun createImages(count: Int): PostImages {
            return PostImages((1..count).map {
                val randomId = (1000..9999).random() // 1000~9999 범위의 랜덤 숫자
                "https://example.com/image$randomId.jpg"
            })
        }

        /**
         * 기본 Post 생성
         */
        fun createPost(
            authorMemberId: Long = DEFAULT_AUTHOR_MEMBER_ID,
            authorNickname: String = DEFAULT_AUTHOR_NICKNAME,
            restaurant: Restaurant = DEFAULT_RESTAURANT,
            content: PostContent = DEFAULT_CONTENT,
            images: PostImages = DEFAULT_IMAGES,
        ): Post {
            return Post.create(
                authorMemberId = authorMemberId,
                authorNickname = authorNickname,
                restaurant = restaurant,
                content = content,
                images = images
            )
        }

        fun createContent(text: String, rating: Int): PostContent {
            return PostContent(
                text = text,
                rating = rating
            )
        }

        /**
         * 이미지가 없는 Post 생성
         */
        fun createPostWithoutImages(): Post {
            return createPost(images = PostImages.empty())
        }

        /**
         * 특정 평점의 Post 생성
         */
        fun createPostWithRating(rating: Int): Post {
            val content = PostContent(DEFAULT_CONTENT_TEXT, rating)
            return createPost(content = content)
        }

        /**
         * ID를 설정하는 헬퍼 메서드 (테스트용)
         */
        fun setId(post: Post, id: Long) {
            val idField: Field = post.javaClass.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(post, id)
        }
    }
}
