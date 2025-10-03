package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberVerifyTest(
    val memberVerify: MemberVerify,
    val memberRegister: MemberRegister,
) : IntegrationTestBase() {

    @Test
    fun `no such member`() {
        val verify = memberVerify.verify(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
        )

        assertEquals(false, verify)
    }

    @Test
    fun `wrong password`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME,
        )
        memberRegister.register(request)
        val wrongPassword = RawPassword("wrong${password.value}")

        val verify = memberVerify.verify(
            email = email,
            password = wrongPassword,
        )

        assertEquals(false, verify)
    }

    @Test
    fun `correct password`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME,
        )
        memberRegister.register(request)

        val verify = memberVerify.verify(
            email = email,
            password = password,
        )

        assertEquals(true, verify)
    }
}
