package com.albert.realmoneyrealtaste

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

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

                // GitHub Actions 환경 최적화
                withReuse(true)
                withStartupTimeout(Duration.ofMinutes(5)) // 시간 여유

                // 메모리 및 성능 최적화
                withCommand(
                    "--character-set-server=utf8mb4",
                    "--innodb-flush-log-at-trx-commit=0",
                    "--innodb-flush-method=O_DIRECT_NO_FSYNC",
                    "--innodb-buffer-pool-size=64M", // 메모리 절약
                    "--skip-log-bin",
                    "--innodb-doublewrite=0", // 성능 향상
                    "--sync-binlog=0"
                )

                // CI 환경에서는 파일시스템 바인드 제거 (권한 이슈 방지)
                if (!System.getenv("CI").isNullOrEmpty()) {
                    // CI 환경에서는 바인드 마운트 사용 안함
                } else {
                    withFileSystemBind(
                        "/tmp/mysql-data",
                        "/var/lib/mysql",
                        org.testcontainers.containers.BindMode.READ_WRITE
                    )
                }

                // 헬스체크 설정
                waitingFor(
                    Wait.forLogMessage(".*ready for connections.*", 2)
                        .withStartupTimeout(Duration.ofMinutes(3))
                )
            }
    }
}
