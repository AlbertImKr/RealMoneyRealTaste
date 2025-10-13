package com.albert.realmoneyrealtaste.domain.post.exceptions

/**
 * 게시글 도메인 관련 예외의 최상위 클래스
 */
sealed class PostDomainException(message: String) : RuntimeException(message)
