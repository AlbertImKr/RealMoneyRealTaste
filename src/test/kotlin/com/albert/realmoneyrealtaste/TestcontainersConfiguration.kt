package com.albert.realmoneyrealtaste

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        return MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .apply {
                withDatabaseName("testdb")
                withUsername("testuser")
                withPassword("testpass")
                withReuse(true)
                withFileSystemBind(
                    "/tmp/mysql-data",
                    "/var/lib/mysql",
                    org.testcontainers.containers.BindMode.READ_WRITE
                )
            }
    }
}
