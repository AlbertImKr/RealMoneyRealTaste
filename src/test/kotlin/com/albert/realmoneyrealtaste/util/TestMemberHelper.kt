package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class TestMemberHelper(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun createActivatedMember(
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
        password: RawPassword = MemberFixture.DEFAULT_RAW_PASSWORD,
    ): Member {
        val passwordHash = PasswordHash.of(password, passwordEncoder)
        val member = Member.register(email, nickname, passwordHash)
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

    @Transactional
    fun createMember(
        email: String = MemberFixture.DEFAULT_EMAIL.address,
        nickname: String = MemberFixture.DEFAULT_NICKNAME.value,
        password: RawPassword = MemberFixture.DEFAULT_RAW_PASSWORD,
    ): Member {
        val passwordHash = PasswordHash.of(password, passwordEncoder)
        val member = Member.register(Email(email), Nickname(nickname), passwordHash)
        return memberRepository.save(member)
    }

    fun getDefaultMember(): Member {
        return memberRepository.findByEmail(MemberFixture.DEFAULT_EMAIL)
            ?: throw IllegalStateException("Default member not found")
    }
}
