package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.TestcontainersConfiguration
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestcontainersConfiguration::class)
class MemberRegisterTest(
    val memberRegister: MemberRegister,
    val passwordEncoder: PasswordEncoder,
) {

    @Test
    fun `register member`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME,
        )

        val member = memberRegister.register(request)

        assertEquals(request.email, member.email)
        assertEquals(request.nickname, member.nickname)
        assertEquals(true, member.verifyPassword(password, passwordEncoder))
    }
}
