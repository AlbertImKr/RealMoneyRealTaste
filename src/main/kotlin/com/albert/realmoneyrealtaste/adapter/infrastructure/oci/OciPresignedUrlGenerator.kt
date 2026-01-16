package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse
import com.albert.realmoneyrealtaste.application.image.required.PresignedUrlGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.Instant

@Profile("oci")
@Component
class OciPresignedUrlGenerator(
    private val s3Presigner: S3Presigner,
    private val ociConfig: OciObjectStorageConfig,
    @Value("\${image.upload.expiration-minutes:15}") private val uploadExpirationMinutes: Long,
    @Value("\${image.get.expiration-minutes:60}") private val getExpirationMinutes: Long,
) : PresignedUrlGenerator {

    override fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPutResponse {
        val expiration = Instant.now().plus(Duration.ofMinutes(uploadExpirationMinutes))

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(ociConfig.bucketName)
            .key(imageKey)
            .contentType(request.contentType)
            .metadata(
                mapOf(
                    "original-name" to request.fileName,
                    "file-size" to request.fileSize.toString(),
                    "width" to request.width.toString(),
                    "height" to request.height.toString()
                )
            )
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(uploadExpirationMinutes))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)

        return PresignedPutResponse(
            uploadUrl = presignedRequest.url().toString(),
            key = imageKey,
            expiresAt = expiration,
            metadata = mapOf(
                "original-name" to request.fileName,
                "content-type" to request.contentType,
                "file-size" to request.fileSize.toString(),
                "width" to request.width.toString(),
                "height" to request.height.toString()
            ),
        )
    }

    override fun generatePresignedGetUrl(imageKey: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(ociConfig.bucketName)
            .key(imageKey)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(getExpirationMinutes))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)

        return presignedRequest.url().toString()
    }
}
