package com.albert.realmoneyrealtaste.application.member.dto

import com.albert.realmoneyrealtaste.domain.member.Introduction
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.ProfileAddress

data class AccountUpdateRequest(
    val nickname: Nickname?,
    val profileAddress: ProfileAddress?,
    val introduction: Introduction?,
)
