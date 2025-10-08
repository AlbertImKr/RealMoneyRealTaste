package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.RawPassword

/**
 * 회원 인증(로그인) 기능을 제공하는 인터페이스
 */
fun interface MemberVerify {

    /**
     * 이메일과 비밀번호를 사용하여 회원을 인증합니다.
     *
     * @param email 인증에 사용할 이메일
     * @param password 인증에 사용할 비밀번호
     * @return 인증 성공 시 true, 실패 시 false
     */
    fun verify(email: Email, password: RawPassword): Boolean
}
