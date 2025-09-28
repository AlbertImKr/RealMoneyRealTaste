package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Member
import org.springframework.data.repository.Repository

interface MemberRepository : Repository<Member, Long> {

    fun save(member: Member): Member
}
