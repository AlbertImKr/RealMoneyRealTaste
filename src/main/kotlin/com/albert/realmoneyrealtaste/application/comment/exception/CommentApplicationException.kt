package com.albert.realmoneyrealtaste.application.comment.exception

/**
 * 댓글 애플리케이션 관련 예외의 기본 클래스
 */
sealed class CommentApplicationException(message: String) : RuntimeException(message)
