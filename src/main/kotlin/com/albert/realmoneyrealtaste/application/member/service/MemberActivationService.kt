package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent.ActivationEmail
import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenDeleter
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberActivationService(
    private val activationTokenReader: ActivationTokenReader,
    private val activationTokenDeleter: ActivationTokenDeleter,
    private val memberReader: MemberReader,
    private val domainEventPublisher: DomainEventPublisher,
    private val eventPublisher: ApplicationEventPublisher,
    private val activationTokenGenerator: ActivationTokenGenerator,
) : MemberActivate {

    companion object {
        const val ERROR_ACTIVATION_TOKEN_ALREADY_ACTIVATED = "이미 활성화된 회원입니다."
        const val ERROR_ACTIVATION_TOKEN_EXPIRED = "활성화 토큰이 만료되었습니다."
        const val ERROR_ACTIVATION_TOKEN_RESEND_EMAIL_FAILED = "인증 이메일 재전송에 실패했습니다."
        const val ERROR_MEMBER_ACTIVATE_FAILED = "회원 활성화에 실패했습니다."
    }

    override fun activate(token: String): Member {
        try {
            val activationToken = activationTokenReader.findByToken(token)

            validateTokenNotExpired(activationToken)

            val member = memberReader.readMemberById(activationToken.memberId)

            member.activate()

            activationTokenDeleter.delete(activationToken)

            // 도메인 이벤트 발행
            domainEventPublisher.publishFrom(member)

            return member
        } catch (e: IllegalArgumentException) {
            throw MemberActivateException(ERROR_MEMBER_ACTIVATE_FAILED)
        }
    }

    override fun resendActivationEmail(email: Email) {
        try {
            val member = memberReader.readMemberByEmail(email)

            require(!member.isActive()) { ERROR_ACTIVATION_TOKEN_ALREADY_ACTIVATED }

            val newToken = activationTokenGenerator.generate(member.requireId())

            // 애플리케이션 이벤트 직접 발행 (도메인 이벤트 없이)
            publishActivationEmailEvent(member, newToken)
        } catch (e: IllegalArgumentException) {
            throw MemberResendActivationEmailException(ERROR_ACTIVATION_TOKEN_RESEND_EMAIL_FAILED)
        }
    }

    /**
     * 활성화 이메일 발송 이벤트 발행
     *
     * @param member 회원
     * @param newToken 새로 생성된 활성화 토큰
     */
    private fun publishActivationEmailEvent(
        member: Member,
        newToken: ActivationToken,
    ) {
        eventPublisher.publishEvent(
            ActivationEmail(
                email = member.email,
                nickname = member.nickname,
                activationToken = newToken
            )
        )
    }

    /**
     * 활성화 토큰이 만료되었는지 검증하고 만료되었다면 토큰을 삭제
     *
     * @param activationToken 활성화 토큰
     * @throws IllegalArgumentException 활성화 토큰이 만료된 경우
     */
    private fun validateTokenNotExpired(activationToken: ActivationToken) {
        if (activationToken.isExpired()) {
            deleteToken(activationToken)
            throw IllegalArgumentException(ERROR_ACTIVATION_TOKEN_EXPIRED)
        }
    }

    /**
     * 활성화 토큰을 삭제
     *
     * @param activationToken 활성화 토큰
     */
    private fun deleteToken(activationToken: ActivationToken) {
        activationTokenDeleter.delete(activationToken)
    }
}
