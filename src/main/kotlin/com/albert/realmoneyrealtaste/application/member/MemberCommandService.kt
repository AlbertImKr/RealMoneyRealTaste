package com.albert.realmoneyrealtaste.application.member

import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.PasswordHash
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberCommandService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
) : MemberRegister {

    override fun register(request: MemberRegisterRequest): Member {
        val passwordHash = PasswordHash.of(request.password, passwordEncoder)

        val member = Member.register(request.email, request.nickname, passwordHash)

        return memberRepository.save(member)
    }
}
