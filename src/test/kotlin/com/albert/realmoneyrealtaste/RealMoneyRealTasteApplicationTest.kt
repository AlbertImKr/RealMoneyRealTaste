package com.albert.realmoneyrealtaste

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.springframework.context.annotation.Import
import kotlin.test.Test

@Import(TestcontainersConfiguration::class)
class RealMoneyRealTasteApplicationTest {

    @Test
    fun `run application context`() {
        val args = arrayOf("--spring.profiles.active=test")

        assertDoesNotThrow {
            main(args)
        }
    }
}
