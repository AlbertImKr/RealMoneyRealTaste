package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname

/**
 * 비밀번호 재설정이 요청된 이벤트
 *
 * @property email 회원 이메일
 * @property nickname 회원 닉네임
 * @property token 비밀번호 재설정 토큰
 */
data class PasswordResetRequestedEvent(
    val email: Email,
    val nickname: Nickname,
    val token: PasswordResetToken,
)
