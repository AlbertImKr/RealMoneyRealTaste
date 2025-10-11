package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken

/**
 * 비밀번호 재설정 토큰 조회기
 */
interface PasswordResetTokenReader {

    /**
     * 토큰으로 비밀번호 재설정 토큰을 조회합니다.
     * @param token 조회할 토큰
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     * @throws InvalidPasswordResetTokenException 유효하지 않은 토큰인 경우
     */
    fun findByToken(token: String): PasswordResetToken

    /**
     * 회원 ID로 비밀번호 재설정 토큰을 조회합니다.
     * @param memberId 조회할 회원 ID
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     * @throws InvalidPasswordResetTokenException 해당 회원의 토큰이 존재하지 않는 경우
     */
    fun findByMemberId(memberId: Long): PasswordResetToken
}
