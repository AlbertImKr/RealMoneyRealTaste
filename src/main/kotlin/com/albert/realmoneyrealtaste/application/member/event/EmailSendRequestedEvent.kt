package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname

/**
 * 이메일 발송 요청 애플리케이션 이벤트
 */
sealed class EmailSendRequestedEvent {
    data class ActivationEmail(
        val email: Email,
        val nickname: Nickname,
        val activationToken: ActivationToken,
    ) : EmailSendRequestedEvent()

    data class PasswordResetEmail(
        val email: Email,
        val nickname: Nickname,
        val passwordResetToken: PasswordResetToken,
    ) : EmailSendRequestedEvent()
}
