package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberUpdateService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) : MemberUpdater {

    override fun updateInfo(
        memberId: Long,
        request: AccountUpdateRequest,
    ): Member {
        val member = memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        request.profileAddress?.let {
            if (memberRepository.existsByDetailProfileAddress(it)) throw DuplicateProfileAddressException()
        }

        member.updateInfo(
            nickname = request.nickname,
            profileAddress = request.profileAddress,
            introduction = request.introduction,
        )

        return member
    }

    override fun updatePassword(
        memberId: Long,
        currentPassword: RawPassword,
        newPassword: RawPassword,
    ): Member {
        val member = memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        member.changePassword(currentPassword, newPassword, passwordEncoder)

        return member
    }

    override fun deactivate(memberId: Long): Member {
        val member = memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        member.deactivate()

        return member
    }
}
