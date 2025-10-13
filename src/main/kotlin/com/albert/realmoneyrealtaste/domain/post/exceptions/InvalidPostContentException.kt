package com.albert.realmoneyrealtaste.domain.post.exceptions

/**
 * 게시글 내용이 유효하지 않을 때 발생하는 예외
 */
class InvalidPostContentException(message: String) : PostDomainException(message)
