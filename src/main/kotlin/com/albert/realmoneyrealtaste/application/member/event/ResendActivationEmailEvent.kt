package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname

data class ResendActivationEmailEvent(
    val email: Email,
    val nickname: Nickname,
    val activationToken: ActivationToken,
)
