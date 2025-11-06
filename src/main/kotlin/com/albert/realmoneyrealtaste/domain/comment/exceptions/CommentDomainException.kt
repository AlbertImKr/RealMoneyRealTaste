package com.albert.realmoneyrealtaste.domain.comment.exceptions

/**
 * 댓글 도메인 관련 예외의 기본 클래스
 */
sealed class CommentDomainException(message: String) : RuntimeException(message)

