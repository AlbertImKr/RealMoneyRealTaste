package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email

/**
 * 회원 계정 활성화 기능을 제공하는 인터페이스
 */
interface MemberActivate {

    /**
     * 회원 계정을 활성화합니다.
     *
     * @param token 활성화 토큰
     * @return 활성화된 회원 객체
     * @throws MemberActivateException 활성화에 실패한 경우 발생
     */
    fun activate(token: String): Member

    /**
     * 활성화 이메일을 재발송합니다.
     *
     * @param email 재발송할 이메일 주소
     * @throws MemberResendActivationEmailException 재발송에 실패한 경우 발생
     */
    fun resendActivationEmail(email: Email)
}
