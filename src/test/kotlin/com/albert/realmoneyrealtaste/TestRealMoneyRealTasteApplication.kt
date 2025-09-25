package com.albert.realmoneyrealtaste

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<RealMoneyRealTasteApplication>().with(TestcontainersConfiguration::class).run(*args)
}
