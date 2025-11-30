package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.member.dto.SuggestedMembersResult
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
    private val followReader: FollowReader,
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

    override fun readAllActiveMembersByIds(memberIds: List<Long>): List<Member> {
        val findAllByIdInAndStatus = memberRepository.findAllByIdInAndStatus(memberIds, MemberStatus.ACTIVE)
        return findAllByIdInAndStatus
    }

    override fun findSuggestedMembers(
        memberId: Long,
        limit: Long,
    ): List<Member> {
        return memberRepository.findSuggestedMembers(memberId, MemberStatus.ACTIVE, limit)
    }

    override fun findSuggestedMembersWithFollowStatus(
        memberId: Long,
        limit: Long,
    ): SuggestedMembersResult {
        val suggestedUsers = memberRepository.findSuggestedMembers(memberId, MemberStatus.ACTIVE, limit)
        val targetIds = suggestedUsers.map { it.requireId() }
        val followingIds = followReader.findFollowings(memberId, targetIds)

        return SuggestedMembersResult(suggestedUsers, followingIds)
    }
}
