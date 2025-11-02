package com.albert.realmoneyrealtaste.application.comment.exception

/**
 * 댓글 생성 요청 예외
 */
sealed class CommentCreateRequestException(message: String) : CommentApplicationException(message) {
    class InvalidPostIdException(message: String) : CommentCreateRequestException(message)

    class InvalidMemberIdException(message: String) : CommentCreateRequestException(message)

    class EmptyContentException(message: String) : CommentCreateRequestException(message)
}
