package com.albert.realmoneyrealtaste.adapter.webview.image

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestS3Helper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isA
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import java.util.List
import kotlin.test.Test

class ImageViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var testS3Helper: TestS3Helper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageCarousel - success - returns carousel fragment with images`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val imageIds = mutableListOf<Long>()

        // 실제 이미지 데이터 생성
        repeat(3) { index ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("test-key-$index.jpg"),
                    uploadedBy = member.requireId(),
                    imageType = ImageType.POST_IMAGE,
                )
            )
            val savedImage = imageRepository.save(image)
            imageIds.add(savedImage.requireId())

            // S3에 실제 파일 업로드
            testS3Helper.uploadTestFile("test-key-$index.jpg", "test image content $index")
        }

        // When & Then
        mockMvc.perform(
            get("/images/carousel")
                .param("imageIds", *imageIds.map { it.toString() }.toTypedArray())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("image/fragments/image-carousel :: image-carousel"))
            .andExpect(model().attributeExists("images"))
            .andExpect(model().attributeExists("carouselId"))
            .andExpect(model().attribute("images", isA<Long>(List::class.java)))
            .andExpect(model().attribute("images", hasSize<Long>(3)))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageCarousel - failure - returns carousel fragment with empty images`() {
        // When & Then
        mockMvc.perform(
            get("/images/carousel")
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageCarousel - success - returns carousel fragment with single image`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("single-test-key.jpg"),
                uploadedBy = member.requireId(),
                imageType = ImageType.POST_IMAGE,
            )
        )
        val savedImage = imageRepository.save(image)

        // S3에 실제 파일 업로드
        testS3Helper.uploadTestFile("single-test-key.jpg", "single test image content")

        // When & Then
        mockMvc.perform(
            get("/images/carousel")
                .param("imageIds", savedImage.requireId().toString())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("image/fragments/image-carousel :: image-carousel"))
            .andExpect(model().attribute("images", isA<Long>(List::class.java)))
            .andExpect(model().attribute("images", hasSize<Long>(1)))
    }

    @Test
    fun `getImageCarousel - success - works without authentication`() {
        // Given
        val member = testMemberHelper.createActivatedMember(
            email = "noauth@example.com",
            nickname = "noauth"
        )
        val imageIds = mutableListOf<Long>()

        // 실제 이미지 데이터 생성
        repeat(2) { index ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("noauth-key-$index.jpg"),
                    uploadedBy = member.requireId(),
                    imageType = ImageType.POST_IMAGE,
                )
            )
            val savedImage = imageRepository.save(image)
            imageIds.add(savedImage.requireId())

            // S3에 실제 파일 업로드
            testS3Helper.uploadTestFile("noauth-key-$index.jpg", "noauth image content $index")
        }

        // When & Then
        mockMvc.perform(
            get("/images/carousel")
                .param("imageIds", *imageIds.map { it.toString() }.toTypedArray())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("image/fragments/image-carousel :: image-carousel"))
            .andExpect(model().attribute("images", isA<Long>(List::class.java)))
            .andExpect(model().attribute("images", hasSize<Long>(2)))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageEdit - success - returns edit fragment with images`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val imageIds = mutableListOf<Long>()

        // 실제 이미지 데이터 생성
        repeat(3) { index ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("edit-key-$index.jpg"),
                    uploadedBy = member.requireId(),
                    imageType = ImageType.POST_IMAGE,
                )
            )
            val savedImage = imageRepository.save(image)
            imageIds.add(savedImage.requireId())

            // S3에 실제 파일 업로드
            testS3Helper.uploadTestFile("edit-key-$index.jpg", "edit image content $index")
        }

        // When & Then
        mockMvc.perform(
            get("/images/edit")
                .param("imageIds", *imageIds.map { it.toString() }.toTypedArray())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("image/fragments/image-edit :: image-edit"))
            .andExpect(model().attributeExists("images"))
            .andExpect(model().attribute("images", isA<Long>(List::class.java)))
            .andExpect(model().attribute("images", hasSize<Long>(3)))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageEdit - failure - returns edit fragment with empty images`() {
        // When & Then
        mockMvc.perform(
            get("/images/edit")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageEdit - success - returns edit fragment with single image`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("edit-single-key.jpg"),
                uploadedBy = member.requireId(),
                imageType = ImageType.POST_IMAGE,
            )
        )
        val savedImage = imageRepository.save(image)

        // S3에 실제 파일 업로드
        testS3Helper.uploadTestFile("edit-single-key.jpg", "edit single image content")

        // When & Then
        mockMvc.perform(
            get("/images/edit")
                .param("imageIds", savedImage.requireId().toString())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("image/fragments/image-edit :: image-edit"))
            .andExpect(model().attribute("images", isA<Long>(List::class.java)))
            .andExpect(model().attribute("images", hasSize<Long>(1)))
    }

    @Test
    fun `getImageEdit - failure - works without authentication`() {
        // Given
        val member = testMemberHelper.createActivatedMember(
            email = "noauth-edit@example.com",
            nickname = "noauthedit"
        )
        val imageIds = mutableListOf<Long>()

        // 실제 이미지 데이터 생성
        repeat(2) { index ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("noauth-edit-key-$index.jpg"),
                    uploadedBy = member.requireId(),
                    imageType = ImageType.POST_IMAGE,
                )
            )
            val savedImage = imageRepository.save(image)
            imageIds.add(savedImage.requireId())

            // S3에 실제 파일 업로드
            testS3Helper.uploadTestFile("noauth-edit-key-$index.jpg", "noauth edit image content $index")
        }

        // When & Then
        mockMvc.perform(
            get("/images/edit")
                .param("imageIds", *imageIds.map { it.toString() }.toTypedArray())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `getImageCarousel - success - generates unique carouselId`() {
        // Given
        val member = testMemberHelper.getDefaultMember()
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("carousel-unique-key.jpg"),
                uploadedBy = member.requireId(),
                imageType = ImageType.POST_IMAGE,
            )
        )
        val savedImage = imageRepository.save(image)

        // S3에 실제 파일 업로드
        testS3Helper.uploadTestFile("carousel-unique-key.jpg", "carousel unique test image content")

        // When
        val result1 = mockMvc.perform(
            get("/images/carousel")
                .param("imageIds", savedImage.requireId().toString())
        ).andReturn()

        val result2 = mockMvc.perform(
            get("/images/carousel")
                .param("imageIds", savedImage.requireId().toString())
        ).andReturn()

        // Then
        val carouselId1 = result1.modelAndView?.model?.get("carouselId") as String
        val carouselId2 = result2.modelAndView?.model?.get("carouselId") as String

        assert(carouselId1.startsWith("carousel-"))
        assert(carouselId2.startsWith("carousel-"))
        assert(carouselId1 != carouselId2)
    }
}
