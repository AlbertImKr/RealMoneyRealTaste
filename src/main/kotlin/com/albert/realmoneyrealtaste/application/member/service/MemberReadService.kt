package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberReadService(
    private val memberRepository: MemberRepository,
) : MemberReader {

    companion object {
        const val ERROR_MEMBER_NOT_FOUND = "멤버를 찾을 수 없습니다."
    }

    override fun readMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            ?: throw MemberNotFoundException(ERROR_MEMBER_NOT_FOUND)
    }

    override fun readActiveMemberById(memberId: Long): Member {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
            ?: throw MemberNotFoundException(ERROR_MEMBER_NOT_FOUND)
    }

    override fun readMemberByEmail(email: Email): Member {
        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException(ERROR_MEMBER_NOT_FOUND)
    }

    override fun existsActiveMemberById(memberId: Long): Boolean {
        return memberRepository.existsByIdAndStatus(memberId, MemberStatus.ACTIVE)
    }

    override fun getNicknameById(memberId: Long): String {
        return readActiveMemberById(memberId).nickname.value
    }

    override fun existsByDetailProfileAddress(profileAddress: ProfileAddress): Boolean {
        return memberRepository.existsByDetailProfileAddress(profileAddress)
    }

    override fun existByEmail(email: Email): Boolean {
        return memberRepository.existsByEmail(email)
    }
}
