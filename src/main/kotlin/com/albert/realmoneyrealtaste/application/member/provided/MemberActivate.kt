package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Member

/**
 * 회원 계정 활성화 기능을 제공하는 인터페이스
 */
fun interface MemberActivate {

    /**
     * 회원 계정을 활성화합니다.
     *
     * @param token 활성화 토큰
     * @return 활성화된 회원 객체
     * @throws com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException 토큰이 유효하지 않은 경우
     * @throws com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException 토큰이 만료된 경우
     * @throws com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException 이미 활성화된 계정인 경우
     */
    fun activate(token: String): Member
}
