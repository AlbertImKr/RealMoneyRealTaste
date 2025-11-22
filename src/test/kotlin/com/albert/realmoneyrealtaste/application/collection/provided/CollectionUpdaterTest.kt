package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.dto.CollectionUpdateRequest
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionUpdaterTest : IntegrationTestBase() {

    @Autowired
    private lateinit var collectionUpdater: CollectionUpdater

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    fun `updateInfo - success - updates collection info and returns updated collection`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), ownerName = member.nickname.value)

        val newInfo = CollectionInfo(
            name = "수정된 컬렉션",
            description = "수정된 설명",
            coverImageUrl = "https://example.com/new-cover.jpg"
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        val result = collectionUpdater.updateInfo(request)

        assertAll(
            { assertEquals(collection.id, result.id) },
            { assertEquals("수정된 컬렉션", result.info.name) },
            { assertEquals("수정된 설명", result.info.description) },
            { assertEquals("https://example.com/new-cover.jpg", result.info.coverImageUrl) },
            { assertEquals(member.id, result.owner.memberId) },
            { assertEquals(member.nickname.value, result.owner.nickname) }
        )
    }

    @Test
    fun `updateInfo - success - persists changes to database`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val originalUpdatedAt = collection.updatedAt

        val newInfo = CollectionInfo(
            name = "DB 저장 테스트",
            description = "데이터베이스 저장 확인용",
            coverImageUrl = null
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        collectionUpdater.updateInfo(request)

        val updated = collectionRepository.findById(collection.requireId())
        assertAll(
            { assertNotNull(updated) },
            { assertEquals("DB 저장 테스트", updated?.info?.name) },
            { assertEquals("데이터베이스 저장 확인용", updated?.info?.description) },
            { assertEquals(null, updated?.info?.coverImageUrl) },
            { assertTrue(originalUpdatedAt.isBefore(updated?.updatedAt)) }
        )
    }

    @Test
    fun `updateInfo - success - updates with empty description`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        val newInfo = CollectionInfo(
            name = "빈 설명 테스트",
            description = "",
            coverImageUrl = collection.info.coverImageUrl
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        val result = collectionUpdater.updateInfo(request)

        assertAll(
            { assertEquals("빈 설명 테스트", result.info.name) },
            { assertEquals("", result.info.description) }
        )
    }

    @Test
    fun `updateInfo - success - updates with null cover image url`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        val newInfo = CollectionInfo(
            name = collection.info.name,
            description = collection.info.description,
            coverImageUrl = null
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        val result = collectionUpdater.updateInfo(request)

        assertEquals(null, result.info.coverImageUrl)
    }

    @Test
    fun `updateInfo - failure - throws CollectionUpdateException when collection not found`() {
        val member = testMemberHelper.createActivatedMember()

        val newInfo = CollectionInfo(
            name = "존재하지 않는 컬렉션",
            description = "테스트",
            coverImageUrl = null
        )
        val request = CollectionUpdateRequest(
            collectionId = 999L,
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updateInfo(request)
        }.let {
            assertAll(
                { assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다.", it.message) },
                { assertTrue(it.cause is IllegalArgumentException) }
            )
        }
    }

    @Test
    fun `updateInfo - failure - throws CollectionUpdateException when not owner`() {
        val owner = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람"
        )
        val collection = createTestCollection(owner.requireId())

        val newInfo = CollectionInfo(
            name = "권한 없는 수정",
            description = "다른 사람이 수정 시도",
            coverImageUrl = null
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = other.requireId(),
            newInfo = newInfo
        )

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updateInfo(request)
        }.let {
            assertAll(
                { assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다.", it.message) },
                { assertTrue(it.cause is IllegalArgumentException) }
            )
        }
    }

    @Test
    fun `updateInfo - failure - does not persist changes when update fails`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val originalName = collection.info.name
        val originalDescription = collection.info.description

        val newInfo = CollectionInfo(
            name = "실패 테스트",
            description = "저장되면 안 됨",
            coverImageUrl = null
        )
        val request = CollectionUpdateRequest(
            collectionId = 999L, // 존재하지 않는 ID
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updateInfo(request)
        }

        val unchanged = collectionRepository.findById(collection.requireId())
        assertAll(
            { assertNotNull(unchanged) },
            { assertEquals(originalName, unchanged?.info?.name) },
            { assertEquals(originalDescription, unchanged?.info?.description) }
        )
    }

    @Test
    fun `updateInfo - failure - throws CollectionUpdateException when invalid info provided`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        // CollectionInfo 생성 시 validation 실패하는 케이스
        assertFailsWith<IllegalArgumentException> {
            val invalidInfo = CollectionInfo(
                name = "", // 빈 이름으로 validation 실패
                description = "설명",
                coverImageUrl = null
            )
            val request = CollectionUpdateRequest(
                collectionId = collection.requireId(),
                ownerMemberId = member.requireId(),
                newInfo = invalidInfo
            )

            collectionUpdater.updateInfo(request)
        }
    }

    @Test
    fun `updateInfo - success - maintains other collection properties`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val originalPrivacy = collection.privacy
        val originalPostCount = collection.posts.postIds.size
        val originalCreatedAt = collection.createdAt

        val newInfo = CollectionInfo(
            name = "속성 유지 테스트",
            description = "다른 속성들이 유지되는지 확인",
            coverImageUrl = "https://example.com/maintained.jpg"
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        val result = collectionUpdater.updateInfo(request)

        assertAll(
            { assertEquals(originalPrivacy, result.privacy) },
            { assertEquals(originalPostCount, result.posts.postIds.size) },
            { assertEquals(originalCreatedAt, result.createdAt) },
            { assertEquals(member.id, result.owner.memberId) }
        )
    }

    @Test
    fun `updateInfo - success - handles long valid values`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())

        val longName = "a".repeat(100) // MAX_NAME_LENGTH에 맞춰 조정
        val longDescription = "b".repeat(500) // MAX_DESCRIPTION_LENGTH에 맞춰 조정
        val newInfo = CollectionInfo(
            name = longName,
            description = longDescription,
            coverImageUrl = "https://example.com/long-test.jpg"
        )
        val request = CollectionUpdateRequest(
            collectionId = collection.requireId(),
            ownerMemberId = member.requireId(),
            newInfo = newInfo
        )

        val result = collectionUpdater.updateInfo(request)

        assertAll(
            { assertEquals(longName, result.info.name) },
            { assertEquals(longDescription, result.info.description) }
        )
    }

    @Test
    fun `updatePrivacy - success - changes privacy to private`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), ownerName = member.nickname.value)

        val result =
            collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PRIVATE)

        assertAll(
            { assertEquals(collection.id, result.id) },
            { assertEquals(CollectionPrivacy.PRIVATE, result.privacy) },
            { assertEquals(member.id, result.owner.memberId) },
            { assertEquals(member.nickname.value, result.owner.nickname) },
        )
    }

    @Test
    fun `updatePrivacy - success - changes privacy to public`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(
            member.requireId(),
            privacy = CollectionPrivacy.PRIVATE,
            ownerName = member.nickname.value
        )

        val result =
            collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PUBLIC)

        assertAll(
            { assertEquals(collection.id, result.id) },
            { assertEquals(CollectionPrivacy.PUBLIC, result.privacy) },
            { assertEquals(member.id, result.owner.memberId) },
            { assertEquals(member.nickname.value, result.owner.nickname) }
        )
    }

    @Test
    fun `updatePrivacy - success - persists privacy change to database`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), ownerName = member.nickname.value)

        collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PRIVATE)

        val updated = collectionRepository.findById(collection.requireId())
        assertAll(
            { assertNotNull(updated) },
            { assertEquals(CollectionPrivacy.PRIVATE, updated?.privacy) },
            { assertEquals(member.nickname.value, updated?.owner?.nickname) }
        )
    }

    @Test
    fun `updatePrivacy - failure - throws exception when collection not found`() {
        val member = testMemberHelper.createActivatedMember()

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updatePrivacy(999L, member.requireId(), CollectionPrivacy.PRIVATE)
        }.let {
            assertAll(
                { assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다.", it.message) },
                { assertTrue(it.cause is IllegalArgumentException) }
            )
        }
    }

    @Test
    fun `updatePrivacy - failure - throws exception when not owner`() {
        val owner = testMemberHelper.createActivatedMember()
        val other = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "다른사람"
        )
        val collection = createTestCollection(owner.requireId())

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updatePrivacy(collection.requireId(), other.requireId(), CollectionPrivacy.PRIVATE)
        }.let {
            assertAll(
                { assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다.", it.message) },
                { assertTrue(it.cause is IllegalArgumentException) }
            )
        }
    }

    @Test
    fun `updatePrivacy - failure - does not persist change when update fails`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val originalPrivacy = collection.privacy

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updatePrivacy(999L, member.requireId(), CollectionPrivacy.PRIVATE) // 존재하지 않는 ID
        }

        val unchanged = collectionRepository.findById(collection.requireId())
        assertEquals(originalPrivacy, unchanged?.privacy)
    }

    @Test
    fun `updatePrivacy - success - maintains other collection properties`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        val originalInfo = collection.info
        val originalPostCount = collection.posts.postIds.size
        val originalCreatedAt = collection.createdAt

        val result =
            collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PRIVATE)

        assertAll(
            { assertEquals(originalInfo.name, result.info.name) },
            { assertEquals(originalInfo.description, result.info.description) },
            { assertEquals(originalInfo.coverImageUrl, result.info.coverImageUrl) },
            { assertEquals(originalPostCount, result.posts.postIds.size) },
            { assertEquals(originalCreatedAt, result.createdAt) },
            { assertEquals(member.id, result.owner.memberId) }
        )
    }

    @Test
    fun `updatePrivacy - success - handles same privacy setting`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId(), privacy = CollectionPrivacy.PUBLIC)

        val result =
            collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PUBLIC)

        assertAll(
            { assertEquals(CollectionPrivacy.PUBLIC, result.privacy) },
        )
    }

    @Test
    fun `updatePrivacy - failure - throws exception when collection is deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val collection = createTestCollection(member.requireId())
        collection.delete(member.requireId()) // 컬렉션 삭제

        assertFailsWith<CollectionUpdateException> {
            collectionUpdater.updatePrivacy(collection.requireId(), member.requireId(), CollectionPrivacy.PRIVATE)
        }.let {
            assertAll(
                { assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다.", it.message) },
                { assertTrue(it.cause is IllegalArgumentException) }
            )
        }
    }

    private fun createTestCollection(
        ownerMemberId: Long,
        name: String = "테스트 컬렉션",
        description: String = "테스트용 컬렉션입니다",
        coverImageUrl: String? = "https://example.com/test.jpg",
        privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
        ownerName: String = "test",
    ) = collectionRepository.save(
        PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = ownerMemberId,
                name = name,
                description = description,
                coverImageUrl = coverImageUrl,
                privacy = privacy,
                ownerName = ownerName
            )
        )
    )
}
