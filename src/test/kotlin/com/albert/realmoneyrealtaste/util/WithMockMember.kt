package com.albert.realmoneyrealtaste.util

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@WithSecurityContext(factory = WithMockMemberSecurityContextFactory::class)
annotation class WithMockMember(
    val memberId: Long = 1L,
    val email: String = "test@example.com",
    val nickname: String = "테스트",
    val introduction: String = "안녕하세요. 테스트입니다.",
    val active: Boolean = true,
    val roles: Array<String> = ["USER"],
)
