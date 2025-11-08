package com.albert.realmoneyrealtaste.domain.collection.value

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionInfoTest {

    @Test
    fun `create - success - with all valid parameters`() {
        val info = CollectionInfo(
            name = "맛집 컬렉션",
            description = "내가 좋아하는 맛집들",
            coverImageUrl = "https://example.com/cover.jpg"
        )

        assertEquals("맛집 컬렉션", info.name)
        assertEquals("내가 좋아하는 맛집들", info.description)
        assertEquals("https://example.com/cover.jpg", info.coverImageUrl)
    }

    @Test
    fun `create - success - without cover image`() {
        val info = CollectionInfo(
            name = "맛집 컬렉션",
            description = "내가 좋아하는 맛집들",
            coverImageUrl = null
        )

        assertEquals("맛집 컬렉션", info.name)
        assertEquals("내가 좋아하는 맛집들", info.description)
        assertEquals(null, info.coverImageUrl)
    }

    @Test
    fun `create - success - with empty description`() {
        val info = CollectionInfo(
            name = "맛집 컬렉션",
            description = "",
            coverImageUrl = null
        )

        assertEquals("맛집 컬렉션", info.name)
        assertEquals("", info.description)
    }

    @Test
    fun `create - success - name exactly at max length`() {
        val exactMaxName = "a".repeat(CollectionInfo.MAX_NAME_LENGTH)

        val info = CollectionInfo(exactMaxName, "설명", null)

        assertEquals(exactMaxName, info.name)
    }

    @Test
    fun `create - success - description exactly at max length`() {
        val exactMaxDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH)

        val info = CollectionInfo("이름", exactMaxDescription, null)

        assertEquals(exactMaxDescription, info.description)
    }

    @Test
    fun `create - success - cover image url exactly at max length`() {
        val exactMaxUrl = "https://example.com/" + "a".repeat(
            CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH - "https://example.com/".length
        )

        val info = CollectionInfo("이름", "설명", exactMaxUrl)

        assertEquals(exactMaxUrl, info.coverImageUrl)
    }

    @Test
    fun `create - failure - empty name`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("", "설명", null)
        }.let {
            assertEquals("컬렉션 이름은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - blank name with spaces`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("   ", "설명", null)
        }.let {
            assertEquals("컬렉션 이름은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - blank name with tabs and newlines`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("\t\n  ", "설명", null)
        }.let {
            assertEquals("컬렉션 이름은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - name exceeds max length by one`() {
        val longName = "a".repeat(CollectionInfo.MAX_NAME_LENGTH + 1)

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo(longName, "설명", null)
        }.let {
            assertEquals("컬렉션 이름은 ${CollectionInfo.MAX_NAME_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - name much longer than max length`() {
        val veryLongName = "a".repeat(CollectionInfo.MAX_NAME_LENGTH * 2)

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo(veryLongName, "설명", null)
        }.let {
            assertEquals("컬렉션 이름은 ${CollectionInfo.MAX_NAME_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - description exceeds max length by one`() {
        val longDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH + 1)

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", longDescription, null)
        }.let {
            assertEquals("컬렉션 설명은 ${CollectionInfo.MAX_DESCRIPTION_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - description much longer than max length`() {
        val veryLongDescription = "a".repeat(CollectionInfo.MAX_DESCRIPTION_LENGTH * 2)

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", veryLongDescription, null)
        }.let {
            assertEquals("컬렉션 설명은 ${CollectionInfo.MAX_DESCRIPTION_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - empty cover image url`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", "설명", "")
        }.let {
            assertEquals("커버 이미지 URL은 빈 값일 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - blank cover image url with spaces`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", "설명", "   ")
        }.let {
            assertEquals("커버 이미지 URL은 빈 값일 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - blank cover image url with tabs and newlines`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", "설명", "\t\n  ")
        }.let {
            assertEquals("커버 이미지 URL은 빈 값일 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - cover image url exceeds max length by one`() {
        val longUrl = "https://example.com/" + "a".repeat(
            CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH - "https://example.com/".length + 1
        )

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", "설명", longUrl)
        }.let {
            assertEquals("커버 이미지 URL은 ${CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - cover image url much longer than max length`() {
        val veryLongUrl = "https://example.com/" + "a".repeat(CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH * 2)

        assertFailsWith<IllegalArgumentException> {
            CollectionInfo("이름", "설명", veryLongUrl)
        }.let {
            assertEquals("커버 이미지 URL은 ${CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH}자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - success - with korean characters in name`() {
        val koreanName = "한글로 된 컬렉션 이름"

        val info = CollectionInfo(koreanName, "설명", null)

        assertEquals(koreanName, info.name)
    }

    @Test
    fun `create - success - with korean characters in description`() {
        val koreanDescription = "한글로 된 설명입니다. 여러 줄로 작성할 수도 있고, 특수문자(!@#$%^&*)도 포함될 수 있습니다."

        val info = CollectionInfo("이름", koreanDescription, null)

        assertEquals(koreanDescription, info.description)
    }

    @Test
    fun `create - success - with special characters in url`() {
        val urlWithSpecialChars = "https://example.com/image.jpg?param1=value1&param2=value2#fragment"

        val info = CollectionInfo("이름", "설명", urlWithSpecialChars)

        assertEquals(urlWithSpecialChars, info.coverImageUrl)
    }

    @Test
    fun `create - success - with unicode characters`() {
        val unicodeName = "🍕 Pizza Collection 🍔"
        val unicodeDescription = "Emoji와 함께하는 맛집 컬렉션 ✨"

        val info = CollectionInfo(unicodeName, unicodeDescription, null)

        assertEquals(unicodeName, info.name)
        assertEquals(unicodeDescription, info.description)
    }

    @Test
    fun `constants - verify expected values`() {
        assertEquals(100, CollectionInfo.MAX_NAME_LENGTH)
        assertEquals(1000, CollectionInfo.MAX_DESCRIPTION_LENGTH)
        assertEquals(500, CollectionInfo.MAX_COVER_IMAGE_URL_LENGTH)
    }
}
