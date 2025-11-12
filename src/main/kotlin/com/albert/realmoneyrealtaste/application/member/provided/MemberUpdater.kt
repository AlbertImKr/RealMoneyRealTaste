package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberDeactivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberUpdateException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordChangeException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword

/**
 * 회원 정보 수정 기능을 제공하는 인터페이스
 */
interface MemberUpdater {

    /**
     * 주어진 회원 ID에 해당하는 회원의 정보를 수정합니다.
     *
     * @param memberId 수정할 회원의 ID
     * @param request 수정할 회원 정보가 담긴 요청 객체
     * @return 수정된 회원 객체
     * @throws MemberUpdateException 회원 정보 수정에 실패한 경우 발생
     */
    fun updateInfo(memberId: Long, request: AccountUpdateRequest): Member

    /**
     * 주어진 회원 ID에 해당하는 회원의 비밀번호를 수정합니다.
     *
     * @param memberId 수정할 회원의 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새로운 비밀번호
     * @return 수정된 회원 객체
     * @throws PasswordChangeException 비밀번호 변경에 실패한 경우 발생
     */
    fun updatePassword(memberId: Long, currentPassword: RawPassword, newPassword: RawPassword): Member

    /**
     * 주어진 회원 ID에 해당하는 회원을 비활성화합니다.
     *
     * @param memberId 비활성화할 회원의 ID
     * @return 비활성화된 회원 객체
     * @throws MemberDeactivateException 회원 탈퇴에 실패한 경우 발생
     */
    fun deactivate(memberId: Long): Member
}
