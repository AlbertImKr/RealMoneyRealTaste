package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidMemberStatusException
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidPasswordException

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
     * @throws MemberNotFoundException 해당 ID의 회원이 존재하지 않는 경우
     * @throws InvalidMemberStatusException 멤버가 등록 완료 상태가 아닌 경우
     * @throws DuplicateProfileAddressException 프로필 주소가 이미 사용 중인 경우
     */
    fun updateInfo(memberId: Long, request: AccountUpdateRequest): Member

    /**
     * 주어진 회원 ID에 해당하는 회원의 비밀번호를 수정합니다.
     *
     * @param memberId 수정할 회원의 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새로운 비밀번호
     * @return 수정된 회원 객체
     * @throws MemberNotFoundException 해당 ID의 회원이 존재하지 않는 경우
     * @throws InvalidMemberStatusException 멤버가 등록 완료 상태가 아닌 경우
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않는 경우
     */
    fun updatePassword(memberId: Long, currentPassword: RawPassword, newPassword: RawPassword): Member

    /**
     * 주어진 회원 ID에 해당하는 회원을 비활성화합니다.
     *
     * @param memberId 비활성화할 회원의 ID
     * @return 비활성화된 회원 객체
     * @throws MemberNotFoundException 해당 ID의 회원이 존재하지 않는 경우
     * @throws InvalidMemberStatusException 멤버가 등록 완료 상태가 아닌 경우
     */
    fun deactivate(memberId: Long): Member
}
