package com.albert.realmoneyrealtaste.domain.comment.exceptions

/**
 * 잘못된 대댓글 참조 예외
 */
class InvalidParentCommentException(message: String) : CommentDomainException(message)
