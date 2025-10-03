package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname

data class MemberRegisteredEvent(
    val memberId: Long,
    val email: Email,
    val nickname: Nickname,
)
