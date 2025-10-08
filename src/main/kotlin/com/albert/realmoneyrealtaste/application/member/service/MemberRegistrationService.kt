package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.event.MemberRegisteredEvent
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateEmailException
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.PasswordHash
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberRegistrationService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : MemberRegister {

    override fun register(request: MemberRegisterRequest): Member {
        memberRepository.findByEmail(request.email)?.let {
            throw DuplicateEmailException()
        }

        val passwordHash = PasswordHash.of(request.password, passwordEncoder)

        val member = Member.register(request.email, request.nickname, passwordHash)
        val savedMember = memberRepository.save(member)

        eventPublisher.publishEvent(
            MemberRegisteredEvent(
                memberId = member.id!!,
                email = member.email,
                nickname = member.nickname,
            )
        )

        return savedMember
    }
}
