package com.albert.realmoneyrealtaste.adapter.infrastructure.s3

import com.albert.realmoneyrealtaste.application.image.required.CloudStorage
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

@Component
class S3CloudStorage(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val s3Config: S3Config,
) : CloudStorage {

    override fun generatePresignedUrl(fileKey: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Config.bucketName)
            .key(fileKey)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)
        return presignedRequest.url().toString()
    }

    override fun delete(fileKey: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(s3Config.bucketName)
            .key(fileKey)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}
