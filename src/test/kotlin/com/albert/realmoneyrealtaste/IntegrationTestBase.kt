package com.albert.realmoneyrealtaste

import com.albert.realmoneyrealtaste.config.TestConfig
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import kotlin.test.BeforeTest

@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestcontainersConfiguration::class, TestConfig::class)
@AutoConfigureMockMvc
abstract class IntegrationTestBase() {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @BeforeTest
    fun setUp() {
        clearAllTables()
    }

    protected fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun clearAllTables() {
        entityManager.flush()

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate()

        val tables = entityManager.createNativeQuery(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()"
        ).resultList as List<String>

        tables.forEach { tableName ->
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate()

        entityManager.clear()
    }
}
