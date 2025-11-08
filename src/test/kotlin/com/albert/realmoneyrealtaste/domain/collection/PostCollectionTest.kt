package com.albert.realmoneyrealtaste.domain.collection

import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionOwner
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionPosts
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PostCollectionTest {

    @Test
    fun `create - success - creates collection with valid parameters`() {
        val collection = PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "맛집 모음",
                description = "내가 다녀온 맛집들",
                coverImageUrl = "https://example.com/cover.jpg",
                privacy = CollectionPrivacy.PUBLIC,
            )
        )

        assertAll(
            { assertEquals(1L, collection.owner.memberId) },
            { assertEquals("맛집 모음", collection.info.name) },
            { assertEquals("내가 다녀온 맛집들", collection.info.description) },
            { assertEquals("https://example.com/cover.jpg", collection.info.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PUBLIC, collection.privacy) },
            { assertEquals(CollectionStatus.ACTIVE, collection.status) },
            { assertTrue(collection.isEmpty()) },
            { assertEquals(0, collection.getPostCount()) },
            { assertNotNull(collection.createdAt) },
            { assertNotNull(collection.updatedAt) },
            { assertEquals(collection.createdAt, collection.updatedAt) }
        )
    }

    @Test
    fun `create - success - creates collection without cover image`() {
        val collection = PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "맛집 모음",
                description = "내가 다녀온 맛집들",
            )
        )

        assertAll(
            { assertEquals(null, collection.info.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PUBLIC, collection.privacy) }
        )
    }

    @Test
    fun `create - success - creates collection with private privacy`() {
        val collection = PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "맛집 모음",
                description = "내가 다녀온 맛집들",
                privacy = CollectionPrivacy.PRIVATE,
            )
        )

        assertAll(
            { assertEquals(CollectionPrivacy.PRIVATE, collection.privacy) },
            { assertTrue(collection.isPrivate()) },
            { assertFalse(collection.isPublic()) }
        )
    }

    @Test
    fun `updateInfo - success - updates collection info`() {
        val collection = createDefaultCollection()
        val newInfo = CollectionInfo(
            name = "새로운 맛집 컬렉션",
            description = "업데이트된 설명",
            coverImageUrl = "https://example.com/new.jpg"
        )
        val originalUpdatedAt = collection.updatedAt

        collection.updateInfo(1L, newInfo)

        assertAll(
            { assertEquals("새로운 맛집 컬렉션", collection.info.name) },
            { assertEquals("업데이트된 설명", collection.info.description) },
            { assertEquals("https://example.com/new.jpg", collection.info.coverImageUrl) },
            { assertTrue(collection.updatedAt.isAfter(originalUpdatedAt)) }
        )
    }

    @Test
    fun `updateInfo - failure - unauthorized user`() {
        val collection = createDefaultCollection()
        val newInfo = CollectionInfo("새 이름", "새 설명", null)

        assertFailsWith<IllegalArgumentException> {
            collection.updateInfo(2L, newInfo)
        }.let {
            assertEquals("컬렉션을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `updateInfo - failure - deleted collection`() {
        val collection = createDefaultCollection()
        collection.delete(1L)
        val newInfo = CollectionInfo("새 이름", "새 설명", null)

        assertFailsWith<IllegalArgumentException> {
            collection.updateInfo(1L, newInfo)
        }.let {
            assertEquals("컬렉션이 활성 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `updatePrivacy - success - changes privacy setting`() {
        val collection = createDefaultCollection()
        val originalUpdatedAt = collection.updatedAt

        collection.updatePrivacy(1L, CollectionPrivacy.PRIVATE)

        assertAll(
            { assertEquals(CollectionPrivacy.PRIVATE, collection.privacy) },
            { assertTrue(collection.isPrivate()) },
            { assertFalse(collection.isPublic()) },
            { assertTrue(collection.updatedAt.isAfter(originalUpdatedAt)) }
        )
    }

    @Test
    fun `updatePrivacy - failure - unauthorized user`() {
        val collection = createDefaultCollection()

        assertFailsWith<IllegalArgumentException> {
            collection.updatePrivacy(2L, CollectionPrivacy.PRIVATE)
        }.let {
            assertEquals("컬렉션을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `updatePrivacy - failure - deleted collection`() {
        val collection = createDefaultCollection()
        collection.delete(1L)

        assertFailsWith<IllegalArgumentException> {
            collection.updatePrivacy(1L, CollectionPrivacy.PRIVATE)
        }.let {
            assertEquals("컬렉션이 활성 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `addPost - success - adds post to collection`() {
        val collection = createDefaultCollection()
        val originalUpdatedAt = collection.updatedAt

        collection.addPost(1L, 100L)

        assertAll(
            { assertEquals(1, collection.getPostCount()) },
            { assertTrue(collection.posts.contains(100L)) },
            { assertFalse(collection.isEmpty()) },
            { assertTrue(collection.updatedAt.isAfter(originalUpdatedAt)) }
        )
    }

    @Test
    fun `addPost - success - adds multiple posts in order`() {
        val collection = createDefaultCollection()

        collection.addPost(1L, 100L)
        collection.addPost(1L, 200L)
        collection.addPost(1L, 300L)

        assertAll(
            { assertEquals(3, collection.getPostCount()) },
            { assertEquals(listOf(100L, 200L, 300L), collection.posts.postIds) }
        )
    }

    @Test
    fun `addPost - failure - duplicate post`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)

        assertFailsWith<IllegalArgumentException> {
            collection.addPost(1L, 100L)
        }.let {
            assertEquals("이미 컬렉션에 포함된 게시글입니다: 100", it.message)
        }
    }

    @Test
    fun `addPost - failure - unauthorized user`() {
        val collection = createDefaultCollection()

        assertFailsWith<IllegalArgumentException> {
            collection.addPost(2L, 100L)
        }.let {
            assertEquals("컬렉션을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `addPost - failure - deleted collection`() {
        val collection = createDefaultCollection()
        collection.delete(1L)

        assertFailsWith<IllegalArgumentException> {
            collection.addPost(1L, 100L)
        }.let {
            assertEquals("컬렉션이 활성 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `removePost - success - removes post from collection`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)
        collection.addPost(1L, 200L)
        collection.addPost(1L, 300L)
        val originalUpdatedAt = collection.updatedAt

        collection.removePost(1L, 200L)

        assertAll(
            { assertEquals(2, collection.getPostCount()) },
            { assertFalse(collection.posts.contains(200L)) },
            { assertTrue(collection.posts.contains(100L)) },
            { assertTrue(collection.posts.contains(300L)) },
            { assertEquals(listOf(100L, 300L), collection.posts.postIds) },
            { assertTrue(collection.updatedAt.isAfter(originalUpdatedAt)) }
        )
    }

    @Test
    fun `removePost - success - removes last post makes collection empty`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)

        collection.removePost(1L, 100L)

        assertAll(
            { assertTrue(collection.isEmpty()) },
            { assertEquals(0, collection.getPostCount()) }
        )
    }

    @Test
    fun `removePost - failure - post not found`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)

        assertFailsWith<IllegalArgumentException> {
            collection.removePost(1L, 200L)
        }.let {
            assertEquals("컬렉션에 존재하지 않는 게시글입니다: 200", it.message)
        }
    }

    @Test
    fun `removePost - failure - from empty collection`() {
        val collection = createDefaultCollection()

        assertFailsWith<IllegalArgumentException> {
            collection.removePost(1L, 100L)
        }.let {
            assertEquals("컬렉션에 존재하지 않는 게시글입니다: 100", it.message)
        }
    }

    @Test
    fun `removePost - failure - unauthorized user`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)

        assertFailsWith<IllegalArgumentException> {
            collection.removePost(2L, 100L)
        }.let {
            assertEquals("컬렉션을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `removePost - failure - deleted collection`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)
        collection.delete(1L)

        assertFailsWith<IllegalArgumentException> {
            collection.removePost(1L, 100L)
        }.let {
            assertEquals("컬렉션이 활성 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `delete - success - soft deletes collection`() {
        val collection = createDefaultCollection()
        collection.addPost(1L, 100L)
        val originalUpdatedAt = collection.updatedAt

        collection.delete(1L)

        assertAll(
            { assertEquals(CollectionStatus.DELETED, collection.status) },
            { assertTrue(collection.isDeleted()) },
            { assertTrue(collection.updatedAt.isAfter(originalUpdatedAt)) },
            { assertEquals(1, collection.getPostCount()) } // 게시글은 그대로 유지
        )
    }

    @Test
    fun `delete - failure - unauthorized user`() {
        val collection = createDefaultCollection()

        assertFailsWith<IllegalArgumentException> {
            collection.delete(2L)
        }.let {
            assertEquals("컬렉션을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `delete - failure - already deleted collection`() {
        val collection = createDefaultCollection()
        collection.delete(1L)

        assertFailsWith<IllegalArgumentException> {
            collection.delete(1L)
        }.let {
            assertEquals("컬렉션이 활성 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `canEditBy - returns correct permission`() {
        val collection = createDefaultCollection()

        assertAll(
            { assertTrue(collection.canEditBy(1L)) },   // 소유자
            { assertFalse(collection.canEditBy(2L)) },  // 다른 사용자
            { assertFalse(collection.canEditBy(0L)) }   // 잘못된 ID
        )
    }

    @Test
    fun `canViewBy - returns correct permission for public collection`() {
        val collection = createDefaultCollection()

        assertAll(
            { assertTrue(collection.canViewBy(null)) }, // 비로그인 사용자
            { assertTrue(collection.canViewBy(1L)) },   // 소유자
            { assertTrue(collection.canViewBy(2L)) },   // 다른 사용자
            { assertTrue(collection.canViewBy(0L)) }    // 잘못된 ID도 public이므로 조회 가능
        )
    }

    @Test
    fun `canViewBy - returns correct permission for private collection`() {
        val collection = createDefaultCollection()
        collection.updatePrivacy(1L, CollectionPrivacy.PRIVATE)

        assertAll(
            { assertFalse(collection.canViewBy(null)) }, // 비로그인 사용자
            { assertTrue(collection.canViewBy(1L)) },    // 소유자
            { assertFalse(collection.canViewBy(2L)) },   // 다른 사용자
            { assertFalse(collection.canViewBy(0L)) }    // 잘못된 ID
        )
    }

    @Test
    fun `canViewBy - returns false for deleted collection regardless of privacy`() {
        val collection = createDefaultCollection()
        collection.delete(1L)

        assertAll(
            { assertFalse(collection.canViewBy(null)) },
            { assertFalse(collection.canViewBy(1L)) },   // 소유자도 조회 불가
            { assertFalse(collection.canViewBy(2L)) }
        )
    }

    @Test
    fun `canViewBy - returns false for deleted private collection`() {
        val privateCollection = PostCollection.create(
            CollectionCreateCommand(
                ownerMemberId = 2L,
                name = "Private",
                description = "설명",
                privacy = CollectionPrivacy.PRIVATE,
            )
        )

        privateCollection.delete(2L)

        assertAll(
            { assertFalse(privateCollection.canViewBy(null)) },
            { assertFalse(privateCollection.canViewBy(2L)) },  // 소유자도 조회 불가
            { assertFalse(privateCollection.canViewBy(1L)) }
        )
    }

    @Test
    fun `status methods - return correct values`() {
        val collection = createDefaultCollection()

        // Active 상태 검증
        assertFalse(collection.isDeleted())

        // Deleted 상태로 변경 후 검증
        collection.delete(1L)
        assertTrue(collection.isDeleted())
    }

    @Test
    fun `privacy methods - return correct values`() {
        val collection = createDefaultCollection()

        // Public 상태 검증
        assertAll(
            { assertTrue(collection.isPublic()) },
            { assertFalse(collection.isPrivate()) }
        )

        // Private 상태로 변경 후 검증
        collection.updatePrivacy(1L, CollectionPrivacy.PRIVATE)
        assertAll(
            { assertFalse(collection.isPublic()) },
            { assertTrue(collection.isPrivate()) }
        )
    }

    @Test
    fun `collection state methods - return correct values`() {
        val collection = createDefaultCollection()

        // Empty collection 검증
        assertAll(
            { assertTrue(collection.isEmpty()) },
            { assertEquals(0, collection.getPostCount()) }
        )

        // With posts 검증
        collection.addPost(1L, 100L)
        collection.addPost(1L, 200L)
        assertAll(
            { assertFalse(collection.isEmpty()) },
            { assertEquals(2, collection.getPostCount()) }
        )
    }

    @Test
    fun `complex operations - maintains consistency`() {
        val collection = createDefaultCollection()

        // 여러 게시글 추가
        collection.addPost(1L, 100L)
        collection.addPost(1L, 200L)
        collection.addPost(1L, 300L)

        // 중간 게시글 제거
        collection.removePost(1L, 200L)

        // 정보 업데이트
        val newInfo = CollectionInfo("Updated Collection", "Updated Description", null)
        collection.updateInfo(1L, newInfo)

        // 공개 설정 변경
        collection.updatePrivacy(1L, CollectionPrivacy.PRIVATE)

        assertAll(
            { assertEquals(2, collection.getPostCount()) },
            { assertEquals(listOf(100L, 300L), collection.posts.postIds) },
            { assertEquals("Updated Collection", collection.info.name) },
            { assertEquals("Updated Description", collection.info.description) },
            { assertTrue(collection.isPrivate()) },
            { assertEquals(CollectionStatus.ACTIVE, collection.status) },
            { assertTrue(collection.canEditBy(1L)) },
            { assertFalse(collection.canViewBy(null)) }  // private이므로 비로그인 사용자는 조회 불가
        )
    }

    @Test
    fun `setters - success - for code coverage`() {
        val collection = TestCollection()
        val newOwner = CollectionOwner(2L)
        val newCreatedAt = LocalDateTime.of(2023, 1, 1, 0, 0)

        collection.setForCoverage(newOwner, newCreatedAt)

        assertAll(
            { assertEquals(2L, collection.owner.memberId) },
            { assertEquals(newCreatedAt, collection.createdAt) }
        )
    }

    private fun createDefaultCollection(): PostCollection {
        return PostCollection.create(createDefaultCollectionCommand())
    }

    private fun createDefaultCollectionCommand(
        ownerMemberId: Long = 1L,
        name: String = "테스트 컬렉션",
        description: String = "테스트용 컬렉션입니다",
        coverImageUrl: String? = null,
        privacy: CollectionPrivacy = CollectionPrivacy.PUBLIC,
    ): CollectionCreateCommand {
        return CollectionCreateCommand(
            ownerMemberId = ownerMemberId,
            name = name,
            description = description,
            coverImageUrl = coverImageUrl,
            privacy = privacy
        )
    }

    private class TestCollection : PostCollection(
        owner = CollectionOwner(1L),
        info = CollectionInfo("테스트", "설명", null),
        privacy = CollectionPrivacy.PUBLIC,
        status = CollectionStatus.ACTIVE,
        posts = CollectionPosts.empty(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    ) {
        fun setForCoverage(
            owner: CollectionOwner,
            createdAt: LocalDateTime,
        ) {
            this.owner = owner
            this.createdAt = createdAt
        }
    }
}
