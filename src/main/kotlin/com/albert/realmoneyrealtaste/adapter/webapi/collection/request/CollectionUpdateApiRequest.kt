package com.albert.realmoneyrealtaste.adapter.webapi.collection.request

import com.albert.realmoneyrealtaste.application.collection.dto.CollectionUpdateRequest
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CollectionUpdateApiRequest(
    @field:NotBlank(message = "컬렉션 이름은 비어 있을 수 없습니다.")
    @field:Size(max = 100, message = "컬렉션 이름은 100자를 초과할 수 없습니다.")
    val name: String,

    @field:Size(max = 500, message = "컬렉션 설명은 500자를 초과할 수 없습니다.")
    val description: String = "",

    @field:Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
    @field:Pattern(
        regexp = "^$|^https?://.*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$",
        message = "올바른 이미지 URL 형식이 아닙니다. (jpg, jpeg, png, gif, webp 형식만 지원)"
    )
    val coverImageUrl: String? = null,
) {
    fun toServiceDto(collectionId: Long, ownerMemberId: Long) =
        CollectionUpdateRequest(
            collectionId = collectionId,
            ownerMemberId = ownerMemberId,
            newInfo = CollectionInfo(
                name = name.trim(),
                description = description.trim(),
                coverImageUrl = coverImageUrl?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
}
