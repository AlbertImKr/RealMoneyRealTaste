package com.albert.realmoneyrealtaste.application.comment.exception

/**
 * 댓글을 찾을 수 없음 예외 (애플리케이션 레이어)
 */
class CommentNotFoundException(message: String) : CommentApplicationException(message)
