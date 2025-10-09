package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberVerifyService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
) : MemberVerify {

    override fun verify(
        email: Email,
        password: RawPassword,
    ): MemberPrincipal {
        val member = memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException()
        if (!member.verifyPassword(password, passwordEncoder)) {
            throw MemberNotFoundException()
        }
        return MemberPrincipal.from(member)
    }
}
