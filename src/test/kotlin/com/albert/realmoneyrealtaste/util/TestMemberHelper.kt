package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class TestMemberHelper(
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun createActivatedMember(
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
        password: PasswordHash = MemberFixture.DEFAULT_PASSWORD,
    ): Member {
        val member = MemberFixture.createMember(
            email = email,
            nickname = nickname,
            password = password,
        )
        member.activate()
        return memberRepository.save(member)
    }

    @Transactional
    fun createActivatedMember(
        email: String = MemberFixture.DEFAULT_EMAIL.address,
        nickname: String = MemberFixture.DEFAULT_NICKNAME.value,
    ) = createActivatedMember(
        email = Email(email),
        nickname = Nickname(nickname),
    )
}
