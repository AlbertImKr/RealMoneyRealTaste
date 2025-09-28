package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.RawPassword

data class MemberRegisterRequest(
    val email: Email,
    val password: RawPassword,
    val nickname: Nickname,
)
