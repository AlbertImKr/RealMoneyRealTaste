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
        validateEmailNotDuplicated(request)

        val passwordHash = PasswordHash.of(request.password, passwordEncoder)

        val member = Member.register(request.email, request.nickname, passwordHash)

        val savedMember = memberRepository.save(member)

        publishMemberRegisteredEvent(savedMember)

        return savedMember
    }

    /**
     * 이메일 중복 여부를 검증합니다.
     *
     * @param request 회원 등록 요청 데이터
     * @throws DuplicateEmailException 이미 사용 중인 이메일인 경우
     */
    private fun validateEmailNotDuplicated(request: MemberRegisterRequest) {
        memberRepository.findByEmail(request.email)?.let {
            throw DuplicateEmailException()
        }
    }

    /**
     * 회원 등록 이벤트를 발행합니다.
     *
     * @param member 등록된 회원
     */
    private fun publishMemberRegisteredEvent(member: Member) {
        eventPublisher.publishEvent(
            MemberRegisteredEvent(
                memberId = member.requireId(),
                email = member.email,
                nickname = member.nickname,
            )
        )
    }
}
