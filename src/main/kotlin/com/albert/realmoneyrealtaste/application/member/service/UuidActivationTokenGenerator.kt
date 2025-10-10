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
        deleteExistingTokenIfPresent(memberId)

        return createAndSaveToken(memberId, expirationHours)
    }

    /**
     * 기존 활성화 토큰을 삭제합니다.
     *
     * 회원에게 이미 발급된 토큰이 있는 경우 삭제하고 즉시 flush하여
     * 새 토큰 생성 시 제약 조건 위반을 방지합니다.
     *
     * @param memberId 회원 ID
     */
    private fun deleteExistingTokenIfPresent(memberId: Long) {
        activationTokenRepository.findByMemberId(memberId)?.let { existingToken ->
            activationTokenRepository.delete(existingToken)
            entityManager.flush() // 제약 조건 위반 방지를 위한 즉시 반영
        }
    }

    /**
     * 새로운 활성화 토큰을 생성하고 저장합니다.
     *
     * @param memberId 회원 ID
     * @param expirationHours 토큰 만료 시간(시간 단위)
     * @return 생성 및 저장된 활성화 토큰
     */
    private fun createAndSaveToken(
        memberId: Long,
        expirationHours: Long,
    ): ActivationToken {
        val createdAt = LocalDateTime.now()
        val token = ActivationToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = createdAt,
            expiresAt = createdAt.plusHours(expirationHours)
        )

        return activationTokenRepository.save(token)
    }
}
