package com.albert.realmoneyrealtaste.domain.collection.command

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo.Companion.MAX_COVER_IMAGE_URL_LENGTH

data class CollectionCreateCommand(
    val ownerMemberId: Long,
    val name: String,
    val description: String,
    val coverImageUrl: String? = null,
    val privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
) {
    init {
        validateName(name)
        validateDescription(description)
        coverImageUrl?.let { validateImageUrl(it) }
    }

    private fun validateName(name: String) {
        require(name.isNotBlank()) { CollectionInfo.ERROR_NAME_BLANK }
        require(name.length <= CollectionInfo.MAX_NAME_LENGTH) { CollectionInfo.ERROR_NAME_LENGTH_EXCEEDED }
    }

    private fun validateDescription(description: String) {
        require(description.length <= CollectionInfo.MAX_DESCRIPTION_LENGTH) { CollectionInfo.ERROR_DESCRIPTION_LENGTH_EXCEEDED }
    }

    private fun validateImageUrl(coverImageUrl: String) {
        require(coverImageUrl.isNotBlank()) { CollectionInfo.ERROR_COVER_IMAGE_URL_BLANK }
        require(coverImageUrl.length <= MAX_COVER_IMAGE_URL_LENGTH) { CollectionInfo.ERROR_COVER_IMAGE_URL_LENGTH_EXCEEDED }
    }
}
