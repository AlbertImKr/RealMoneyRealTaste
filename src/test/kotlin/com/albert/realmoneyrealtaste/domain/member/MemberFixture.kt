package com.albert.realmoneyrealtaste.domain.member

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberFixture {

    companion object {
        val DEFAULT_EMAIL = Email("default@gmail.com")
        val DEFAULT_NICKNAME = Nickname("defaultNick")
        val DEFAULT_PASSWORD = Password("123456")
        val DEFAULT_PASSWORD_ENCODER = BCryptPasswordEncoder()

        fun createMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: Password = DEFAULT_PASSWORD,
            status: MemberStatus = MemberStatus.PENDING,
        ): Member {
            val member = Member.register(email, nickname, password)
            return when (status) {
                MemberStatus.PENDING -> member
                MemberStatus.ACTIVE -> member.activate()
                MemberStatus.DEACTIVATED -> member.activate().deactivate()
            }
        }

        fun setId(entity: BaseEntity, id: Long) {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(entity, id)
        }
    }
}
