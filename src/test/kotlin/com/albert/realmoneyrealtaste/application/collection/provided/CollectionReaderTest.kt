package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionNotFoundException
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.CollectionStatus
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CollectionReaderTest(
    private val collectionReader: CollectionReader,
    private val testMemberHelper: TestMemberHelper,
    private val collectionRepository: CollectionRepository,
) : IntegrationTestBase() {

    @Test
    fun `readMyCollections - success - returns member's active collections`() {
        val member = testMemberHelper.createActivatedMember()
        val collections = (1..5).map { i ->
            createTestCollection(
                ownerMemberId = member.requireId(),
                name = "컬렉션 $i"
            )
        }

        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        val result = collectionReader.readMyCollections(member.requireId(), pageRequest)

        assertEquals(collections.size, result.content.size)
        assertTrue(result.content.all { it.owner.memberId == member.id })
        assertTrue(result.content.all { it.status == CollectionStatus.ACTIVE })
    }

    @Test
    fun `readMyCollections - success - returns empty page when member has no collections`() {
        val member = testMemberHelper.createActivatedMember()
        val pageRequest = PageRequest.of(0, 10)

        val result = collectionReader.readMyCollections(member.requireId(), pageRequest)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `readMyCollections - success - excludes deleted collections`() {
        val member = testMemberHelper.createActivatedMember()
        val activeCollection = createTestCollection(member.requireId(), name = "활성 컬렉션")
        val deletedCollection = createTestCollection(member.requireId(), name = "삭제된 컬렉션")

        deletedCollection.delete(member.requireId())

        val pageRequest = PageRequest.of(0, 10)
        val result = collectionReader.readMyCollections(member.requireId(), pageRequest)

        assertEquals(1, result.content.size)
        assertEquals(activeCollection.id, result.content.first().id)
        assertTrue(result.content.all { it.status == CollectionStatus.ACTIVE })
    }

    @Test
    fun `readMyCollections - success - respects pagination`() {
        val member = testMemberHelper.createActivatedMember()
        val collections = (1..15).map { i ->
            createTestCollection(member.requireId(), name = "컬렉션 $i")
        }

        val pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        val result = collectionReader.readMyCollections(member.requireId(), pageRequest)

        assertEquals(5, result.content.size)
        assertEquals(15, result.totalElements)
        assertEquals(3, result.totalPages)
    }

    @Test
    fun `readMyPublicCollections - success - returns only public collections`() {
        val member = testMemberHelper.createActivatedMember()
        val publicCollection1 = createTestCollection(
            member.requireId(),
            name = "공개 컬렉션 1",
            privacy = CollectionPrivacy.PUBLIC
        )
        val privateCollection = createTestCollection(
            member.requireId(),
            name = "비공개 컬렉션",
            privacy = CollectionPrivacy.PRIVATE
        )
        val publicCollection2 = createTestCollection(
            member.requireId(),
            name = "공개 컬렉션 2",
            privacy = CollectionPrivacy.PUBLIC
        )

        val pageRequest = PageRequest.of(0, 10)
        val result = collectionReader.readMyPublicCollections(member.requireId(), pageRequest)

        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.privacy == CollectionPrivacy.PUBLIC })
        assertTrue(result.content.map { it.info.name }.containsAll(listOf("공개 컬렉션 1", "공개 컬렉션 2")))
    }

    @Test
    fun `readMyPublicCollections - success - excludes deleted collections`() {
        val member = testMemberHelper.createActivatedMember()
        val publicCollection = createTestCollection(
            member.requireId(),
            name = "공개 컬렉션",
            privacy = CollectionPrivacy.PUBLIC
        )
        val deletedPublicCollection = createTestCollection(
            member.requireId(),
            name = "삭제된 공개 컬렉션",
            privacy = CollectionPrivacy.PUBLIC
        )

        deletedPublicCollection.delete(member.requireId())

        val pageRequest = PageRequest.of(0, 10)
        val result = collectionReader.readMyPublicCollections(member.requireId(), pageRequest)

        assertEquals(1, result.content.size)
        assertEquals(publicCollection.id, result.content.first().id)
    }

    @Test
    fun `readMyPublicCollections - success - returns empty when no public collections`() {
        val member = testMemberHelper.createActivatedMember()
        createTestCollection(
            member.requireId(),
            name = "비공개 컬렉션",
            privacy = CollectionPrivacy.PRIVATE
        )

        val pageRequest = PageRequest.of(0, 10)
        val result = collectionReader.readMyPublicCollections(member.requireId(), pageRequest)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `readById - success - returns collection when exists`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        val result = collectionReader.readById(collection.requireId())

        assertEquals(collection.id, result.id)
        assertEquals(collection.info.name, result.info.name)
        assertEquals(member.id, result.owner.memberId)
    }

    @Test
    fun `readById - success - returns collection even when private`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(
            member.requireId(),
            privacy = CollectionPrivacy.PRIVATE
        )

        val result = collectionReader.readById(collection.requireId())

        assertEquals(collection.id, result.id)
        assertEquals(CollectionPrivacy.PRIVATE, result.privacy)
    }

    @Test
    fun `readById - failure - throws exception when collection not found`() {
        val nonExistentId = 999L

        assertFailsWith<CollectionNotFoundException> {
            collectionReader.readById(nonExistentId)
        }.let {
            assertEquals("컬렉션을 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `readById - failure - throws exception when collection is deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        collection.delete(member.requireId())

        assertFailsWith<CollectionNotFoundException> {
            collectionReader.readById(collection.requireId())
        }.let {
            assertEquals("컬렉션을 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `readMyCollections - success - returns only member's collections`() {
        val member1 = testMemberHelper.createActivatedMember()
        val member2 = testMemberHelper.createActivatedMember(
            email = "member2@test.com",
            nickname = "회원2"
        )

        val member1Collection = createTestCollection(member1.requireId(), name = "회원1 컬렉션")
        val member2Collection = createTestCollection(member2.requireId(), name = "회원2 컬렉션")

        val pageRequest = PageRequest.of(0, 10)
        val result1 = collectionReader.readMyCollections(member1.requireId(), pageRequest)
        val result2 = collectionReader.readMyCollections(member2.requireId(), pageRequest)

        assertEquals(1, result1.content.size)
        assertEquals(1, result2.content.size)
        assertEquals(member1Collection.id, result1.content.first().id)
        assertEquals(member2Collection.id, result2.content.first().id)
    }

    @Test
    fun `readMyPublicCollections - success - respects pagination`() {
        val member = testMemberHelper.createActivatedMember()
        val publicCollections = (1..12).map { i ->
            createTestCollection(
                member.requireId(),
                name = "공개 컬렉션 $i",
                privacy = CollectionPrivacy.PUBLIC
            )
        }

        val pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        val result = collectionReader.readMyPublicCollections(member.requireId(), pageRequest)

        assertEquals(5, result.content.size)
        assertEquals(publicCollections.size.toLong(), result.totalElements)
        assertTrue(result.content.all { it.privacy == CollectionPrivacy.PUBLIC })
    }

    private fun createTestCollection(
        ownerMemberId: Long,
        name: String = "테스트 컬렉션",
        description: String = "테스트용 컬렉션입니다",
        coverImageUrl: String? = "https://example.com/test.jpg",
        privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
    ): PostCollection {
        val command = CollectionCreateCommand(
            ownerMemberId = ownerMemberId,
            name = name,
            description = description,
            coverImageUrl = coverImageUrl,
            privacy = privacy,
            ownerName = "test"
        )
        return collectionRepository.save(PostCollection.create(command))
    }
}
