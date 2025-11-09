package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordResetTokenNotFoundException
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken

/**
 * 비밀번호 재설정 토큰 조회기
 */
interface PasswordResetTokenReader {

    /**
     * 토큰으로 비밀번호 재설정 토큰을 조회합니다.
     * @param token 조회할 토큰
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     * @throws InvalidPasswordResetTokenException 토큰이 유효하지 않은 경우 발생
     */
    fun findByToken(token: String): PasswordResetToken

    /**
     * 회원 ID로 비밀번호 재설정 토큰을 조회합니다.
     * @param memberId 조회할 회원 ID
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     * @throws PasswordResetTokenNotFoundException 토큰을 찾을 수 없는 경우 발생
     */
    fun findByMemberId(memberId: Long): PasswordResetToken

    /**
     * 회원 ID로 비밀번호 재설정 토큰을 조회합니다. (없으면 null 반환)
     * @param memberId 조회할 회원 ID
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     */
    fun findByMemberIdOrNull(memberId: Long): PasswordResetToken?
}
