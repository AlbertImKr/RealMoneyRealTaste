package com.albert.realmoneyrealtaste.application.member.listener

import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivationEmailSender
import com.albert.realmoneyrealtaste.application.member.provided.MemberPasswordResetEmailSender
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 이메일 발송 요청 이벤트를 처리하는 리스너
 */
@Component
class EmailEventListener(
    private val memberActivationEmailSender: MemberActivationEmailSender,
    private val passwordResetEmailSender: MemberPasswordResetEmailSender,
) {

    /**
     * 활성화 이메일 발송 요청 이벤트 처리
     *
     * @param event 활성화 이메일 발송 요청 이벤트
     */
    @Async
    @EventListener
    fun handleActivationEmailRequest(event: EmailSendRequestedEvent.ActivationEmail) {
        memberActivationEmailSender.sendActivationEmail(
            email = event.email,
            nickname = event.nickname,
            activationToken = event.activationToken,
        )
    }

    /**
     * 비밀번호 재설정 이메일 발송 요청 이벤트 처리
     *
     * @param event 비밀번호 재설정 이메일 발송 요청 이벤트
     */
    @Async
    @EventListener
    fun handlePasswordResetEmailRequest(event: EmailSendRequestedEvent.PasswordResetEmail) {
        passwordResetEmailSender.sendResetEmail(
            email = event.email,
            nickname = event.nickname,
            passwordResetToken = event.passwordResetToken,
        )
    }
}
