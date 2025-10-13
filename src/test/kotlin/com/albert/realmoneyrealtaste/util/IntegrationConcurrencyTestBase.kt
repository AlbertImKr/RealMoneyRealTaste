package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.TestcontainersConfiguration
import com.albert.realmoneyrealtaste.config.TestConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestcontainersConfiguration::class, TestConfig::class)
abstract class IntegrationConcurrencyTestBase() {

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        clearAllTables()
    }

    @AfterEach
    fun tearDown() {
        clearAllTables()
    }

    private fun clearAllTables() {
        transactionTemplate.execute {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

            val tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String::class.java
            )

            tables.forEach { tableName ->
                jdbcTemplate.execute("TRUNCATE TABLE $tableName")
            }

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
        }
    }
}
