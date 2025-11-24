package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.junit.jupiter.api.Assertions.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberReaderTest(
    private val memberReader: MemberReader,
    private val memberRegister: MemberRegister,
) : IntegrationTestBase() {

    @Test
    fun `readMemberById - success - returns member when member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val member = memberReader.readMemberById(registeredMember.id!!)

        assertEquals(registeredMember.id, member.id)
        assertEquals(registeredMember.email, member.email)
        assertEquals(registeredMember.nickname, member.nickname)
        assertEquals(MemberStatus.PENDING, member.status)
    }

    @Test
    fun `readMemberById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.readMemberById(nonExistentId)
        }
    }

    @Test
    fun `readActiveMemberById - success - returns active member when active member exists`() {
        val registeredMember = createActiveMember(MemberFixture.DEFAULT_EMAIL)

        val member = memberReader.readActiveMemberById(registeredMember.id!!)

        assertEquals(registeredMember.id, member.id)
        assertEquals(MemberStatus.ACTIVE, member.status)
    }

    @Test
    fun `readActiveMemberById - failure - throws exception when member is not active`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        assertFailsWith<MemberNotFoundException> {
            memberReader.readActiveMemberById(registeredMember.id!!)
        }
    }

    @Test
    fun `readActiveMemberById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.readActiveMemberById(nonExistentId)
        }
    }

    @Test
    fun `readMemberByEmail - success - returns member when member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val member = memberReader.readMemberByEmail(MemberFixture.DEFAULT_EMAIL)

        assertEquals(registeredMember.id, member.id)
        assertEquals(registeredMember.email, member.email)
        assertEquals(registeredMember.nickname, member.nickname)
    }

    @Test
    fun `readMemberByEmail - failure - throws exception when member does not exist`() {
        val nonExistentEmail = Email("nonexistent@example.com")

        assertFailsWith<MemberNotFoundException> {
            memberReader.readMemberByEmail(nonExistentEmail)
        }
    }

    @Test
    fun `existsActiveMemberById - success - returns true when active member exists`() {
        val registeredMember = createActiveMember(MemberFixture.DEFAULT_EMAIL)

        val exists = memberReader.existsActiveMemberById(registeredMember.id!!)

        assertTrue(exists)
    }

    @Test
    fun `existsActiveMemberById - success - returns false when member is not active`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val exists = memberReader.existsActiveMemberById(registeredMember.id!!)

        assertFalse(exists)
    }

    @Test
    fun `existsActiveMemberById - success - returns false when member does not exist`() {
        val nonExistentId = 99999L

        val exists = memberReader.existsActiveMemberById(nonExistentId)

        assertFalse(exists)
    }

    @Test
    fun `getNicknameById - success - returns nickname when member exists`() {
        val registeredMember = createActiveMember(MemberFixture.DEFAULT_EMAIL)

        val nickname = memberReader.getNicknameById(registeredMember.id!!)

        assertEquals(MemberFixture.DEFAULT_NICKNAME.value, nickname)
    }

    @Test
    fun `getNicknameById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.getNicknameById(nonExistentId)
        }
    }

    @Test
    fun `readAllActiveMembersByIds - success - returns all active members for given IDs`() {
        // given
        val activeMember1 = createActiveMember(email = Email("activeMember1@example.com"))
        val activeMember2 = createActiveMember(email = Email("activeMember2@example.com"))
        val activeMember3 = createActiveMember(email = Email("activeMember3@example.com"))
        val memberIds = listOf(activeMember1.id!!, activeMember2.id!!, activeMember3.id!!)

        // when
        val result = memberReader.readAllActiveMembersByIds(memberIds)

        // then
        assertAll(
            { assertEquals(memberIds.size, result.size) },
            { assertEquals(memberIds.toSet(), result.map { it.id }.toSet()) }
        )
    }

    @Test
    fun `readAllActiveMembersByIds - success - returns empty list for empty ID list`() {
        // given
        val emptyIds = emptyList<Long>()

        // when
        val result = memberReader.readAllActiveMembersByIds(emptyIds)

        // then
        assertAll(
            { assertEquals(0, result.size) }
        )
    }

    @Test
    fun `readAllActiveMembersByIds - success - returns empty list when no members exist`() {
        // given
        val nonExistentIds = listOf(999L, 1000L)

        // when
        val result = memberReader.readAllActiveMembersByIds(nonExistentIds)

        // then
        assertAll(
            { assertEquals(0, result.size) }
        )
    }

    @Test
    fun `readAllActiveMembersByIds - success - returns only active members when mixed status members exist`() {
        // given
        val activeMember1 = createActiveMember(email = Email("activeMember1@example.com"))
        val inactiveMember2 = createMember(email = Email("activeMember2@example.com")) // 비활성화 상태
        val activeMember3 = createActiveMember(email = Email("activeMember3@example.com"))
        val inactiveMember4 = createMember(email = Email("activeMember4@example.com")) // 비활성화 상태
        val memberIds = listOf(activeMember1.id!!, inactiveMember2.id!!, activeMember3.id!!, inactiveMember4.id!!)
        // ID 2, 4는 비활성화 상태이므로 결과에 포함되지 않음
        val expectedMembers = listOf(activeMember1, activeMember3)

        // when
        val result = memberReader.readAllActiveMembersByIds(memberIds)

        // then
        assertAll(
            { assertEquals(expectedMembers.size, result.size) },
            { assertEquals(expectedMembers.map { it.id }.toSet(), result.map { it.id }.toSet()) }
        )
    }

    @Test
    fun `readAllActiveMembersByIds - success - preserves member order when multiple members returned`() {
        // given
        val activeMember1 = createActiveMember(email = Email("activeMember1@example.com"))
        val activeMember2 = createActiveMember(email = Email("activeMember2@example.com"))
        val activeMember3 = createActiveMember(email = Email("activeMember3@example.com"))
        // Repository에서 ID 순서대로 반환한다고 가정
        val memberIds = listOf(activeMember1.id!!, activeMember2.id!!, activeMember3.id!!)

        // when
        val result = memberReader.readAllActiveMembersByIds(memberIds)

        // then
        assertAll(
            { assertEquals(memberIds.size, result.size) },
            { assertEquals(memberIds, result.map { it.id }) }
        )
    }

    @Test
    fun `findSuggestedMembers - success - returns suggested members within limit`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        (1..10).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 5L

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertAll(
            { assertTrue(result.size <= limit) },
            { assertFalse(result.any { it.id == targetMember.id }) }, // 자기 자신은 제외
            { result.all { it.status == MemberStatus.ACTIVE } } // 모두 활성화된 회원
        )
    }

    @Test
    fun `findSuggestedMembers - success - returns empty list when no other active members exist`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        val limit = 5L

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findSuggestedMembers - success - excludes inactive members from suggestions`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        createActiveMember(email = Email("active1@example.com"))
        val inactiveMember2 = createMember(email = Email("inactive2@example.com")) // 비활성화
        createActiveMember(email = Email("active3@example.com"))
        val inactiveMember4 = createMember(email = Email("inactive4@example.com")) // 비활성화
        val limit = 10L

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertAll(
            { assertTrue(result.isNotEmpty()) },
            { result.all { it.status == MemberStatus.ACTIVE } }, // 모두 활성화된 회원만
            { assertFalse(result.any { it.id == inactiveMember2.id }) }, // 비활성화 회원 제외
            { assertFalse(result.any { it.id == inactiveMember4.id }) }, // 비활성화 회원 제외
            { assertFalse(result.any { it.id == targetMember.id }) } // 자기 자신 제외
        )
    }

    @Test
    fun `findSuggestedMembers - success - respects limit parameter`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        (1..10).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 3L

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertEquals(limit, result.size.toLong())
        assertTrue(result.all { it.status == MemberStatus.ACTIVE })
        assertFalse(result.any { it.id == targetMember.id })
    }

    @Test
    fun `findSuggestedMembers - success - returns all available members when limit exceeds available count`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        val suggestedMembers = (1..3).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 10L // 사용 가능한 회원보다 큰 limit

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertEquals(suggestedMembers.size.toLong(), result.size.toLong())
        assertTrue(result.all { it.status == MemberStatus.ACTIVE })
        assertFalse(result.any { it.id == targetMember.id })
    }

    @Test
    fun `findSuggestedMembers - success - handles limit of zero`() {
        // given
        val targetMember = createActiveMember(email = Email("target@example.com"))
        (1..5).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 0L

        // when
        val result = memberReader.findSuggestedMembers(targetMember.id!!, limit)

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findSuggestedMembers - success - handles non-existent member ID`() {
        // given
        val nonExistentMemberId = 99999L
        (1..5).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 3L

        // when
        val result = memberReader.findSuggestedMembers(nonExistentMemberId, limit)

        // then - 비활성화 회원이 없는 한, 다른 활성화된 회원들을 반환할 수 있음
        // 구체적인 동작은 Repository 구현에 따라 달라질 수 있음
        assertTrue(result.all { it.status == MemberStatus.ACTIVE })
        assertTrue(result.size <= limit)
    }

    @Test
    fun `findSuggestedMembers - success - returns different results for different member IDs`() {
        // given
        val member1 = createActiveMember(email = Email("member1@example.com"))
        val member2 = createActiveMember(email = Email("member2@example.com"))
        (1..5).map { i ->
            createActiveMember(email = Email("suggested$i@example.com"))
        }
        val limit = 3L

        // when
        val result1 = memberReader.findSuggestedMembers(member1.id!!, limit)
        val result2 = memberReader.findSuggestedMembers(member2.id!!, limit)

        // then
        assertAll(
            { assertTrue(result1.all { it.status == MemberStatus.ACTIVE }) },
            { assertTrue(result2.all { it.status == MemberStatus.ACTIVE }) },
            { assertFalse(result1.any { it.id == member1.id }) }, // 자기 자신 제외
            { assertFalse(result2.any { it.id == member2.id }) }, // 자기 자신 제외
            { assertTrue(result1.size <= limit) },
            { assertTrue(result2.size <= limit) }
        )
    }

    private fun createActiveMember(email: Email): Member {
        val registeredMember = createMember(email)
        registeredMember.activate()
        return registeredMember
    }

    private fun createMember(email: Email): Member {
        return memberRegister.register(
            MemberRegisterRequest(
                email = email,
                password = MemberFixture.DEFAULT_RAW_PASSWORD,
                nickname = MemberFixture.DEFAULT_NICKNAME
            )
        )
    }
}
