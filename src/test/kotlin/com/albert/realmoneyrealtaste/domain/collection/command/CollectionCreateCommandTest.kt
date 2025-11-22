package com.albert.realmoneyrealtaste.domain.collection.command

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionCreateCommandTest {

    @Test
    fun `create - success - creates command with valid parameters`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "맛집 컬렉션",
            description = "내가 좋아하는 맛집들",
            coverImageUrl = "https://example.com/cover.jpg",
            privacy = CollectionPrivacy.PUBLIC,
            ownerName = "test"
        )

        assertAll(
            { assertEquals(1L, command.ownerMemberId) },
            { assertEquals("맛집 컬렉션", command.name) },
            { assertEquals("내가 좋아하는 맛집들", command.description) },
            { assertEquals("https://example.com/cover.jpg", command.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PUBLIC, command.privacy) }
        )
    }

    @Test
    fun `create - success - creates command with minimal parameters`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "",
            ownerName = "test"
        )

        assertAll(
            { assertEquals(1L, command.ownerMemberId) },
            { assertEquals("컬렉션", command.name) },
            { assertEquals("", command.description) },
            { assertEquals(null, command.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PUBLIC, command.privacy) },
            { assertEquals("test", command.ownerName) }
        )
    }

    @Test
    fun `create - failure - throws exception when ownerMemberId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 0L,
                name = "컬렉션",
                description = "설명",
                ownerName = "test"
            )
        }.let {
            assertEquals("소유자 회원 ID는 양수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when ownerMemberId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = -1L,
                name = "컬렉션",
                description = "설명",
                ownerName = "test"
            )
        }.let {
            assertEquals("소유자 회원 ID는 양수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when ownerName is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = "설명",
                ownerName = ""
            )
        }.let {
            assertEquals("소유자 닉네임은 비어있을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - success - accepts valid ownerName with various characters`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            ownerName = "테스트유저123"
        )

        assertEquals("테스트유저123", command.ownerName)
    }

    @Test
    fun `create - success - accepts ownerName with Korean characters`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            ownerName = "맛집탐험가"
        )

        assertEquals("맛집탐험가", command.ownerName)
    }

    @Test
    fun `create - success - accepts maximum positive ownerMemberId`() {
        val command = CollectionCreateCommand(
            ownerMemberId = Long.MAX_VALUE,
            name = "컬렉션",
            description = "설명",
            ownerName = "test"
        )

        assertEquals(Long.MAX_VALUE, command.ownerMemberId)
    }

    @Test
    fun `create - success - creates command with private privacy`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "비공개 컬렉션",
            description = "개인적인 컬렉션",
            privacy = CollectionPrivacy.PRIVATE,
            ownerName = "test"
        )

        assertEquals(CollectionPrivacy.PRIVATE, command.privacy)
    }

    @Test
    fun `create - failure - throws exception when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "",
                description = "설명",
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_NAME_BLANK, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when name is whitespace only`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "   ",
                description = "설명",
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_NAME_BLANK, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when name exceeds max length`() {
        val longName = "a".repeat(CollectionInfo.MAX_NAME_LENGTH + 1)

        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = longName,
                description = "설명",
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_NAME_LENGTH_EXCEEDED, it.message)
        }
    }

    @Test
    fun `create - success - accepts name at max length`() {
        val maxLengthName = "a".repeat(CollectionInfo.MAX_NAME_LENGTH)

        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = maxLengthName,
            description = "설명",
            ownerName = "test"
        )

        assertEquals(CollectionInfo.MAX_NAME_LENGTH, command.name.length)
    }

    @Test
    fun `create - failure - throws exception when description exceeds max length`() {
        val longDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH + 1)

        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = longDescription,
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_DESCRIPTION_LENGTH_EXCEEDED, it.message)
        }
    }

    @Test
    fun `create - success - accepts description at max length`() {
        val maxLengthDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH)

        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = maxLengthDescription,
            ownerName = "test"
        )

        assertEquals(CollectionInfo.MAX_DESCRIPTION_LENGTH, command.description.length)
    }

    @Test
    fun `create - success - accepts empty description`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "",
            ownerName = "test"
        )

        assertEquals("", command.description)
    }

    @Test
    fun `create - failure - throws exception when cover image url is blank`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = "설명",
                coverImageUrl = "",
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_COVER_IMAGE_URL_BLANK, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when cover image url is whitespace only`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = "설명",
                coverImageUrl = "   ",
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_COVER_IMAGE_URL_BLANK, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when cover image url exceeds max length`() {
        val longUrl = "https://example.com/" + "a".repeat(CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH)

        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = "설명",
                coverImageUrl = longUrl,
                ownerName = "test"
            )
        }.let {
            assertEquals(CollectionInfo.ERROR_COVER_IMAGE_URL_LENGTH_EXCEEDED, it.message)
        }
    }

    @Test
    fun `create - success - accepts cover image url at max length`() {
        val maxLengthUrl = "a".repeat(CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH)

        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            coverImageUrl = maxLengthUrl,
            ownerName = "test"
        )

        assertEquals(CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH, command.coverImageUrl!!.length)
    }

    @Test
    fun `create - success - accepts null cover image url`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            coverImageUrl = null,
            ownerName = "test"
        )

        assertEquals(null, command.coverImageUrl)
    }

    @Test
    fun `create - success - accepts valid cover image url`() {
        val validUrl = "https://example.com/image.jpg"

        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            coverImageUrl = validUrl,
            ownerName = "test"
        )

        assertEquals(validUrl, command.coverImageUrl)
    }

    @Test
    fun `create - success - uses default privacy when not specified`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            ownerName = "test"
        )

        assertEquals(CollectionPrivacy.PUBLIC, command.privacy)
    }

    @Test
    fun `create - success - accepts all privacy values`() {
        CollectionPrivacy.entries.forEach { privacy ->
            val command = CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "컬렉션",
                description = "설명",
                privacy = privacy,
                ownerName = "test"
            )

            assertEquals(privacy, command.privacy)
        }
    }
}
