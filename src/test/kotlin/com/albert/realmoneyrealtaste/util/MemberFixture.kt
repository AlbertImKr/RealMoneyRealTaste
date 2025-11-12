package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.config.TestPasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import java.time.LocalDateTime
import java.util.UUID

class MemberFixture {

    companion object {
        const val DEFAULT_USERNAME = "default@gmail.com"
        const val OTHER_EMAIL_ADDRESS = "other@gmai.com"
        const val OTHER_NICKNAME_VALUE = "otherNick"
        const val DEFAULT_PASSWORD_PLAIN = "Default1!"
        val TEST_ENCODER = TestPasswordEncoder()

        val DEFAULT_EMAIL = Email(DEFAULT_USERNAME)
        val OTHER_EMAIL = Email(OTHER_EMAIL_ADDRESS)
        val DEFAULT_NICKNAME = Nickname("defaultNick")
        val OTHER_NICKNAME = Nickname(OTHER_NICKNAME_VALUE)
        val DEFAULT_RAW_PASSWORD = RawPassword(DEFAULT_PASSWORD_PLAIN)
        val NEW_RAW_PASSWORD = RawPassword("NewDefault1!")

        fun createActivationToken(memberId: Long): ActivationToken {
            val now = LocalDateTime.now()
            return ActivationToken(memberId, UUID.randomUUID().toString(), now, now.plusDays(1))
        }
    }
}
