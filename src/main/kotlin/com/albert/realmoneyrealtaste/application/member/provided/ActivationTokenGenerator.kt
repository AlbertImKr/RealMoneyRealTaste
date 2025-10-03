package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.ActivationToken

fun interface ActivationTokenGenerator {

    fun generate(memberId: Long, expirationHours: Long): ActivationToken
}
