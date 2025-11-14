package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress

/**
 * 회원 조회 기능을 제공하는 인터페이스
 */
interface MemberReader {

    /**
     * 주어진 회원 ID로 회원을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 객체
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우 발생
     */
    fun readMemberById(memberId: Long): Member

    /**
     * 주어진 회원 ID로 활성화된 회원을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 활성화된 회원 객체
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우 발생
     */
    fun readActiveMemberById(memberId: Long): Member

    /**
     * 주어진 이메일로 회원을 조회합니다.
     *
     * @param email 조회할 회원의 이메일
     * @return 조회된 회원 객체
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우 발생
     */
    fun readMemberByEmail(email: Email): Member

    /**
     * 주어진 회원 ID로 활성화된 회원의 존재 여부를 확인합니다.
     *
     * @param memberId 확인할 회원의 ID
     * @return 활성화된 회원이 존재하면 true, 그렇지 않으면 false
     */
    fun existsActiveMemberById(memberId: Long): Boolean

    /**
     * 주어진 회원 ID로 회원의 닉네임을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원의 닉네임
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우 발생
     */
    fun getNicknameById(memberId: Long): String

    /**
     * 주어진 프로필 주소로 회원의 존재 여부를 확인합니다.
     *
     * @param profileAddress 확인할 회원의 프로필 주소
     * @return 해당 프로필 주소를 가진 회원이 존재하면 true, 그렇지 않으면 false
     */
    fun existsByDetailProfileAddress(profileAddress: ProfileAddress): Boolean

    /**
     * 주어진 이메일로 회원의 존재 여부를 확인합니다.
     *
     * @param email 확인할 회원의 이메일
     * @return 해당 이메일을 가진 회원이 존재하면 true, 그렇지 않으면 false
     */
    fun existByEmail(email: Email): Boolean

    /**
     * 주어진 회원 ID 목록으로 활성화된 모든 회원을 조회합니다.
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 조회된 활성화된 회원 객체 목록
     */
    fun readAllActiveMembersByIds(memberIds: List<Long>): List<Member>
}
