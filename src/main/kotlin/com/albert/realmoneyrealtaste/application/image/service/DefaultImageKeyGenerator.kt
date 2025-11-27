package com.albert.realmoneyrealtaste.application.image.service

import com.albert.realmoneyrealtaste.application.image.provided.ImageKeyGenerator
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class DefaultImageKeyGenerator : ImageKeyGenerator {

    companion object {
        private const val DEFAULT_EXTENSION = "jpg"
        private const val DATE_PATTERN = "yyyy/MM/dd"
        private const val IMAGE_PREFIX = "images"
        private const val REPLACE_CHAR = "_"
        private const val DELIMITER = "."
        private val FILE_NAME_SANITIZER = Regex("[^a-zA-Z0-9._-]")
    }

    override fun generateSecureImageKey(fileName: String): String {
        val uuid = UUID.randomUUID().toString()
        val sanitizedName = fileName.replace(FILE_NAME_SANITIZER, REPLACE_CHAR)
        val extension = sanitizedName.substringAfterLast(DELIMITER, DEFAULT_EXTENSION)
        val datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        return "$IMAGE_PREFIX/$datePrefix/$uuid.$extension"
    }
}
