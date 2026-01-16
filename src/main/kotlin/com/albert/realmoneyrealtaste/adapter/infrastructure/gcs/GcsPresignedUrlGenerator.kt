package com.albert.realmoneyrealtaste.adapter.infrastructure.gcs

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse
import com.albert.realmoneyrealtaste.application.image.required.PresignedUrlGenerator
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.SignUrlOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Profile("gcp")
@Component
class GcsPresignedUrlGenerator(
    private val storage: Storage,
    private val gcsConfig: GcsConfig,
    @Value("\${image.upload.expiration-minutes:15}") private val uploadExpirationMinutes: Long,
    @Value("\${image.get.expiration-minutes:60}") private val getExpirationMinutes: Long,
) : PresignedUrlGenerator {

    override fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPutResponse {
        val expiration = Instant.now().plusSeconds(uploadExpirationMinutes * 60)

        val blobId = BlobId.of(gcsConfig.bucketName, imageKey)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(request.contentType)
            .build()

        val extHeaders = mapOf(
            "x-goog-meta-content-type" to request.contentType,
            "x-goog-meta-original-name" to request.fileName,
            "x-goog-meta-file-size" to request.fileSize.toString(),
            "x-goog-meta-width" to request.width.toString(),
            "x-goog-meta-height" to request.height.toString()
        )

        val url = storage.signUrl(
            blobInfo,
            uploadExpirationMinutes,
            TimeUnit.MINUTES,
            SignUrlOption.httpMethod(HttpMethod.PUT),
            SignUrlOption.withV4Signature(),
            SignUrlOption.withExtHeaders(extHeaders),
        )

        return PresignedPutResponse(
            uploadUrl = url.toString(),
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
        val blobId = BlobId.of(gcsConfig.bucketName, imageKey)
        val blobInfo = BlobInfo.newBuilder(blobId).build()

        val url = storage.signUrl(
            blobInfo,
            getExpirationMinutes,
            TimeUnit.MINUTES,
            SignUrlOption.httpMethod(HttpMethod.GET),
            SignUrlOption.withV4Signature()
        )

        return url.toString()
    }
}
