package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname

/**
 * 회원 비밀번호 재설정 이메일 전송 기능을 제공하는 인터페이스
 */
fun interface MemberPasswordResetEmailSender {

    /**
     * 회원 비밀번호 재설정 이메일을 전송합니다.
     *
     * @param email 비밀번호 재설정 이메일을 전송할 대상 이메일 주소
     * @param nickname 비밀번호 재설정 이메일에 포함될 대상 회원의 닉네임
     * @param passwordResetToken 비밀번호 재설정 토큰
     */
    fun sendResetEmail(email: Email, nickname: Nickname, passwordResetToken: PasswordResetToken)
}
