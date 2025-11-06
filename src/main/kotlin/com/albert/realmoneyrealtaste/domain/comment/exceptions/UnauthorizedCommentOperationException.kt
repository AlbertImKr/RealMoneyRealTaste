package com.albert.realmoneyrealtaste.domain.comment.exceptions

/**
 * 댓글 권한 없음 예외
 */
class UnauthorizedCommentOperationException(message: String) : CommentDomainException(message)
