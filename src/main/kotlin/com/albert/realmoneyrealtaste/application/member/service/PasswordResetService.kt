package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.event.PasswordResetRequestedEvent
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenDeleter
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenReader
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetter
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.exceptions.ExpiredPasswordResetTokenException
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
    private val eventPublisher: ApplicationEventPublisher,
) : PasswordResetter {

    override fun sendPasswordResetEmail(email: Email) {
        val member = memberReader.findMemberByEmailOrNull(email) ?: return

        deleteExistingTokenIfPresent(member.requireId())

        val token = passwordRestTokenGenerator.generate(member.requireId())

        publishPasswordResetRequestedEvent(member, token)
    }

    override fun resetPassword(token: String, newPassword: RawPassword) {
        val resetToken = passwordRestTokenReader.findByToken(token)
        validateTokenNotExpired(resetToken)

        val member = memberReader.readMemberById(resetToken.memberId)

        member.changePassword(PasswordHash.of(newPassword, passwordEncoder))

        deleteToken(resetToken)
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
     * 비밀번호 재설정 요청 이벤트를 발행합니다.
     *
     * @param member 회원
     * @param token 비밀번호 재설정 토큰
     */
    private fun publishPasswordResetRequestedEvent(member: Member, token: PasswordResetToken) {
        eventPublisher.publishEvent(
            PasswordResetRequestedEvent(
                email = member.email,
                nickname = member.nickname,
                token = token,
            )
        )
    }

    /**
     * 토큰 만료 여부를 검증합니다.
     */
    private fun validateTokenNotExpired(token: PasswordResetToken) {
        if (token.isExpired()) {
            deleteToken(token)
            throw ExpiredPasswordResetTokenException()
        }
    }

    /**
     * 토큰을 삭제합니다.
     */
    private fun deleteToken(token: PasswordResetToken) {
        passwordRestTokenDeleter.delete(token)
    }
}
