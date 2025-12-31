package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse
import com.albert.realmoneyrealtaste.application.image.required.PresignedUrlGenerator
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

@Profile("prod")
@Component
class OciPresignedUrlGenerator(
    private val objectStorageClient: ObjectStorageClient,
    private val ociConfig: OciObjectStorageConfig,
    @Value("\${image.upload.expiration-minutes:15}") private val uploadExpirationMinutes: Long,
    @Value("\${image.get.expiration-minutes:60}") private val getExpirationMinutes: Long,
) : PresignedUrlGenerator {

    override fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPutResponse {
        val expiration = Instant.now().plus(Duration.ofMinutes(uploadExpirationMinutes))

        val parDetails = CreatePreauthenticatedRequestDetails.builder()
            .name("upload-${UUID.randomUUID()}")
            .objectName(imageKey)
            .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
            .timeExpires(Date.from(expiration))
            .build()

        val parRequest = CreatePreauthenticatedRequestRequest.builder()
            .namespaceName(ociConfig.namespace)
            .bucketName(ociConfig.bucketName)
            .createPreauthenticatedRequestDetails(parDetails)
            .build()

        val response = objectStorageClient.createPreauthenticatedRequest(parRequest)
        val parUrl =
            "https://objectstorage.${ociConfig.region}.oraclecloud.com${response.preauthenticatedRequest.accessUri}"

        val metadata = mapOf(
            "original-name" to request.fileName,
            "content-type" to request.contentType,
            "file-size" to request.fileSize.toString(),
            "width" to request.width.toString(),
            "height" to request.height.toString()
        )

        return PresignedPutResponse(
            uploadUrl = parUrl,
            key = imageKey,
            expiresAt = expiration,
            metadata = metadata,
        )
    }

    override fun generatePresignedGetUrl(imageKey: String): String {
        val expiration = Instant.now().plus(Duration.ofMinutes(getExpirationMinutes))

        val parDetails = CreatePreauthenticatedRequestDetails.builder()
            .name("read-${UUID.randomUUID()}")
            .objectName(imageKey)
            .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
            .timeExpires(Date.from(expiration))
            .build()

        val parRequest = CreatePreauthenticatedRequestRequest.builder()
            .namespaceName(ociConfig.namespace)
            .bucketName(ociConfig.bucketName)
            .createPreauthenticatedRequestDetails(parDetails)
            .build()

        val response = objectStorageClient.createPreauthenticatedRequest(parRequest)
        return "https://objectstorage.${ociConfig.region}.oraclecloud.com${response.preauthenticatedRequest.accessUri}"
    }
}
