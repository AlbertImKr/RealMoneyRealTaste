package com.albert.realmoneyrealtaste.adapter.webview.post

import kotlin.test.Test

class PostCreateFormTest {

    @Test
    fun `constructor - success - all fields provided`() {
        val form = PostCreateForm(
            restaurantName = "Test Restaurant",
            restaurantAddress = "123 Test St",
            restaurantLatitude = 37.7749,
            restaurantLongitude = -122.4194,
            contentText = "This is a test post content.",
            contentRating = 5,
            imagesUrls = listOf("http://example.com/image1.jpg", "http://example.com/image2.jpg"),
        )
        assert(form.restaurantName == "Test Restaurant")
        assert(form.restaurantAddress == "123 Test St")
        assert(form.restaurantLatitude == 37.7749)
        assert(form.restaurantLongitude == -122.4194)
        assert(form.contentText == "This is a test post content.")
        assert(form.contentRating == 5)
        assert(form.imagesUrls.size == 2)
    }
}
