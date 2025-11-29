package com.albert.realmoneyrealtaste.adapter.infrastructure.s3

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse
import com.albert.realmoneyrealtaste.application.image.required.PresignedUrlGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.Instant

@Component
class S3PresignedUrlGenerator(
    private val s3Presigner: S3Presigner,
    private val s3Config: S3Config,
    @Value("\${image.upload.expiration-minutes:15}") private val s3PutUrlExpirationMinutes: Long,
    @Value("\${image.get.expiration-minutes:15}") private val s3GetUrlExpirationMinutes: Long,
) : PresignedUrlGenerator {

    private val logger = LoggerFactory.getLogger(S3PresignedUrlGenerator::class.java)

    override fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPutResponse {

        val expiration = Instant.now().plus(Duration.ofMinutes(s3PutUrlExpirationMinutes))

        val metadata = mapOf(
            "original-name" to request.fileName,
            "content-type" to request.contentType,
            "file-size" to request.fileSize.toString(),
            "width" to request.width.toString(),
            "height" to request.height.toString()
        )
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Config.bucketName)
            .key(imageKey)
            .contentType(request.contentType)
            .metadata(metadata)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(s3PutUrlExpirationMinutes))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)

        logger.info("Generated presigned PUT URL for key: $imageKey")

        return PresignedPutResponse(
            uploadUrl = presignedRequest.url().toString(),
            key = imageKey,
            expiresAt = expiration,
            metadata = metadata,
        )
    }

    /**
     * 이미지 다운로드를 위한 Presigned GET URL 생성
     */
    override fun generatePresignedGetUrl(imageKey: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Config.bucketName)
            .key(imageKey)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(s3GetUrlExpirationMinutes))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)

        logger.info("Generated presigned GET URL for key: $imageKey")

        return presignedRequest.url().toString()
    }
}
