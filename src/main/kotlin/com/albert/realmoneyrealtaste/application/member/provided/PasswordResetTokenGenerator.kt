package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken

/**
 * 비밀번호 재설정 토큰 생성기
 */
fun interface PasswordResetTokenGenerator {

    /**
     * 비밀번호 재설정 토큰을 생성합니다.
     * @param memberId 토큰을 생성할 회원의 ID
     * @return 생성된 비밀번호 재설정 토큰
     */
    fun generate(memberId: Long): PasswordResetToken
}
