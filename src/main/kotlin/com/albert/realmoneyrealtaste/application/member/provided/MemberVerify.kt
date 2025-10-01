package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.RawPassword

fun interface MemberVerify {

    fun verify(email: Email, password: RawPassword): Boolean
}
