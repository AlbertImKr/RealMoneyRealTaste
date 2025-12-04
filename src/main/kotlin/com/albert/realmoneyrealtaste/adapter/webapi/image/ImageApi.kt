package com.albert.realmoneyrealtaste.adapter.webapi.image

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.image.dto.ImageInfo
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse
import com.albert.realmoneyrealtaste.application.image.provided.ImageDeleter
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadRequester
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadTracker
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/images")
@Validated
class ImageApi(
    private val imageUploadRequester: ImageUploadRequester,
    private val imageUploadTracker: ImageUploadTracker,
    private val imageReader: ImageReader,
    private val imageDeleter: ImageDeleter,
) {

    @PostMapping("/upload-request")
    fun requestImageUpload(
        @RequestBody @Valid request: ImageUploadRequest,
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<PresignedPutResponse> {
        val response = imageUploadRequester.generatePresignedPostUrl(request, member.id)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/upload-confirm")
    fun confirmImageUpload(
        @RequestParam("key") key: String,
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<ImageUploadResult> {
        val result = imageUploadTracker.confirmUpload(key, member.id)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/upload-status/**")
    fun getUploadStatus(request: HttpServletRequest): ResponseEntity<ImageUploadResult> {
        val fullPath = request.requestURI
        val key = fullPath.substringAfter("/api/images/upload-status/")
        val result = imageReader.getUploadStatus(FileKey(key))
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{imageId}")
    fun getImageRedirect(
        @PathVariable imageId: Long,
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<Unit> {
        val url = imageReader.getImageUrl(imageId, member.id)
        return ResponseEntity.status(302).header("Location", url).build()
    }

    @GetMapping("/{imageId}/url")
    fun getImageUrl(
        @PathVariable imageId: Long,
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<Map<String, String>> {
        val url = imageReader.getImageUrl(imageId, member.id)

        return ResponseEntity.ok(mapOf("url" to url))
    }

    @GetMapping("/my-images")
    fun getMyImages(
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<List<ImageInfo>> {
        val images = imageReader.getImagesByMember(member.id)
        return ResponseEntity.ok(images)
    }

    @DeleteMapping("/{imageId}")
    fun deleteImage(
        @PathVariable imageId: Long,
        @AuthenticationPrincipal member: MemberPrincipal,
    ): ResponseEntity<Map<String, String>> {
        imageDeleter.deleteImage(imageId, member.id)
        return ResponseEntity.ok(mapOf("message" to "이미지가 성공적으로 삭제되었습니다"))
    }
}
