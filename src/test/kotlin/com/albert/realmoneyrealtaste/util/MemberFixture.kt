package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.config.TestPasswordEncoder
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import java.time.LocalDateTime
import java.util.UUID

class MemberFixture {

    companion object {
        const val DEFAULT_USERNAME = "default@gmail.com"
        const val OTHER_EMAIL_ADDRESS = "other@gmai.com"
        const val OTHER_NICKNAME_VALUE = "otherNick"
        val DEFAULT_EMAIL = Email(DEFAULT_USERNAME)
        val OTHER_EMAIL = Email(OTHER_EMAIL_ADDRESS)
        val DEFAULT_NICKNAME = Nickname("defaultNick")
        val OTHER_NICKNAME = Nickname(OTHER_NICKNAME_VALUE)
        val DEFAULT_RAW_PASSWORD = RawPassword("Default1!")
        val TEST_ENCODER = TestPasswordEncoder()
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
            return Member.register(email, nickname, password)
        }

        fun createMemberWithId(
            id: Long,
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            val member = Member.register(email, nickname, password)
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
