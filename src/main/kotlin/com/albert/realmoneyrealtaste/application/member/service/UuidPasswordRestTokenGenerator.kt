package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Transactional
@Service
class UuidPasswordRestTokenGenerator(
    private val passwordRestTokenRepository: PasswordResetTokenRepository,
    @param:Value("\${app.member.password-reset-token.expiration-hours}") private val expirationHours: Long,
) : PasswordResetTokenGenerator {
    override fun generate(memberId: Long): PasswordResetToken {
        val now = LocalDateTime.now()
        val token = PasswordResetToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = now,
            expiresAt = now.plusHours(expirationHours),
        )

        return passwordRestTokenRepository.save(token)
    }
}
