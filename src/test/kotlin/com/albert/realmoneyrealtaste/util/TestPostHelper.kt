package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import com.albert.realmoneyrealtaste.util.PostFixture.Companion.DEFAULT_AUTHOR_NICKNAME
import com.albert.realmoneyrealtaste.util.PostFixture.Companion.DEFAULT_CONTENT
import com.albert.realmoneyrealtaste.util.PostFixture.Companion.DEFAULT_RESTAURANT
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class TestPostHelper(
    private val postRepository: PostRepository,
) {
    @Transactional
    fun createPost(
        authorMemberId: Long,
        authorNickname: String = DEFAULT_AUTHOR_NICKNAME,
        restaurant: Restaurant = DEFAULT_RESTAURANT,
        content: PostContent = DEFAULT_CONTENT,
        images: PostImages = PostFixture.createImages(3),
    ) = PostFixture.createPost(
        authorMemberId = authorMemberId,
        authorNickname = authorNickname,
        restaurant = restaurant,
        content = content,
        images = images,
    ).let { postRepository.save(it) }
}
