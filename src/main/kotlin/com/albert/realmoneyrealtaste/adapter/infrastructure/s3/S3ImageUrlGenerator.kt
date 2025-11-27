package com.albert.realmoneyrealtaste.adapter.infrastructure.s3

import com.albert.realmoneyrealtaste.application.image.provided.ImageUrlGenerator
import org.springframework.stereotype.Component

@Component
class S3ImageUrlGenerator(
    private val s3Config: S3Config,
) : ImageUrlGenerator {

    override fun generateImageUrl(key: String): String {
        return "https://${s3Config.bucketName}.s3.${s3Config.region}.amazonaws.com/$key"
    }
}
