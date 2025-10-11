package com.albert.realmoneyrealtaste.application.member.listener

import com.albert.realmoneyrealtaste.application.member.event.MemberRegisteredEvent
import com.albert.realmoneyrealtaste.application.member.event.PasswordResetRequestedEvent
import com.albert.realmoneyrealtaste.application.member.event.ResendActivationEmailEvent
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivationEmailSender
import com.albert.realmoneyrealtaste.application.member.provided.MemberPasswordResetEmailSender
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberEventListener(
    private val memberActivationEmailSender: MemberActivationEmailSender,
    private val passwordResetEmailSender: MemberPasswordResetEmailSender,
) {
    /**
     * 회원 가입 이벤트 처리
     * - 회원 가입 시 인증 이메일 발송
     *
     * @param event 회원 가입 이벤트
     */
    @Async
    @EventListener
    fun handleMemberRegistered(event: MemberRegisteredEvent) {
        memberActivationEmailSender.sendActivationEmail(
            email = event.email,
            nickname = event.nickname,
            activationToken = event.activationToken,
        )
    }

    /**
     * 인증 이메일 재전송 이벤트 처리
     * - 인증 이메일 재전송 시 인증 이메일 발송
     *
     * @param event 인증 이메일 재전송 이벤트
     */
    @Async
    @EventListener
    fun handleResendActivationEmail(event: ResendActivationEmailEvent) {
        memberActivationEmailSender.sendActivationEmail(
            email = event.email,
            nickname = event.nickname,
            activationToken = event.activationToken,
        )
    }

    /**
     * 비밀번호 재설정 요청 이벤트 처리
     * - 비밀번호 재설정 요청 시 비밀번호 재설정 이메일 발송
     *
     * @param passwordResetToken 비밀번호 재설정 요청 이벤트
     */
    @Async
    @EventListener
    fun handlePasswordResetRequested(passwordResetToken: PasswordResetRequestedEvent) {
        passwordResetEmailSender.sendResetEmail(
            email = passwordResetToken.email,
            nickname = passwordResetToken.nickname,
            passwordResetToken = passwordResetToken.token,
        )
    }
}
