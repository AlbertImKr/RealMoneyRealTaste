package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.domain.member.Member

/**
 * 회원 조회 기능을 제공하는 인터페이스
 */
fun interface MemberReader {

    /**
     * 주어진 회원 ID로 회원을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 객체
     * @throws MemberNotFoundException 해당 ID의 회원이 존재하지 않는 경우
     */
    fun readMemberById(memberId: Long): Member
}
