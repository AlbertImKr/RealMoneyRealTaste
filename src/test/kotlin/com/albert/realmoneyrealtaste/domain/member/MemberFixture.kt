package com.albert.realmoneyrealtaste.domain.member

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberFixture {

    companion object {
        val DEFAULT_EMAIL = Email("default@gmail.com")
        val DEFAULT_NICKNAME = Nickname("defaultNick")
        const val DEFAULT_PASSWORD = "defaultPassword"
        val DEFAULT_PASSWORD_ENCODER = BCryptPasswordEncoder()

        fun createMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: String = DEFAULT_PASSWORD,
            passwordEncoder: BCryptPasswordEncoder = DEFAULT_PASSWORD_ENCODER,
            status: MemberStatus = MemberStatus.PENDING,
        ): Member {
            val member = Member.register(email, nickname, password, passwordEncoder)
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
