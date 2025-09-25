package com.albert.realmoneyrealtaste.domain.member

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NicknameTest {

    @Test
    fun `test valid nickname`() {
        val nickname = Nickname("ValidName")

        assertThat(nickname.value).isEqualTo("ValidName")
    }

    @Test
    fun `test invalid nickname - empty`() {
        assertThatThrownBy { Nickname("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("닉네임은 필수입니다")
    }

    @Test
    fun `test invalid nickname - too short`() {
        assertThatThrownBy { Nickname("A") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("닉네임은 2-20자 사이여야 합니다")
    }

    @Test
    fun `test invalid nickname - too long`() {
        assertThatThrownBy { Nickname("A".repeat(21)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("닉네임은 2-20자 사이여야 합니다")
    }

    @Test
    fun `test invalid nickname - special characters`() {
        assertThatThrownBy { Nickname("Invalid@Name!") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("닉네임은 한글, 영문, 숫자만 사용 가능합니다")
    }
}
