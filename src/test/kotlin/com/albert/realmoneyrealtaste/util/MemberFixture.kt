package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import java.time.LocalDateTime
import java.util.UUID

class MemberFixture {

    companion object {
        const val DEFAULT_USERNAME = "default@gmail.com"
        val DEFAULT_EMAIL = Email(DEFAULT_USERNAME)
        val DEFAULT_NICKNAME = Nickname("defaultNick")
        val DEFAULT_RAW_PASSWORD = RawPassword("Default1!")
        val TEST_ENCODER = object : PasswordEncoder {
            override fun encode(rawPassword: RawPassword): String {
                return "hashed-${rawPassword.value}"
            }

            override fun matches(rawPassword: RawPassword, passwordHash: String): Boolean {
                return passwordHash == encode(rawPassword)
            }
        }
        val DEFAULT_PASSWORD = PasswordHash.of(DEFAULT_RAW_PASSWORD, TEST_ENCODER)
        val NEW_RAW_PASSWORD = RawPassword("NewDefault1!")

        fun createActivationToken(memberId: Long): ActivationToken {
            val now = LocalDateTime.now()
            return ActivationToken(memberId, UUID.randomUUID().toString(), now, now.plusDays(1))
        }

        fun createMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.Companion.register(email, nickname, password)
        }

        fun createMemberWithId(
            id: Long,
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            val member = Member.Companion.register(email, nickname, password)
            setId(member, id)
            return member
        }

        fun createAdminMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.Companion.registerAdmin(email, nickname, password)
        }

        fun createAdminMemberWithId(
            id: Long,
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            val member = Member.Companion.registerAdmin(email, nickname, password)
            setId(member, id)
            return member
        }

        fun createManagerMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.Companion.registerManager(email, nickname, password)
        }

        fun createManagerMemberWithId(
            id: Long,
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            val member = Member.Companion.registerManager(email, nickname, password)
            setId(member, id)
            return member
        }

        fun setId(entity: BaseEntity, id: Long) {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(entity, id)
        }
    }
}
