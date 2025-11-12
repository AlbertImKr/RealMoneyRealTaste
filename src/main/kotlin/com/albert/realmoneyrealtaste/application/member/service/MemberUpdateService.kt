package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberDeactivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberUpdateException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordChangeException
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberUpdateService(
    private val memberReader: MemberReader,
    private val passwordEncoder: PasswordEncoder,
) : MemberUpdater {

    companion object {
        private const val ERROR_MEMBER_INFO_UPDATE = "회원 정보 업데이트 중 오류가 발생했습니다"
        private const val ERROR_PASSWORD_UPDATE = "비밀번호 변경 중 오류가 발생했습니다"
        private const val ERROR_MEMBER_DEACTIVATE = "회원 탈퇴 중 오류가 발생했습니다"
    }

    override fun updateInfo(
        memberId: Long,
        request: AccountUpdateRequest,
    ): Member {
        try {
            val member = memberReader.readMemberById(memberId)

            validateProfileAddressNotDuplicated(request.profileAddress)

            member.updateInfo(
                nickname = request.nickname,
                profileAddress = request.profileAddress,
                introduction = request.introduction,
            )

            return member
        } catch (e: IllegalArgumentException) {
            throw MemberUpdateException(ERROR_MEMBER_INFO_UPDATE, e)
        }
    }

    override fun updatePassword(
        memberId: Long,
        currentPassword: RawPassword,
        newPassword: RawPassword,
    ): Member {
        try {
            val member = memberReader.readMemberById(memberId)

            member.changePassword(currentPassword, newPassword, passwordEncoder)

            return member
        } catch (e: IllegalArgumentException) {
            throw PasswordChangeException(ERROR_PASSWORD_UPDATE, e)
        }
    }

    override fun deactivate(memberId: Long): Member {
        try {

            val member = memberReader.readMemberById(memberId)

            member.deactivate()

            return member
        } catch (e: IllegalArgumentException) {
            throw MemberDeactivateException(ERROR_MEMBER_DEACTIVATE, e)
        }
    }

    /**
     * 프로필 주소 중복 여부를 검증합니다.
     *
     * @param profileAddress 검증할 프로필 주소
     */
    private fun validateProfileAddressNotDuplicated(profileAddress: ProfileAddress?) {
        profileAddress?.let {
            if (memberReader.existsByDetailProfileAddress(it)) {
                throw DuplicateProfileAddressException()
            }
        }
    }
}

