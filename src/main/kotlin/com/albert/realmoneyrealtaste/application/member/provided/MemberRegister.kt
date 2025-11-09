package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberRegisterException
import com.albert.realmoneyrealtaste.domain.member.Member

/**
 * 회원 등록 기능을 제공하는 인터페이스
 */
fun interface MemberRegister {

    /**
     * 새로운 회원을 등록합니다. MemberRegisterEvent가 발행됩니다.
     *
     * @param request 회원 등록 요청 데이터
     * @return 등록된 회원 객체
     * @throws MemberRegisterException 회원 등록에 실패한 경우 발생
     */
    fun register(request: MemberRegisterRequest): Member
}
