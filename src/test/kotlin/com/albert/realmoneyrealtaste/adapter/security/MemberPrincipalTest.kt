package com.albert.realmoneyrealtaste.adapter.security

import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.Role
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberPrincipalTest {

    @Test
    fun `getAuthorities - success - returns authorities with ROLE prefix`() {
        val roles = setOf(Role.USER, Role.ADMIN)
        val principal = MemberPrincipal(
            memberId = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "안녕하세요",
            roles = roles
        )

        val authorities = principal.getAuthorities()

        assertEquals(2, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN")))
    }

    @Test
    fun `getAuthorities - success - returns empty collection when roles is empty`() {
        val principal = MemberPrincipal(
            memberId = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            roles = emptySet()
        )

        val authorities = principal.getAuthorities()

        assertEquals(0, authorities.size)
    }

    @Test
    fun `getAuthorities - success - returns single authority for single role`() {
        val principal = MemberPrincipal(
            memberId = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            roles = setOf(Role.MANAGER)
        )

        val authorities = principal.getAuthorities()

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_MANAGER")))
    }

    @Test
    fun `from - success - creates MemberPrincipal from Member`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(member.id, principal.memberId)
        assertEquals(member.email, principal.email)
        assertEquals(member.nickname, principal.nickname)
        assertTrue(principal.active)
        assertEquals(1, principal.getAuthorities().size)
        assertTrue(principal.getAuthorities().contains(SimpleGrantedAuthority("ROLE_USER")))
    }

    @Test
    fun `from - success - creates principal when member is not active`() {
        val member = MemberFixture.createMemberWithId(42L)

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(false, principal.active)
    }

    @Test
    fun `from - success - creates principal with multiple roles`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()
        member.grantRole(Role.MANAGER)
        member.grantRole(Role.ADMIN)

        val principal = MemberPrincipal.from(member)

        val authorities = principal.getAuthorities()
        assertEquals(3, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_MANAGER")))
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN")))
    }

    @Test
    fun `from - success - creates principal with deactivated member`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()
        member.deactivate()

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(false, principal.active)
        assertEquals("아직 자기소개가 없어요!", principal.introduction)
    }

    @Test
    fun `from - success - sets introduction value when it exists`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()
        member.updateInfo(introduction = Introduction("안녕하세요"))

        val principal = MemberPrincipal.from(member)

        assertEquals("안녕하세요", principal.introduction)
    }

    @Test
    fun `from - success - sets empty string when introduction is null`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertEquals("아직 자기소개가 없어요!", principal.introduction)
    }

    @Test
    fun `from - success - sets empty string when introduction value is empty`() {
        val member = MemberFixture.createMemberWithId(42L)
        member.activate()
        member.updateInfo(introduction = Introduction(""))  // 빈 문자열

        val principal = MemberPrincipal.from(member)

        assertEquals("", principal.introduction)  // Elvis 우측이 실행되지 않음
    }

    @Test
    fun `constructor - success - creates MemberPrincipal with all properties`() {
        val email = Email("test@example.com")
        val nickname = Nickname("testUser")
        val roles = setOf(Role.USER, Role.MANAGER)

        val principal = MemberPrincipal(
            memberId = 100L,
            email = email,
            nickname = nickname,
            active = true,
            introduction = "자기소개",
            roles = roles
        )

        assertEquals(100L, principal.memberId)
        assertEquals(email, principal.email)
        assertEquals(nickname, principal.nickname)
        assertTrue(principal.active)
        assertEquals("자기소개", principal.introduction)
    }

    @Test
    fun `getAuthorities - success - maps all role types correctly`() {
        val allRoles = setOf(Role.USER, Role.MANAGER, Role.ADMIN)
        val principal = MemberPrincipal(
            memberId = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            roles = allRoles
        )

        val authorities = principal.getAuthorities()

        assertEquals(3, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_USER" })
        assertTrue(authorities.any { it.authority == "ROLE_MANAGER" })
        assertTrue(authorities.any { it.authority == "ROLE_ADMIN" })
    }
}
