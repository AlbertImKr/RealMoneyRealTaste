package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member

/**
 * 회원 계정 활성화 기능을 제공하는 인터페이스
 */
interface MemberActivate {

    /**
     * 회원 계정을 활성화합니다.
     *
     * @param token 활성화 토큰
     * @return 활성화된 회원 객체
     * @throws InvalidActivationTokenException 토큰이 유효하지 않은 경우
     * @throws ExpiredActivationTokenException 토큰이 만료된 경우
     * @throws AlreadyActivatedException 이미 활성화된 계정인 경우
     */
    fun activate(token: String): Member

    /**
     * 활성화 이메일을 재발송합니다.
     *
     * @param email 재발송할 이메일 주소
     * @throws MemberNotFoundException 해당 이메일로 등록된 회원이 없는 경우
     * @throws AlreadyActivatedException 이미 활성화된 계정인 경우
     */
    fun resendActivationEmail(email: Email)
}
