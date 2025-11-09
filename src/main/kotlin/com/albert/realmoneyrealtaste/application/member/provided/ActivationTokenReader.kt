package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.ActivationTokenNotFoundException
import com.albert.realmoneyrealtaste.domain.member.ActivationToken

/**
 * 활성화 토큰 조회자
 */
fun interface ActivationTokenReader {

    /**
     * 활성화 토큰을 조회합니다.
     * @param  token 토큰 문자열
     * @return 토큰 문자열
     * @throws ActivationTokenNotFoundException 토큰을 찾을 수 없는 경우 발생
     */
    fun findByToken(token: String): ActivationToken
}
