package com.albert.realmoneyrealtaste.adapter.webapi.image

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestS3Helper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals

class ImageApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var testS3Helper: TestS3Helper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - success - returns presigned URL`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.uploadUrl").isString)
            .andExpect(jsonPath("$.key").isString)
            .andExpect(jsonPath("$.metadata").isMap)
            .andExpect(jsonPath("$.expiresAt").isString)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - invalid file name`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - invalid content type`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.txt",
            fileSize = 1024L,
            contentType = "text/plain",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - file size too large`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 10L * 1024L * 1024L, // 10MB
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - image dimensions too large`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 3000, // 3000px
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `confirmImageUpload - success - confirms upload`() {
        // Given
        val key = "images/2024/01/01/test-key.jpg"
        // S3에 미리 파일 업로드
        testS3Helper.uploadTestFile(key, "test image content")

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-confirm")
                .with(csrf())
                .param("key", key)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").isBoolean)
            .andExpect(jsonPath("$.imageId").isNumber)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getUploadStatus - success - returns upload status`() {
        // Given
        val key = "images/2024/01/01/test-key.jpg"
        // S3에 미리 파일 업로드
        testS3Helper.uploadTestFile(key, "test image content")

        // When & Then
        mockMvc.perform(
            get("/api/images/upload-status/$key")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").isBoolean)
            .andExpect(jsonPath("$.imageId").isNumber)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageUrl - success - returns image URL`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test-key.jpg"),
                uploadedBy = member.requireId(),
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        imageRepository.save(image)

        // S3에 실제 파일 업로드
        testS3Helper.uploadTestFile("test-key.jpg", "test image content")

        // When & Then
        mockMvc.perform(
            get("/api/images/{imageId}/url", image.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.url").isString)
            .andExpect(jsonPath("$.url").value(containsString("test-key.jpg")))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageUrl - failure - image not found`() {
        // Given
        val imageId = 999L

        // When & Then
        mockMvc.perform(
            get("/api/images/{imageId}/url", imageId)
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getMyImages - success - returns user images`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 이미지 생성
        repeat(2) { index ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("test-key-$index.jpg"),
                    uploadedBy = member.requireId(),
                    imageType = ImageType.PROFILE_IMAGE,
                )
            )
            imageRepository.save(image)

            // S3에 실제 파일 업로드
            testS3Helper.uploadTestFile("test-key-$index.jpg", "test image content $index")
        }

        // When & Then
        mockMvc.perform(
            get("/api/images/my-images")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteImage - success - deletes image`() {
        // Given
        val author = testMemberHelper.getDefaultMember()
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test-key.jpg"),
                uploadedBy = author.requireId(),
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        val save = imageRepository.save(image)

        // S3에 실제 파일 업로드
        testS3Helper.uploadTestFile("test-key.jpg", "test image content")

        // When & Then
        mockMvc.perform(
            delete("/api/images/{imageId}", image.requireId())
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("이미지가 성공적으로 삭제되었습니다"))

        // S3에서 파일 삭제 확인
        assertEquals(save.isDeleted, true)
    }

    @Test
    fun `requestImageUpload - failure - unauthorized`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - no CSRF token`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageUrl - failure - specific error message for not found`() {
        // Given
        val imageId = 999L

        // When & Then
        mockMvc.perform(
            get("/api/images/{imageId}/url", imageId)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(containsString("이미지가 존재하지 않습니다.")))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteImage - failure - cannot delete other user's image`() {
        // Given
        val author = testMemberHelper.createActivatedMember("other@example.com")
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test-key.jpg"),
                uploadedBy = author.requireId(),
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        imageRepository.save(image)

        // When & Then - 다른 사용자가 삭제 시도
        mockMvc.perform(
            delete("/api/images/{imageId}", image.requireId())
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(containsString("이미지 삭제에 실패했습니다.")))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - failure - zero image dimensions`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 0, // 0px
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - success - maximum allowed dimensions`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 2000, // 최대 허용 크기
            height = 2000,  // 최대 허용 크기
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `requestImageUpload - success - maximum allowed file size`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 5242880L, // 정확히 5MB
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then
        mockMvc.perform(
            post("/api/images/upload-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getUploadStatus - failure - file not found in S3`() {
        // Given
        val key = "images/2024/01/01/non-existent-file.jpg"

        // When & Then
        mockMvc.perform(
            get("/api/images/upload-status/$key")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
    }
}
