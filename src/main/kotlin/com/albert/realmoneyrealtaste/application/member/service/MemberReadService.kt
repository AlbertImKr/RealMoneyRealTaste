package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberReadService(
    private val memberRepository: MemberRepository,
) : MemberReader {

    override fun readMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()
    }
}
