package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.domain.member.ActivationToken

/**
 * 활성화 토큰 조회자
 */
fun interface ActivationTokenReader {

    /**
     * 활성화 토큰을 조회합니다.
     * @param  token 토큰 문자열
     * @return 토큰 문자열
     * @throws InvalidActivationTokenException 토큰이 존재하지 않는 경우
     */
    fun findByToken(token: String): ActivationToken
}
