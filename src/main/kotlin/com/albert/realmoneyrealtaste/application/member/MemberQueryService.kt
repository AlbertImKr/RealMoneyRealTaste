package com.albert.realmoneyrealtaste.application.member

import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberQueryService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
) : MemberVerify {

    override fun verify(
        email: Email,
        password: RawPassword,
    ): Boolean {
        return memberRepository.findByEmail(email)
            .map { it.verifyPassword(password, passwordEncoder) }
            .orElse(false)
    }
}
