package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.ProfileAddress
import org.springframework.data.repository.Repository

/**
 * 회원 관리를 위한 리포지토리 인터페이스
 */
interface MemberRepository : Repository<Member, Long> {

    fun save(member: Member): Member

    fun findByEmail(email: Email): Member?

    fun findById(id: Long): Member?

    fun existsByDetailProfileAddress(profileAddress: ProfileAddress): Boolean
}
