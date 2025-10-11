package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * Value Object 검증 예외의 기본 클래스
 */
sealed class ValueObjectValidationException(message: String) : RuntimeException(message)
