package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.ActivationToken

/**
 * 활성화 토큰 삭제자
 */
fun interface ActivationTokenDeleter {

    /**
     * 활성화 토큰을 삭제합니다.
     * @param token 활성화 토큰
     */
    fun delete(token: ActivationToken)
}
