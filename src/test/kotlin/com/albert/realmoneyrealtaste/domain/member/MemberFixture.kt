package com.albert.realmoneyrealtaste.domain.member

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
        val NEW_PASSWORD = PasswordHash.of(NEW_RAW_PASSWORD, TEST_ENCODER)

        fun createMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.register(email, nickname, password)
        }

        fun createAdminMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.registerAdmin(email, nickname, password)
        }

        fun createManagerMember(
            email: Email = DEFAULT_EMAIL,
            nickname: Nickname = DEFAULT_NICKNAME,
            password: PasswordHash = DEFAULT_PASSWORD,
        ): Member {
            return Member.registerManager(email, nickname, password)
        }

        fun setId(entity: BaseEntity, id: Long) {
            val field = BaseEntity::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(entity, id)
        }
    }
}
