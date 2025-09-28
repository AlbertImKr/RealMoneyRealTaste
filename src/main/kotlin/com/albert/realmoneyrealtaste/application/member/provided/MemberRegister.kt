package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Member

fun interface MemberRegister {

    fun register(request: MemberRegisterRequest): Member
}
