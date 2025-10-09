package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.ActivationToken

/**
 * 회원 활성화 토큰 생성 기능을 제공하는 인터페이스
 */
fun interface ActivationTokenGenerator {

    /**
     * 회원 활성화 토큰을 생성합니다.
     *
     * @param memberId 토큰을 생성할 회원의 ID
     * @param expirationHours 토큰의 만료 시간(시간 단위)
     * @return 생성된 회원 활성화 토큰
     */
    fun generate(memberId: Long, expirationHours: Long): ActivationToken
}
