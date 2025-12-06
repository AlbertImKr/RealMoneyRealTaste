package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import kotlin.random.Random

/**
 * 테스트용 BaseEntity ID 설정 확장 함수
 */
fun BaseEntity.setId(id: Long = Random.nextLong()) {
    val idField = BaseEntity::class.java.getDeclaredField("id")
    idField.isAccessible = true
    idField.set(this, id)
}

/**
 * 체이닝을 위한 확장 함수
 */
fun <T : BaseEntity> T.withId(id: Long): T {
    this.setId(id)
    return this
}
