package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import org.springframework.data.repository.Repository
import java.util.Optional

interface MemberRepository : Repository<Member, Long> {

    fun save(member: Member): Member

    fun findByEmail(email: Email): Optional<Member>
}
