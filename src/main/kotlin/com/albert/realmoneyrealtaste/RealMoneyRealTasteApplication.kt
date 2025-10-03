package com.albert.realmoneyrealtaste

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class RealMoneyRealTasteApplication

fun main(args: Array<String>) {
    runApplication<RealMoneyRealTasteApplication>(*args)
}
