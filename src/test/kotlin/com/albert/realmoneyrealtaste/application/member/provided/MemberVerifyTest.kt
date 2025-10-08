package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberVerifyTest(
    val memberVerify: MemberVerify,
    val memberRegister: MemberRegister,
) : IntegrationTestBase() {

    @Test
    fun `verify - failure - returns false when member does not exist`() {
        val verify = memberVerify.verify(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD
        )

        assertFalse(verify)
    }

    @Test
    fun `verify - failure - returns false when password is incorrect`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        memberRegister.register(request)
        val wrongPassword = RawPassword("wrong${password.value}")

        val verify = memberVerify.verify(
            email = email,
            password = wrongPassword
        )

        assertFalse(verify)
    }

    @Test
    fun `verify - success - returns true when credentials are correct`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        memberRegister.register(request)

        val verify = memberVerify.verify(
            email = email,
            password = password
        )

        assertTrue(verify)
    }
}
