package com.albert.realmoneyrealtaste.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayInputStream

@Component
class TestS3Helper {

    @Autowired
    private lateinit var s3Client: S3Client

    fun uploadTestFile(key: String, content: String = "test content") {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket("test-bucket")
            .key(key)
            .contentType("image/jpeg")
            .contentLength(content.length.toLong())
            .build()

        s3Client.putObject(
            putObjectRequest, RequestBody.fromInputStream(
                ByteArrayInputStream(content.toByteArray()),
                content.length.toLong()
            )
        )
    }
}
