package com.albert.realmoneyrealtaste.adapter.security

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.domain.member.value.Role
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberPrincipalTest {

    @Test
    fun `getAuthorities - success - returns authorities with ROLE prefix`() {
        val roles = setOf(Role.USER, Role.ADMIN)
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "안녕하세요",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = roles,
        )

        val authorities = principal.getAuthorities()

        assertEquals(2, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN")))
    }

    @Test
    fun `getAuthorities - success - returns empty collection when roles is empty`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = emptySet()
        )

        val authorities = principal.getAuthorities()

        assertEquals(0, authorities.size)
    }

    @Test
    fun `getAuthorities - success - returns single authority for single role`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = setOf(Role.MANAGER)
        )

        val authorities = principal.getAuthorities()

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_MANAGER")))
    }

    @Test
    fun `from - success - creates MemberPrincipal from Member`() {
        val member = createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(member.id, principal.id)
        assertEquals(member.email, principal.email)
        assertEquals(member.nickname, principal.nickname)
        assertTrue(principal.active)
        assertEquals(1, principal.getAuthorities().size)
        assertTrue(principal.getAuthorities().contains(SimpleGrantedAuthority("ROLE_USER")))
        assertNotNull(principal.createdAt)
    }

    @Test
    fun `from - success - creates principal when member is not active`() {
        val member = createMemberWithId(42L)

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(false, principal.active)
    }

    @Test
    fun `from - success - creates principal with multiple roles`() {
        val member = createMemberWithId(42L)
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
        val member = createMemberWithId(42L)
        member.activate()
        member.deactivate()

        val principal = MemberPrincipal.from(member)

        assertNotNull(principal)
        assertEquals(false, principal.active)
    }

    @Test
    fun `from - success - sets introduction value when it exists`() {
        val member = createMemberWithId(42L)
        member.activate()
        member.updateInfo(introduction = Introduction("안녕하세요"))

        val principal = MemberPrincipal.from(member)

        assertEquals("안녕하세요", principal.introduction)
    }

    @Test
    fun `from - success - sets empty string when introduction is null`() {
        val member = createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertEquals("아직 자기소개가 없어요!", principal.introduction)
    }

    @Test
    fun `from - success - sets empty string when introduction value is empty`() {
        val member = createMemberWithId(42L)
        member.activate()
        member.updateInfo(introduction = Introduction(""))  // 빈 문자열

        val principal = MemberPrincipal.from(member)

        assertEquals("", principal.introduction)  // Elvis 우측이 실행되지 않음
    }

    @Test
    fun `from - success - sets address value when it exists`() {
        val member = createMemberWithId(42L)
        member.activate()
        member.updateInfo(address = ("서울시 강남구"))

        val principal = MemberPrincipal.from(member)

        assertEquals("서울시 강남구", principal.address)
    }

    @Test
    fun `from - success - sets default address when address is null`() {
        val member = createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertEquals("아직 주소가 없어요!", principal.address)
    }

    @Test
    fun `from - success - sets default profileImageUrl`() {
        val member = createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertEquals("#", principal.profileImageUrl)
    }

    @Test
    fun `from - success - sets default followersCount and followingsCount`() {
        val member = createMemberWithId(42L)
        member.activate()

        val principal = MemberPrincipal.from(member)

        assertEquals(0L, principal.followersCount)
        assertEquals(0L, principal.followingsCount)
    }

    @Test
    fun `constructor - success - creates MemberPrincipal with all properties`() {
        val email = Email("test@example.com")
        val nickname = Nickname("testUser")
        val roles = setOf(Role.USER, Role.MANAGER)

        val principal = MemberPrincipal(
            id = 100L,
            email = email,
            nickname = nickname,
            active = true,
            introduction = "자기소개",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = roles
        )

        assertEquals(100L, principal.id)
        assertEquals(email, principal.email)
        assertEquals(nickname, principal.nickname)
        assertTrue(principal.active)
        assertEquals("자기소개", principal.introduction)
    }

    @Test
    fun `constructor - success - uses default values for optional parameters`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "자기소개",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = setOf(Role.USER)
            // profileImageUrl, followersCount, followingsCount은 기본값 사용
        )

        assertEquals("#", principal.profileImageUrl) // 기본값
        assertEquals(0L, principal.followersCount)   // 기본값
        assertEquals(0L, principal.followingsCount) // 기본값
    }

    @Test
    fun `constructor - success - sets custom values for optional parameters`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "자기소개",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            profileImageUrl = "https://example.com/profile.jpg",
            roles = setOf(Role.USER),
            followersCount = 100L,
            followingsCount = 50L
        )

        assertEquals("https://example.com/profile.jpg", principal.profileImageUrl)
        assertEquals(100L, principal.followersCount)
        assertEquals(50L, principal.followingsCount)
    }

    @Test
    fun `getAuthorities - success - maps all role types correctly`() {
        val allRoles = setOf(Role.USER, Role.MANAGER, Role.ADMIN)
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = allRoles
        )

        val authorities = principal.getAuthorities()

        assertEquals(3, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_USER" })
        assertTrue(authorities.any { it.authority == "ROLE_MANAGER" })
        assertTrue(authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `hasRole - success - returns true when user has the role`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = setOf(Role.USER, Role.ADMIN)
        )

        assertTrue(principal.hasRole(Role.USER))
        assertTrue(principal.hasRole(Role.ADMIN))
    }

    @Test
    fun `hasRole - success - returns false when user does not have the role`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = setOf(Role.USER)
        )

        assertTrue(principal.hasRole(Role.USER))
        assertFalse(principal.hasRole(Role.ADMIN))
        assertFalse(principal.hasRole(Role.MANAGER))
    }

    @Test
    fun `hasRole - success - returns false when roles is empty`() {
        val principal = MemberPrincipal(
            id = 1L,
            email = Email("test@example.com"),
            nickname = Nickname("testUser"),
            active = true,
            introduction = "",
            address = "서울시",
            createdAt = LocalDateTime.now(),
            roles = emptySet()
        )

        assertFalse(principal.hasRole(Role.USER))
        assertFalse(principal.hasRole(Role.ADMIN))
        assertFalse(principal.hasRole(Role.MANAGER))
    }

    fun createMemberWithId(
        id: Long,
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
        password: RawPassword = MemberFixture.DEFAULT_RAW_PASSWORD,
    ): Member {
        val member = Member.register(
            email = email,
            nickname = nickname,
            password = PasswordHash.of(
                password,
                MemberFixture.TEST_ENCODER
            ),
        )
        setId(member, id)
        return member
    }

    fun setId(entity: BaseEntity, id: Long) {
        val field = BaseEntity::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(entity, id)
    }
}
