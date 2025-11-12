package com.albert.realmoneyrealtaste.domain.member.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RolesTest {

    @Test
    fun `ofUser - success - creates roles with USER role only`() {
        val roles = Roles.ofUser()

        assertTrue(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.MANAGER))
        assertFalse(roles.hasRole(Role.ADMIN))
        assertEquals(1, roles.getRoles().size)
    }

    @Test
    fun `of - success - creates roles with specified roles`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
        assertFalse(roles.hasRole(Role.ADMIN))
        assertEquals(2, roles.getRoles().size)
    }

    @Test
    fun `of - success - creates roles with single role`() {
        val roles = Roles.of(Role.ADMIN)

        assertTrue(roles.hasRole(Role.ADMIN))
        assertFalse(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.MANAGER))
        assertEquals(1, roles.getRoles().size)
    }

    @Test
    fun `of - failure - throws exception when no roles provided`() {
        assertFailsWith<IllegalArgumentException> {
            Roles.of()
        }.let {
            assertEquals("적어도 하나의 역할이 필요합니다", it.message)
        }
    }

    @Test
    fun `of - success - removes duplicate roles`() {
        val roles = Roles.of(Role.USER, Role.USER, Role.MANAGER)

        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
        assertEquals(2, roles.getRoles().size)
    }

    @Test
    fun `getRoles - success - returns immutable copy of roles`() {
        val roles = Roles.ofUser()
        val roleSet = roles.getRoles()

        assertTrue(roleSet.contains(Role.USER))
        assertEquals(1, roleSet.size)

        roles.addRole(Role.MANAGER)
        assertEquals(1, roleSet.size)
        assertFalse(roleSet.contains(Role.MANAGER))
    }

    @Test
    fun `hasRole - success - returns true when role exists`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
    }

    @Test
    fun `hasRole - success - returns false when role does not exist`() {
        val roles = Roles.ofUser()

        assertFalse(roles.hasRole(Role.ADMIN))
        assertFalse(roles.hasRole(Role.MANAGER))
    }

    @Test
    fun `hasAnyRole - success - returns true when at least one role exists`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        assertTrue(roles.hasAnyRole(Role.USER))
        assertTrue(roles.hasAnyRole(Role.MANAGER))
        assertTrue(roles.hasAnyRole(Role.USER, Role.ADMIN))
        assertTrue(roles.hasAnyRole(Role.ADMIN, Role.MANAGER))
    }

    @Test
    fun `hasAnyRole - success - returns false when no roles exist`() {
        val roles = Roles.ofUser()

        assertFalse(roles.hasAnyRole(Role.ADMIN))
        assertFalse(roles.hasAnyRole(Role.MANAGER, Role.ADMIN))
    }

    @Test
    fun `hasAnyRole - success - returns false when empty array provided`() {
        val roles = Roles.ofUser()

        assertFalse(roles.hasAnyRole())
    }

    @Test
    fun `addRole - success - adds new role`() {
        val roles = Roles.ofUser()

        roles.addRole(Role.MANAGER)

        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
        assertEquals(2, roles.getRoles().size)
    }

    @Test
    fun `addRole - success - does not duplicate existing role`() {
        val roles = Roles.ofUser()

        roles.addRole(Role.USER)

        assertTrue(roles.hasRole(Role.USER))
        assertEquals(1, roles.getRoles().size)
    }

    @Test
    fun `removeRole - success - removes role when multiple roles exist`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        roles.removeRole(Role.MANAGER)

        assertTrue(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.MANAGER))
        assertEquals(1, roles.getRoles().size)
    }

    @Test
    fun `removeRole - success - removes USER role when multiple roles exist`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        roles.removeRole(Role.USER)

        assertFalse(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
        assertEquals(1, roles.getRoles().size)
    }

    @Test
    fun `removeRole - failure - throws exception when trying to remove last USER role`() {
        val roles = Roles.ofUser()

        assertFailsWith<IllegalArgumentException> {
            roles.removeRole(Role.USER)
        }.let {
            assertEquals("적어도 하나의 역할이 필요합니다", it.message)
        }
    }

    @Test
    fun `isAdmin - success - returns true when ADMIN role exists`() {
        val roles = Roles.of(Role.USER, Role.ADMIN)

        assertTrue(roles.isAdmin())
    }

    @Test
    fun `isAdmin - success - returns false when ADMIN role does not exist`() {
        val roles = Roles.ofUser()

        assertFalse(roles.isAdmin())
    }

    @Test
    fun `isManager - success - returns true when MANAGER role exists`() {
        val roles = Roles.of(Role.USER, Role.MANAGER)

        assertTrue(roles.isManager())
    }

    @Test
    fun `isManager - success - returns false when MANAGER role does not exist`() {
        val roles = Roles.ofUser()

        assertFalse(roles.isManager())
    }

    @Test
    fun `combined operations - success - multiple add and remove operations`() {
        val roles = Roles.ofUser()

        roles.addRole(Role.MANAGER)
        roles.addRole(Role.ADMIN)
        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.MANAGER))
        assertTrue(roles.hasRole(Role.ADMIN))
        assertEquals(3, roles.getRoles().size)

        roles.removeRole(Role.MANAGER)
        assertTrue(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.MANAGER))
        assertTrue(roles.hasRole(Role.ADMIN))
        assertEquals(2, roles.getRoles().size)

        roles.removeRole(Role.ADMIN)
        assertTrue(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.ADMIN))
        assertEquals(1, roles.getRoles().size)
    }
}
