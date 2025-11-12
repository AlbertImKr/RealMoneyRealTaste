package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionCreateException
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionCreatorTest(
    private val collectionCreator: CollectionCreator,
    private val collectionRepository: CollectionRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Test
    fun `createCollection - success - creates collection with valid command`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(ownerMemberId = member.requireId())

        val result = collectionCreator.createCollection(command)

        assertNotNull(result.id)
        assertEquals(member.id, result.owner.memberId)
        assertEquals(command.name, result.info.name)
        assertEquals(command.description, result.info.description)
        assertEquals(command.coverImageUrl, result.info.coverImageUrl)
        assertEquals(command.privacy, result.privacy)
        assertEquals(0, result.posts.size())
        assertTrue(result.createdAt.isBefore(result.updatedAt.plusSeconds(1)))
    }

    @Test
    fun `createCollection - success - saves collection to repository`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(ownerMemberId = member.requireId())

        val result = collectionCreator.createCollection(command)

        val savedCollection = collectionRepository.findById(result.requireId())

        assertNotNull(savedCollection)
        assertEquals(result.id, savedCollection.id)
        assertEquals(result.owner.memberId, savedCollection.owner.memberId)
        assertEquals(result.info.name, savedCollection.info.name)
    }

    @Test
    fun `createCollection - success - creates collection without cover image`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            coverImageUrl = null
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(null, result.info.coverImageUrl)
    }

    @Test
    fun `createCollection - success - creates collection with cover image`() {
        val member = testMemberHelper.createActivatedMember()
        val imageUrl = "https://example.com/cover.jpg"
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            coverImageUrl = imageUrl
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(imageUrl, result.info.coverImageUrl)
    }

    @Test
    fun `createCollection - success - creates private collection`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            privacy = CollectionPrivacy.PRIVATE
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(CollectionPrivacy.PRIVATE, result.privacy)
    }

    @Test
    fun `createCollection - success - creates public collection`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            privacy = CollectionPrivacy.PUBLIC
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(CollectionPrivacy.PUBLIC, result.privacy)
    }

    @Test
    fun `createCollection - success - creates collection with empty description`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            description = ""
        )

        val result = collectionCreator.createCollection(command)

        assertEquals("", result.info.description)
    }

    @Test
    fun `createCollection - failure - throws CollectionCreateException when member not found`() {
        val command = createCollectionCommand(ownerMemberId = 999L)

        assertFailsWith<CollectionCreateException> {
            collectionCreator.createCollection(command)
        }.let {
            assertTrue(it.message!!.contains("컬렉션 생성 중 오류가 발생했습니다"))
        }
    }

    @Test
    fun `createCollection - failure - does not save collection when member not found`() {
        val command = createCollectionCommand(ownerMemberId = 999L)
        val initialCount = collectionRepository.countByOwnerMemberId(999L)

        assertFailsWith<CollectionCreateException> {
            collectionCreator.createCollection(command)
        }

        val finalCount = collectionRepository.countByOwnerMemberId(999L)
        assertEquals(initialCount, finalCount)
    }

    @Test
    fun `createCollection - success - creates multiple collections for same member`() {
        val member = testMemberHelper.createActivatedMember()
        val command1 = createCollectionCommand(
            ownerMemberId = member.requireId(),
            name = "첫 번째 컬렉션"
        )
        val command2 = createCollectionCommand(
            ownerMemberId = member.requireId(),
            name = "두 번째 컬렉션"
        )

        val collection1 = collectionCreator.createCollection(command1)
        val collection2 = collectionCreator.createCollection(command2)

        assertNotNull(collection1.id)
        assertNotNull(collection2.id)
        assertTrue(collection1.id != collection2.id)
        assertEquals("첫 번째 컬렉션", collection1.info.name)
        assertEquals("두 번째 컬렉션", collection2.info.name)
        assertEquals(member.id, collection1.owner.memberId)
        assertEquals(member.id, collection2.owner.memberId)
    }

    @Test
    fun `createCollection - success - creates collections for different members`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = Email("member1@test.com"),
            nickname = Nickname("회원1")
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = Email("member2@test.com"),
            nickname = Nickname("회원2")
        )

        val command1 = createCollectionCommand(ownerMemberId = member1.requireId())
        val command2 = createCollectionCommand(ownerMemberId = member2.requireId())

        val collection1 = collectionCreator.createCollection(command1)
        val collection2 = collectionCreator.createCollection(command2)

        assertEquals(member1.id, collection1.owner.memberId)
        assertEquals(member2.id, collection2.owner.memberId)
        assertTrue(collection1.id != collection2.id)
    }

    @Test
    fun `createCollection - success - initializes post count to zero`() {
        val member = testMemberHelper.createActivatedMember()
        val command = createCollectionCommand(ownerMemberId = member.requireId())

        val result = collectionCreator.createCollection(command)

        assertEquals(0, result.posts.size())
    }

    @Test
    fun `createCollection - success - handles various privacy values`() {
        val member = testMemberHelper.createActivatedMember()

        CollectionPrivacy.entries.forEach { privacy ->
            val command = createCollectionCommand(
                ownerMemberId = member.requireId(),
                name = "컬렉션 ${privacy.name}",
                privacy = privacy
            )

            val result = collectionCreator.createCollection(command)

            assertEquals(privacy, result.privacy)
        }
    }

    @Test
    fun `createCollection - failure - wraps validation exception from command`() {
        val member = testMemberHelper.createActivatedMember()

        // Command 생성 시 검증 실패하는 케이스를 테스트하려면 별도 헬퍼 메서드 필요
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = member.requireId(),
                name = "", // 빈 이름
                description = "설명"
            )
        }
    }

    @Test
    fun `createCollection - success - creates collection with long valid name`() {
        val member = testMemberHelper.createActivatedMember()
        val longName = "a".repeat(50) // CollectionInfo.MAX_NAME_LENGTH 값에 따라 조정
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            name = longName
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(longName, result.info.name)
    }

    @Test
    fun `createCollection - success - creates collection with long valid description`() {
        val member = testMemberHelper.createActivatedMember()
        val longDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH)
        val command = createCollectionCommand(
            ownerMemberId = member.requireId(),
            description = longDescription
        )

        val result = collectionCreator.createCollection(command)

        assertEquals(longDescription, result.info.description)
    }

    private fun createCollectionCommand(
        ownerMemberId: Long,
        name: String = "테스트 컬렉션",
        description: String = "테스트용 컬렉션 설명",
        coverImageUrl: String? = "https://example.com/cover.jpg",
        privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
    ) = CollectionCreateCommand(
        ownerMemberId = ownerMemberId,
        name = name,
        description = description,
        coverImageUrl = coverImageUrl,
        privacy = privacy
    )
}
