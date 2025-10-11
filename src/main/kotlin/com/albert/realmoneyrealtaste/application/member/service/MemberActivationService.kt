package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.event.MemberRegisteredEvent
import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidMemberStatusException
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
        val activationToken = findActivationTokenOrThrow(token)
        validateTokenNotExpired(activationToken)
        val member = findMemberByTokenOrThrow(activationToken)

        member.activateOrThrow()
        deleteToken(activationToken)

        return member
    }

    override fun resendActivationEmail(email: Email) {
        val member = findMemberByEmailOrThrow(email)
        validateMemberNotActivated(member)
        publishMemberRegisteredEvent(member)
    }

    /**
     * 이메일로 회원 조회
     *
     * @param email 회원 이메일
     * @return 조회된 회원
     * @throws MemberNotFoundException 회원이 존재하지 않는 경우
     */
    private fun findMemberByEmailOrThrow(email: Email): Member {
        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException()
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
     * 회원 등록 이벤트를 발행
     *
     * @param member 등록된 회원
     */
    private fun publishMemberRegisteredEvent(member: Member) {
        eventPublisher.publishEvent(
            MemberRegisteredEvent(
                memberId = member.requireId(),
                email = member.email,
                nickname = member.nickname,
            )
        )
    }

    /**
     * 토큰으로 활성화 토큰을 조회
     *
     * @param token 활성화 토큰 문자열
     * @return 조회된 활성화 토큰
     * @throws InvalidActivationTokenException 토큰이 존재하지 않는 경우
     */
    private fun findActivationTokenOrThrow(token: String): ActivationToken {
        return activationTokenRepository.findByToken(token)
            ?: throw InvalidActivationTokenException()
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
        return memberRepository.findById(activationToken.memberId)
            ?: run {
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
        activationTokenRepository.delete(activationToken)
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
