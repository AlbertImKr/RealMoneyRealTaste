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
            privacy = CollectionPrivacy.PUBLIC
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
            description = ""
        )

        assertAll(
            { assertEquals(1L, command.ownerMemberId) },
            { assertEquals("컬렉션", command.name) },
            { assertEquals("", command.description) },
            { assertEquals(null, command.coverImageUrl) },
            { assertEquals(CollectionPrivacy.PUBLIC, command.privacy) }
        )
    }

    @Test
    fun `create - success - creates command with private privacy`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "비공개 컬렉션",
            description = "개인적인 컬렉션",
            privacy = CollectionPrivacy.PRIVATE
        )

        assertEquals(CollectionPrivacy.PRIVATE, command.privacy)
    }

    @Test
    fun `create - failure - throws exception when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionCreateCommand(
                ownerMemberId = 1L,
                name = "",
                description = "설명"
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
                description = "설명"
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
                description = "설명"
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
            description = "설명"
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
                description = longDescription
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
            description = maxLengthDescription
        )

        assertEquals(CollectionInfo.MAX_DESCRIPTION_LENGTH, command.description.length)
    }

    @Test
    fun `create - success - accepts empty description`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = ""
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
                coverImageUrl = ""
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
                coverImageUrl = "   "
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
                coverImageUrl = longUrl
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
            coverImageUrl = maxLengthUrl
        )

        assertEquals(CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH, command.coverImageUrl!!.length)
    }

    @Test
    fun `create - success - accepts null cover image url`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명",
            coverImageUrl = null
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
            coverImageUrl = validUrl
        )

        assertEquals(validUrl, command.coverImageUrl)
    }

    @Test
    fun `create - success - uses default privacy when not specified`() {
        val command = CollectionCreateCommand(
            ownerMemberId = 1L,
            name = "컬렉션",
            description = "설명"
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
                privacy = privacy
            )

            assertEquals(privacy, command.privacy)
        }
    }
}
