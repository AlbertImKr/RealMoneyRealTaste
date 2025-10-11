package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import org.springframework.data.repository.Repository

/**
 * 비밀번호 재설정 토큰 관리를 위한 리포지토리 인터페이스
 */
interface PasswordResetTokenRepository : Repository<PasswordResetToken, Long> {

    /**
     * 비밀번호 재설정 토큰을 저장합니다.
     *
     * @param token 저장할 비밀번호 재설정 토큰
     * @return 저장된 비밀번호 재설정 토큰
     */
    fun save(token: PasswordResetToken): PasswordResetToken

    /**
     * 토큰 문자열을 사용하여 비밀번호 재설정 토큰을 조회합니다.
     *
     * @param token 조회할 토큰 문자열
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     */
    fun findByToken(token: String): PasswordResetToken?

    /**
     * 비밀번호 재설정 토큰을 삭제합니다.
     *
     * @param token 삭제할 비밀번호 재설정 토큰
     */
    fun delete(token: PasswordResetToken)

    /**
     * 회원 ID를 사용하여 비밀번호 재설정 토큰을 조회합니다.
     *
     * @param memberId 조회할 회원 ID
     * @return 조회된 비밀번호 재설정 토큰, 없으면 null
     */
    fun findByMemberId(memberId: Long): PasswordResetToken?
}
