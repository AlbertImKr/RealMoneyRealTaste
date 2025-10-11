package com.albert.realmoneyrealtaste.application.member.dto

import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword

data class MemberRegisterRequest(
    val email: Email,
    val password: RawPassword,
    val nickname: Nickname,
)
