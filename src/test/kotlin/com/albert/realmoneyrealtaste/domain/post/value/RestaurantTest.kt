package com.albert.realmoneyrealtaste.domain.post.value

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidRestaurantInfoException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RestaurantTest {

    @Test
    fun `create - success - creates restaurant with valid parameters`() {
        val restaurant = Restaurant(
            name = "맛있는집",
            address = "서울시 강남구",
            latitude = 37.5665,
            longitude = 126.9780
        )

        assertEquals("맛있는집", restaurant.name)
        assertEquals("서울시 강남구", restaurant.address)
        assertEquals(37.5665, restaurant.latitude)
        assertEquals(126.9780, restaurant.longitude)
    }

    @Test
    fun `create - failure - throws exception when name is blank`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "",
                address = "서울시 강남구",
                latitude = 37.5665,
                longitude = 126.9780
            )
        }.let {
            assertEquals("맛집 이름은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when name exceeds max length`() {
        val longName = "a".repeat(101)

        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = longName,
                address = "서울시 강남구",
                latitude = 37.5665,
                longitude = 126.9780
            )
        }.let {
            assertEquals("맛집 이름은 100자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when address is blank`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = "",
                latitude = 37.5665,
                longitude = 126.9780
            )
        }.let {
            assertEquals("주소는 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when address exceeds max length`() {
        val longAddress = "a".repeat(256)

        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = longAddress,
                latitude = 37.5665,
                longitude = 126.9780
            )
        }.let {
            assertEquals("주소는 255자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when latitude is out of range`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = "서울시 강남구",
                latitude = 91.0,
                longitude = 126.9780
            )
        }.let {
            assertTrue(it.message!!.contains("위도는 -90에서 90 사이여야 합니다"))
        }
    }

    @Test
    fun `create - failure - throws exception when latitude is below range`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = "서울시 강남구",
                latitude = -91.0,
                longitude = 126.9780
            )
        }.let {
            assertTrue(it.message!!.contains("위도는 -90에서 90 사이여야 합니다"))
        }
    }

    @Test
    fun `create - failure - throws exception when longitude is out of range`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = "서울시 강남구",
                latitude = 37.5665,
                longitude = 181.0
            )
        }.let {
            assertTrue(it.message!!.contains("경도는 -180에서 180 사이여야 합니다"))
        }
    }

    @Test
    fun `create - failure - throws exception when longitude is below range`() {
        assertFailsWith<InvalidRestaurantInfoException> {
            Restaurant(
                name = "맛있는집",
                address = "서울시 강남구",
                latitude = 37.5665,
                longitude = -181.0
            )
        }.let {
            assertTrue(it.message!!.contains("경도는 -180에서 180 사이여야 합니다"))
        }
    }

    @Test
    fun `create - success - accepts boundary values for latitude`() {
        val restaurant1 = Restaurant("맛집", "주소", -90.0, 0.0)
        val restaurant2 = Restaurant("맛집", "주소", 90.0, 0.0)

        assertEquals(-90.0, restaurant1.latitude)
        assertEquals(90.0, restaurant2.latitude)
    }

    @Test
    fun `create - success - accepts boundary values for longitude`() {
        val restaurant1 = Restaurant("맛집", "주소", 0.0, -180.0)
        val restaurant2 = Restaurant("맛집", "주소", 0.0, 180.0)

        assertEquals(-180.0, restaurant1.longitude)
        assertEquals(180.0, restaurant2.longitude)
    }
}
