package com.albert.realmoneyrealtaste.application.member

import com.albert.realmoneyrealtaste.application.member.event.MemberRegisteredEvent
import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberActivationService(
    private val activationTokenRepository: ActivationTokenRepository,
    private val memberRepository: MemberRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : MemberActivate {

    override fun activate(token: String): Member {
        val activationToken = activationTokenRepository.findByToken(token)
            ?: throw InvalidActivationTokenException()

        if (activationToken.isExpired()) {
            activationTokenRepository.delete(activationToken)
            throw ExpiredActivationTokenException()
        }

        val member = memberRepository.findById(activationToken.memberId)
            ?: throw InvalidActivationTokenException().also {
                activationTokenRepository.delete(activationToken)
            }

        member.activateOrThrow()

        activationTokenRepository.delete(activationToken)

        return member
    }

    private fun Member.activateOrThrow() {
        try {
            this.activate()
        } catch (_: IllegalArgumentException) {
            throw AlreadyActivatedException()
        }
    }

    override fun resendActivationEmail(email: Email) {
        val member = memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException()

        member.status == MemberStatus.ACTIVE && throw AlreadyActivatedException()

        eventPublisher.publishEvent(
            MemberRegisteredEvent(
                memberId = member.id!!,
                email = member.email,
                nickname = member.nickname,
            )
        )
    }
}
