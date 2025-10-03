package com.albert.realmoneyrealtaste

import com.albert.realmoneyrealtaste.config.TestConfig
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.springframework.context.annotation.Import
import kotlin.test.Test

@Import(TestcontainersConfiguration::class, TestConfig::class)
class RealMoneyRealTasteApplicationTest {

    @Test
    fun `run application context`() {
        val args = arrayOf("--spring.profiles.active=dev")

        assertDoesNotThrow {
            main(args)
        }
    }
}
