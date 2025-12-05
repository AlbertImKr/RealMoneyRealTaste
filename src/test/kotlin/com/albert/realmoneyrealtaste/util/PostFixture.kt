package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.value.Author
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant

class PostFixture {

    companion object {
        const val DEFAULT_AUTHOR_MEMBER_ID = 1L
        const val DEFAULT_AUTHOR_NICKNAME = "테스트작성자"
        const val DEFAULT_AUTHOR_INTRODUCTION = "안녕하세요. 테스트 작성자입니다."
        const val DEFAULT_RESTAURANT_NAME = "맛있는집"
        const val DEFAULT_RESTAURANT_ADDRESS = "서울시 강남구 테헤란로 123"
        const val DEFAULT_LATITUDE = 37.5665
        const val DEFAULT_LONGITUDE = 126.9780
        const val DEFAULT_CONTENT_TEXT = "정말 맛있었어요! 강추합니다."
        const val DEFAULT_RATING = 5
        val DEFAULT_IMAGE_URLS = listOf(
            1, 2L
        )

        val DEFAULT_AUTHOR = Author(
            memberId = DEFAULT_AUTHOR_MEMBER_ID,
            nickname = DEFAULT_AUTHOR_NICKNAME,
            introduction = DEFAULT_AUTHOR_INTRODUCTION,
            imageId = 1L
        )

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
                it.toLong()
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
            images: PostImages = createImages(3),
        ): Post {
            return Post.create(
                authorMemberId = authorMemberId,
                authorNickname = authorNickname,
                authorIntroduction = DEFAULT_AUTHOR_INTRODUCTION,
                restaurant = restaurant,
                content = content,
                images = images,
                authorImageId = 1L
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
    }
}
