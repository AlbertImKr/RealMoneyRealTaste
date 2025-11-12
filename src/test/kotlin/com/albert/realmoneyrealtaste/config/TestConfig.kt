package com.albert.realmoneyrealtaste.config

import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.scheduling.annotation.AsyncConfigurer
import java.util.concurrent.Executor

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun emailSender(): EmailSender {
        return TestEmailSender()
    }

    @Bean
    @Primary
    fun asyncConfigurer(): AsyncConfigurer {
        return object : AsyncConfigurer {
            override fun getAsyncExecutor(): Executor = SyncTaskExecutor()
        }
    }
}
