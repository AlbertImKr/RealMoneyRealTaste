package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent.PasswordResetEmail
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.exception.PassWordResetException
import com.albert.realmoneyrealtaste.application.member.exception.SendPasswordResetEmailException
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenDeleter
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenReader
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetter
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import jakarta.persistence.EntityManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PasswordResetService(
    private val memberReader: MemberReader,
    private val passwordRestTokenReader: PasswordResetTokenReader,
    private val passwordRestTokenGenerator: PasswordResetTokenGenerator,
    private val passwordRestTokenDeleter: PasswordResetTokenDeleter,
    private val passwordEncoder: PasswordEncoder,
    private val entityManager: EntityManager,
    private val domainEventPublisher: DomainEventPublisher,
    private val eventPublisher: ApplicationEventPublisher,
) : PasswordResetter {

    companion object {
        private const val ERROR_SENDING_PASSWORD_RESET_EMAIL = "비밀번호 재설정 이메일 전송 중 오류가 발생했습니다"
        private const val ERROR_RESETTING_PASSWORD = "비밀번호 재설정 중 오류가 발생했습니다"
        private const val ERROR_INVALID_TOKEN = "유효하지 않은 비밀번호 재설정 토큰입니다"
    }

    override fun sendPasswordResetEmail(email: String) {
        try {
            val member = memberReader.readMemberByEmail(Email(email))

            deleteExistingTokenIfPresent(member.requireId())

            val token = passwordRestTokenGenerator.generate(member.requireId())

            // 애플리케이션 이벤트 직접 발행 (도메인 이벤트 없이)
            publishPasswordResetEmailEvent(member, token)
        } catch (e: IllegalArgumentException) {
            throw SendPasswordResetEmailException(ERROR_SENDING_PASSWORD_RESET_EMAIL)
        }
    }

    override fun resetPassword(token: String, newPassword: RawPassword) {
        try {
            val resetToken = passwordRestTokenReader.findByToken(token)
            validateTokenNotExpired(resetToken)

            val member = memberReader.readMemberById(resetToken.memberId)

            member.changePassword(PasswordHash.of(newPassword, passwordEncoder))

            // 도메인 이벤트 발행
            domainEventPublisher.publishFrom(member)

            deleteToken(resetToken)
        } catch (e: IllegalArgumentException) {
            throw PassWordResetException(ERROR_RESETTING_PASSWORD)
        }
    }

    /**
     * 기존 비밀번호 재설정 토큰을 삭제합니다.
     *
     * @param memberId 회원 ID
     */
    private fun deleteExistingTokenIfPresent(memberId: Long) {
        passwordRestTokenReader.findByMemberIdOrNull(memberId)?.let {
            passwordRestTokenDeleter.delete(it)
            entityManager.flush()
        }
    }

    /**
     * 비밀번호 재설정 이메일 발송 이벤트를 발행합니다.
     *
     * @param member 회원
     * @param token 비밀번호 재설정 토큰
     */
    private fun publishPasswordResetEmailEvent(member: Member, token: PasswordResetToken) {
        eventPublisher.publishEvent(
            PasswordResetEmail(
                email = member.email,
                nickname = member.nickname,
                passwordResetToken = token
            )
        )
    }

    /**
     * 토큰 만료 여부를 검증합니다.
     */
    private fun validateTokenNotExpired(token: PasswordResetToken) {
        if (token.isExpired()) {
            deleteToken(token)
            throw ExpiredPasswordResetTokenException(ERROR_INVALID_TOKEN)
        }
    }

    /**
     * 토큰을 삭제합니다.
     */
    private fun deleteToken(token: PasswordResetToken) {
        passwordRestTokenDeleter.delete(token)
    }
}
