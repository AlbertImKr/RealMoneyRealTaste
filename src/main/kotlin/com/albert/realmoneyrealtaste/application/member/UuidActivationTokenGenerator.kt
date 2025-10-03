package com.albert.realmoneyrealtaste.application.member

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Transactional
@Service
class UuidActivationTokenGenerator(
    private val activationTokenRepository: ActivationTokenRepository,
) : ActivationTokenGenerator {

    override fun generate(memberId: Long, expirationHours: Long): ActivationToken {
        val createdAt = LocalDateTime.now()
        return activationTokenRepository.save(
            ActivationToken(
                memberId = memberId,
                token = UUID.randomUUID().toString(),
                createdAt = createdAt,
                expiresAt = createdAt.plusHours(expirationHours)
            )
        )
    }
}
