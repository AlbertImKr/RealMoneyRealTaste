package com.albert.realmoneyrealtaste.application.comment.exception

/**
 * 댓글 수정 요청 예외
 */
sealed class CommentUpdateRequestException(message: String) : CommentApplicationException(message) {
    class InvalidCommentIdException(message: String) : CommentUpdateRequestException(message)

    class EmptyContentException(message: String) : CommentUpdateRequestException(message)

    class ExceedContentLengthException(message: String) : CommentUpdateRequestException(message)
}
