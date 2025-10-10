package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.ProfileAddress
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
        val member = findMemberByIdOrThrow(memberId)

        validateProfileAddressNotDuplicated(request.profileAddress)

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
        val member = findMemberByIdOrThrow(memberId)

        member.changePassword(currentPassword, newPassword, passwordEncoder)

        return member
    }

    override fun deactivate(memberId: Long): Member {
        val member = findMemberByIdOrThrow(memberId)

        member.deactivate()

        return member
    }

    /**
     * 회원 ID로 회원을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 조회된 회원
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     */
    private fun findMemberByIdOrThrow(memberId: Long): Member {
        return memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()
    }

    /**
     * 프로필 주소 중복 여부를 검증합니다.
     *
     * @param profileAddress 검증할 프로필 주소
     * @throws DuplicateProfileAddressException 프로필 주소가 이미 사용 중인 경우
     */
    private fun validateProfileAddressNotDuplicated(profileAddress: ProfileAddress?) {
        profileAddress?.let {
            if (memberRepository.existsByDetailProfileAddress(it)) {
                throw DuplicateProfileAddressException()
            }
        }
    }
}

