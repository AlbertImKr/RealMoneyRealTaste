package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class MemberVerifyService(
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
) : MemberVerify {

    override fun verify(
        email: Email,
        password: RawPassword,
    ): MemberPrincipal {
        val member = findMemberByEmailOrThrow(email)

        validatePassword(member, password)

        return MemberPrincipal.from(member)
    }

    /**
     * 이메일로 회원을 조회합니다.
     *
     * @param email 회원 이메일
     * @return 조회된 회원
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     */
    private fun findMemberByEmailOrThrow(email: Email): Member {
        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException()
    }

    /**
     * 회원의 비밀번호를 검증합니다.
     *
     * @param member 검증할 회원
     * @param password 입력된 비밀번호
     * @throws MemberNotFoundException 비밀번호가 일치하지 않는 경우
     */
    private fun validatePassword(member: Member, password: RawPassword) {
        if (!member.verifyPassword(password, passwordEncoder)) {
            throw MemberNotFoundException()
        }
    }
}
