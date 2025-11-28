package com.albert.realmoneyrealtaste.adapter.webview.image

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ImageView(
    private val imageReader: ImageReader,
) {

    /**
     * 이미지 캐러셀 프래그먼트 조회
     */
    @GetMapping(ImageUrls.READ_CAROUSEL)
    fun getImageCarousel(
        @RequestParam imageIds: List<Long>,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        model: Model,
    ): String {
        val images = imageReader.readImagesByIds(imageIds)
        model.addAttribute("images", images.map { it.url })

        return ImageViews.IMAGE_CAROUSEL_FRAGMENT
    }
}
