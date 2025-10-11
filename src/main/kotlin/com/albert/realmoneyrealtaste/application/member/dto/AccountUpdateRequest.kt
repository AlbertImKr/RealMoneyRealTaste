package com.albert.realmoneyrealtaste.application.member.dto

import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress

data class AccountUpdateRequest(
    val nickname: Nickname?,
    val profileAddress: ProfileAddress?,
    val introduction: Introduction?,
)
