package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.event.MemberRegisteredEvent
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateEmailException
import com.albert.realmoneyrealtaste.application.member.exception.MemberRegisterException
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberRegistrationService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val activationTokenGenerator: ActivationTokenGenerator,
    private val memberReader: MemberReader,
) : MemberRegister {

    companion object {
        const val ERROR_MEMBER_DUPLICATE_EMAIL = "이미 사용 중인 이메일입니다."
        const val ERROR_MEMBER_REGISTRATION_FAILED = "회원 등록 중 오류가 발생했습니다."
    }

    override fun register(request: MemberRegisterRequest): Member {
        try {
            validateEmailNotDuplicated(request)

            val passwordHash = PasswordHash.of(request.password, passwordEncoder)

            val member = Member.register(request.email, request.nickname, passwordHash)

            val savedMember = memberRepository.save(member)

            publishMemberRegisteredEvent(savedMember)

            return savedMember
        } catch (e: IllegalArgumentException) {
            throw MemberRegisterException(ERROR_MEMBER_REGISTRATION_FAILED, e)
        }
    }

    /**
     * 이메일 중복 여부를 검증합니다.
     *
     * @param request 회원 등록 요청 데이터
     * @throws DuplicateEmailException 이미 사용 중인 이메일인 경우
     */
    private fun validateEmailNotDuplicated(request: MemberRegisterRequest) {
        memberReader.existByEmail(request.email).let {
            if (it) {
                throw DuplicateEmailException("$ERROR_MEMBER_DUPLICATE_EMAIL: ${request.email.address}")
            }
        }
    }

    /**
     * 회원 등록 이벤트를 발행합니다.
     *
     * @param member 등록된 회원
     */
    private fun publishMemberRegisteredEvent(member: Member) {
        val activationToken = activationTokenGenerator.generate(member.requireId())
        eventPublisher.publishEvent(
            MemberRegisteredEvent(
                email = member.email,
                nickname = member.nickname,
                activationToken = activationToken,
            )
        )
    }
}
