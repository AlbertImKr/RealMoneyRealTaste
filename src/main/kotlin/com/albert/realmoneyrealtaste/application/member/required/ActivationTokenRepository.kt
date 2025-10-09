package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import org.springframework.data.repository.Repository

/**
 * 회원 활성화 토큰 관리를 위한 리포지토리 인터페이스
 */
interface ActivationTokenRepository : Repository<ActivationToken, Long> {

    /**
     * 활성화 토큰을 저장합니다.
     *
     * @param token 저장할 활성화 토큰
     * @return 저장된 활성화 토큰
     */
    fun save(token: ActivationToken): ActivationToken

    /**
     * 토큰 문자열을 사용하여 활성화 토큰을 조회합니다.
     *
     * @param token 조회할 토큰 문자열
     * @return 조회된 활성화 토큰, 없으면 null
     */
    fun findByToken(token: String): ActivationToken?

    /**
     * 활성화 토큰을 삭제합니다.
     *
     * @param token 삭제할 활성화 토큰
     */
    fun delete(token: ActivationToken)

    /**
     * 회원 ID를 사용하여 활성화 토큰을 조회합니다.
     *
     * @param memberId 조회할 회원 ID
     * @return 조회된 활성화 토큰, 없으면 null
     */
    fun findByMemberId(memberId: Long): ActivationToken?
}
