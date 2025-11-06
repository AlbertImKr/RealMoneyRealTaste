package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import org.springframework.data.repository.Repository

/**
 * 회원 관리를 위한 리포지토리 인터페이스
 */
interface MemberRepository : Repository<Member, Long> {

    /**
     * 회원 저장
     *
     * @param member 저장할 회원 엔티티
     * @return 저장된 회원 엔티티
     */
    fun save(member: Member): Member

    /**
     * 이메일로 회원 조회
     *
     * @param email 조회할 이메일
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findByEmail(email: Email): Member?

    /**
     * ID로 회원 조회
     *
     * @param id 조회할 회원 ID
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findById(id: Long): Member?

    /**
     * ID와 상태로 회원 조회 (특정 상태가 아닌 경우)
     *
     * @param id 조회할 회원 ID
     * @param status 제외할 회원 상태
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findByIdAndStatus(id: Long, status: MemberStatus): Member?

    /**
     * 프로필 주소로 회원 존재 여부 확인
     *
     * @param profileAddress 확인할 프로필 주소
     * @return 존재 여부
     */
    fun existsByDetailProfileAddress(profileAddress: ProfileAddress): Boolean

    /**
     * ID와 상태로 회원 존재 여부 확인 (특정 상태가 아닌 경우)
     *
     * @param id 확인할 회원 ID
     * @param status 제외할 회원 상태
     * @return 존재 여부
     */
    fun existsByIdAndStatus(id: Long, status: MemberStatus): Boolean
}
