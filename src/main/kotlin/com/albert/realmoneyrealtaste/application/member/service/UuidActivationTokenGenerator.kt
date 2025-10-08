package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Transactional
@Service
class UuidActivationTokenGenerator(
    private val activationTokenRepository: ActivationTokenRepository,
    @PersistenceContext private val entityManager: EntityManager,
) : ActivationTokenGenerator {

    override fun generate(memberId: Long, expirationHours: Long): ActivationToken {
        activationTokenRepository.findByMemberId(memberId)?.let {
            activationTokenRepository.delete(it)
            entityManager.flush()
        }

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
