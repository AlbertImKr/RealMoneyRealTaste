package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.domain.member.Introduction
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.ProfileAddress
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AccountUpdateForm(
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    val nickname: String?,

    @field:Size(min = 3, max = 15, message = "프로필 주소는 3자 이상 15자 이하로 입력해주세요.")
    val profileAddress: String?,

    @field:Size(max = 500, message = "소개글은 최대 500자까지 입력 가능합니다.")
    val introduction: String?,
) {
    fun toAccountUpdateRequest(): AccountUpdateRequest =
        AccountUpdateRequest(
            nickname = this.nickname?.let { Nickname(it) },
            profileAddress = this.profileAddress?.let { ProfileAddress(it) },
            introduction = this.introduction?.let { Introduction(it) }
        )
}
