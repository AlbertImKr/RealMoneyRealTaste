package com.albert.realmoneyrealtaste.domain.collection.command

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo.Companion.MAX_COVER_IMAGE_URL_LENGTH

data class CollectionCreateCommand(
    val ownerMemberId: Long,
    val name: String,
    val description: String,
    val coverImageUrl: String? = null,
    val ownerName: String,
    val privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
) {
    init {
        validate()
    }

    private fun validate() {
        require(ownerMemberId > 0) { "소유자 회원 ID는 양수여야 합니다." }
        require(ownerName.isNotEmpty()) { "소유자 닉네임은 비어있을 수 없습니다." }

        require(name.isNotBlank()) { CollectionInfo.ERROR_NAME_BLANK }
        require(name.length <= CollectionInfo.MAX_NAME_LENGTH) { CollectionInfo.ERROR_NAME_LENGTH_EXCEEDED }

        require(description.length <= CollectionInfo.MAX_DESCRIPTION_LENGTH) { CollectionInfo.ERROR_DESCRIPTION_LENGTH_EXCEEDED }

        coverImageUrl?.let {
            require(coverImageUrl.isNotBlank()) { CollectionInfo.ERROR_COVER_IMAGE_URL_BLANK }
            require(coverImageUrl.length <= MAX_COVER_IMAGE_URL_LENGTH) { CollectionInfo.ERROR_COVER_IMAGE_URL_LENGTH_EXCEEDED }
        }
    }
}
