package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.application.member.provided.MemberActivationEmailSender
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberEventListener(
    private val memberActivationEmailSender: MemberActivationEmailSender,
) {
    @Async
    @EventListener
    fun handleMemberRegistered(event: MemberRegisteredEvent) {
        memberActivationEmailSender.sendActivationEmail(
            memberId = event.memberId,
            email = event.email,
            nickname = event.nickname,
        )
    }

    @Async
    @EventListener
    fun handleResendActivationEmail(event: ResendActivationEmailEvent) {
        memberActivationEmailSender.sendActivationEmail(
            memberId = event.memberId,
            email = event.email,
            nickname = event.nickname,
        )
    }
}
