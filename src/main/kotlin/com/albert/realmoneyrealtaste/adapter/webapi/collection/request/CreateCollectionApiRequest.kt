package com.albert.realmoneyrealtaste.adapter.webapi.collection.request

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateCollectionApiRequest(
    @field:NotBlank(message = "컬렉션 이름은 비어 있을 수 없습니다.")
    @field:Size(max = 100, message = "컬렉션 이름은 100자를 초과할 수 없습니다.")
    val name: String,

    @field:Size(max = 500, message = "컬렉션 설명은 500자를 초과할 수 없습니다.")
    val description: String = "",

    @field:Pattern(
        regexp = "^(PUBLIC|PRIVATE)$",
        message = "공개 설정은 PUBLIC 또는 PRIVATE만 가능합니다."
    )
    val visibility: String = "PRIVATE",

    @field:Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
    @field:Pattern(
        regexp = "^$|^https?://.*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$",
        message = "올바른 이미지 URL 형식이 아닙니다. (jpg, jpeg, png, gif, webp 형식만 지원)"
    )
    val coverImageUrl: String? = null,
) {
    fun toCommand(ownerMemberId: Long): CollectionCreateCommand {
        return CollectionCreateCommand(
            ownerMemberId = ownerMemberId,
            name = name.trim(),
            description = description.trim(),
            privacy = when (visibility.uppercase()) {
                "PUBLIC" -> CollectionPrivacy.PUBLIC
                else -> CollectionPrivacy.PRIVATE
            },
            coverImageUrl = coverImageUrl?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
}
