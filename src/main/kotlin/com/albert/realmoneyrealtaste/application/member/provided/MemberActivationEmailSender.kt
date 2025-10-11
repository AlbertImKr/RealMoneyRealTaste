package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname

/**
 * 회원 활성화 이메일 전송 기능을 제공하는 인터페이스
 */
fun interface MemberActivationEmailSender {

    /**
     * 회원 활성화 이메일을 전송합니다.
     *
     * @param email 활성화 이메일을 전송할 대상 이메일 주소
     * @param nickname 활성화 이메일에 포함될 대상 회원의 닉네임
     * @param activationToken 회원 활성화 토큰
     */
    fun sendActivationEmail(email: Email, nickname: Nickname, activationToken: ActivationToken)
}
