package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword

/**
 * 비밀번호 재설정 서비스 제공 인터페이스
 */
interface PasswordResetter {

    /**
     * 비밀번호 재설정 이메일 전송
     * @param email 대상 이메일
     */
    fun sendPasswordResetEmail(email: Email)

    /**
     * 비밀번호 재설정
     * @param token 재설정 토큰
     * @param newPassword 새로운 비밀번호
     */
    fun resetPassword(token: String, newPassword: RawPassword)
}
