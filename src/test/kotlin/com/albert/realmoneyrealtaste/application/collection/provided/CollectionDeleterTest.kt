package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionDeleteException
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.CollectionStatus
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CollectionDeleterTest(
    private val collectionDeleter: CollectionDeleter,
    private val collectionRepository: CollectionRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Test
    fun `deleteCollection - success - deletes collection when owner requests`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        collectionRepository.save(collection)

        collectionDeleter.deleteCollection(collection.requireId(), member.requireId())

        val deletedCollection = collectionRepository.findById(collection.requireId())
        assertNotNull(deletedCollection)
        assertEquals(CollectionStatus.DELETED, deletedCollection.status)
    }

    @Test
    fun `deleteCollection - success - persists deletion to database`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        collectionDeleter.deleteCollection(collection.requireId(), member.requireId())

        val persistedCollection = collectionRepository.findByIdAndStatusNot(
            collection.requireId(),
            CollectionStatus.DELETED
        )
        assertEquals(null, persistedCollection) // 삭제된 컬렉션은 조회되지 않아야 함
    }

    @Test
    fun `deleteCollection - success - deletes private collection`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(
            member.requireId(),
            privacy = CollectionPrivacy.PRIVATE
        )

        collectionDeleter.deleteCollection(collection.requireId(), member.requireId())

        val deletedCollection = collectionRepository.findById(collection.requireId())
        assertNotNull(deletedCollection)
        assertEquals(CollectionStatus.DELETED, deletedCollection.status)
    }

    @Test
    fun `deleteCollection - success - deletes public collection`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(
            member.requireId(),
            privacy = CollectionPrivacy.PUBLIC
        )

        collectionDeleter.deleteCollection(collection.requireId(), member.requireId())

        val deletedCollection = collectionRepository.findById(collection.requireId())
        assertNotNull(deletedCollection)
        assertEquals(CollectionStatus.DELETED, deletedCollection.status)
    }

    @Test
    fun `deleteCollection - failure - throws exception when collection not found`() {
        val member = testMemberHelper.createActivatedMember()
        val nonExistentCollectionId = 999L

        assertFailsWith<CollectionDeleteException> {
            collectionDeleter.deleteCollection(nonExistentCollectionId, member.requireId())
        }.let {
            assertEquals("컬렉션을 삭제할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `deleteCollection - failure - throws exception when requester is not owner`() {
        val owner = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람"
        )
        val collection = createTestCollection(owner.requireId())

        assertFailsWith<IllegalArgumentException> {
            collectionDeleter.deleteCollection(collection.requireId(), other.requireId())
        }
    }

    @Test
    fun `deleteCollection - failure - does not delete collection when not owner`() {
        val owner = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람"
        )
        val collection = createTestCollection(owner.requireId())

        try {
            collectionDeleter.deleteCollection(collection.requireId(), other.requireId())
        } catch (e: IllegalArgumentException) {
            // 예외가 발생해야 함
        }

        val unchangedCollection = collectionRepository.findById(collection.requireId())
        assertNotNull(unchangedCollection)
        assertEquals(CollectionStatus.ACTIVE, unchangedCollection.status)
    }

    @Test
    fun `deleteCollection - failure - throws exception when collection already deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        collection.delete(member.requireId()) // 이미 삭제

        assertFailsWith<CollectionDeleteException> {
            collectionDeleter.deleteCollection(collection.requireId(), member.requireId())
        }.let {
            assertEquals("컬렉션을 삭제할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `deleteCollection - success - can delete multiple collections`() {
        val member = testMemberHelper.createActivatedMember()
        val collection1 = createTestCollection(member.requireId(), name = "첫 번째 컬렉션")
        val collection2 = createTestCollection(member.requireId(), name = "두 번째 컬렉션")
        val collection3 = createTestCollection(member.requireId(), name = "세 번째 컬렉션")

        collectionDeleter.deleteCollection(collection1.requireId(), member.requireId())
        collectionDeleter.deleteCollection(collection2.requireId(), member.requireId())
        collectionDeleter.deleteCollection(collection3.requireId(), member.requireId())

        val deletedCollection1 = collectionRepository.findById(collection1.requireId())!!
        val deletedCollection2 = collectionRepository.findById(collection2.requireId())!!
        val deletedCollection3 = collectionRepository.findById(collection3.requireId())!!

        assertEquals(CollectionStatus.DELETED, deletedCollection1.status)
        assertEquals(CollectionStatus.DELETED, deletedCollection2.status)
        assertEquals(CollectionStatus.DELETED, deletedCollection3.status)
    }

    @Test
    fun `deleteCollection - success - maintains other properties after deletion`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(
            member.requireId(),
            name = "삭제될 컬렉션",
            description = "삭제되지만 다른 속성은 유지",
            privacy = CollectionPrivacy.PRIVATE
        )
        val originalId = collection.id
        val originalInfo = collection.info
        val originalOwner = collection.owner
        val originalCreatedAt = collection.createdAt

        collectionDeleter.deleteCollection(collection.requireId(), member.requireId())

        val deletedCollection = collectionRepository.findById(collection.requireId())
        assertNotNull(deletedCollection)
        assertEquals(originalId, deletedCollection.id)
        assertEquals(originalInfo.name, deletedCollection.info.name)
        assertEquals(originalInfo.description, deletedCollection.info.description)
        assertEquals(originalOwner.memberId, deletedCollection.owner.memberId)
        assertEquals(originalCreatedAt, deletedCollection.createdAt)
        assertEquals(CollectionStatus.DELETED, deletedCollection.status)
    }

    @Test
    fun `deleteCollection - failure - non-existent member cannot delete`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val nonExistentMemberId = 999L

        assertFailsWith<IllegalArgumentException> {
            collectionDeleter.deleteCollection(collection.requireId(), nonExistentMemberId)
        }
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
