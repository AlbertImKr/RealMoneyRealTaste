package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.event.ResendActivationEmailEvent
import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenDeleter
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidMemberStatusException
import com.albert.realmoneyrealtaste.domain.member.value.Email
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberActivationService(
    private val actionTokenReader: ActivationTokenReader,
    private val actionTokenDeleter: ActivationTokenDeleter,
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
    private val activationTokenGenerator: ActivationTokenGenerator,
) : MemberActivate {

    override fun activate(token: String): Member {
        val activationToken = actionTokenReader.findByToken(token)

        validateTokenNotExpired(activationToken)

        val member = findMemberByTokenOrThrow(activationToken)

        member.activateOrThrow()
        actionTokenDeleter.delete(activationToken)

        return member
    }

    override fun resendActivationEmail(email: Email) {
        val member = memberReader.readMemberByEmail(email)

        validateMemberNotActivated(member)

        val newToken = activationTokenGenerator.generate(member.requireId())

        publishResendActivationEmailEvent(member, newToken)
    }

    /**
     * 인증 이메일 재전송 이벤트 발행
     *
     * @param member 회원
     * @param newToken 새로 생성된 활성화 토큰
     */
    private fun publishResendActivationEmailEvent(
        member: Member,
        newToken: ActivationToken,
    ) {
        eventPublisher.publishEvent(
            ResendActivationEmailEvent(
                email = member.email,
                nickname = member.nickname,
                activationToken = newToken
            )
        )
    }

    /**
     * 회원이 이미 활성화된 상태인지 검증
     *
     * @param member 회원
     * @throws AlreadyActivatedException 이미 활성화된 회원인 경우
     */
    private fun validateMemberNotActivated(member: Member) {
        if (member.status == MemberStatus.ACTIVE) {
            throw AlreadyActivatedException()
        }
    }

    /**
     * 활성화 토큰이 만료되었는지 검증하고 만료되었다면 토큰을 삭제
     *
     * @param activationToken 활성화 토큰
     * @throws ExpiredActivationTokenException 토큰이 만료된 경우
     */
    private fun validateTokenNotExpired(activationToken: ActivationToken) {
        if (activationToken.isExpired()) {
            deleteToken(activationToken)
            throw ExpiredActivationTokenException()
        }
    }

    /**
     * 활성화 토큰에서 회원을 조회하고 없으면 토큰을 삭제
     *
     * @param activationToken 활성화 토큰
     * @return 조회된 회원
     * @throws InvalidActivationTokenException 토큰에 해당하는 회원이 없는 경우
     */
    private fun findMemberByTokenOrThrow(activationToken: ActivationToken): Member {
        return try {
            memberReader.readMemberById(activationToken.memberId)
        } catch (_: MemberNotFoundException) {
            deleteToken(activationToken)
            throw InvalidActivationTokenException()
        }
    }

    /**
     * 활성화 토큰을 삭제
     *
     * @param activationToken 활성화 토큰
     */
    private fun deleteToken(activationToken: ActivationToken) {
        actionTokenDeleter.delete(activationToken)
    }

    /**
     * 회원을 활성화
     *
     * @throws AlreadyActivatedException 이미 활성화된 회원인 경우
     */
    private fun Member.activateOrThrow() {
        try {
            this.activate()
        } catch (_: InvalidMemberStatusException) {
            throw AlreadyActivatedException()
        }
    }
}
