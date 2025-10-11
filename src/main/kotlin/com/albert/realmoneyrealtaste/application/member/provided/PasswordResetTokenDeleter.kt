package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken

/**
 * 비밀번호 재설정 토큰 삭제기
 */
fun interface PasswordResetTokenDeleter {

    /**
     * 비밀번호 재설정 토큰을 삭제합니다.
     * @param passwordResetToken 삭제할 비밀번호 재설정 토큰
     */
    fun delete(passwordResetToken: PasswordResetToken)
}
