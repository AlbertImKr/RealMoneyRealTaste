package com.albert.realmoneyrealtaste.domain.post.exceptions

/**
 * 게시글 상태가 유효하지 않을 때 발생하는 예외
 */
class InvalidPostStatusException(message: String) : PostDomainException(message)
