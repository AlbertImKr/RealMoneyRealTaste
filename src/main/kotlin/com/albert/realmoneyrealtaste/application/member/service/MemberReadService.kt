package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.exceptions.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.value.Email
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

    override fun readActiveMemberById(memberId: Long): Member {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
            ?: throw MemberNotFoundException()
    }

    override fun readMemberByEmail(email: Email): Member {
        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException()
    }

    override fun findMemberByEmailOrNull(email: Email): Member? {
        return memberRepository.findByEmail(email)
    }

    override fun existsActiveMemberById(memberId: Long): Boolean {
        return memberRepository.existsByIdAndStatus(memberId, MemberStatus.ACTIVE)
    }

    override fun getNicknameById(memberId: Long): String {
        return readActiveMemberById(memberId).nickname.value
    }
}
