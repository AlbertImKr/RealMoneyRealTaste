package com.albert.realmoneyrealtaste.domain.post.exceptions

/**
 * 게시글에 대한 권한이 없을 때 발생하는 예외
 */
class UnauthorizedPostOperationException(message: String) : PostDomainException(message)
