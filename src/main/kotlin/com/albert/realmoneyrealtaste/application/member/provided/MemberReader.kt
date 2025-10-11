package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email

/**
 * 회원 조회 기능을 제공하는 인터페이스
 */
interface MemberReader {

    /**
     * 주어진 회원 ID로 회원을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 객체
     * @throws MemberNotFoundException 해당 ID의 회원이 존재하지 않는 경우
     */
    fun readMemberById(memberId: Long): Member

    /**
     * 주어진 이메일로 회원을 조회합니다.
     *
     * @param email 조회할 회원의 이메일
     * @return 조회된 회원 객체
     * @throws MemberNotFoundException 해당 이메일의 회원이 존재하지 않는 경우
     */
    fun readMemberByEmail(email: Email): Member

    /**
     * 주어진 이메일로 회원을 조회합니다. 회원이 존재하지 않을 경우 null을 반환합니다.
     *
     * @param email 조회할 회원의 이메일
     * @return 조회된 회원 객체 또는 null
     */
    fun findMemberByEmailOrNull(email: Email): Member?
}
