package com.albert.realmoneyrealtaste.application.comment.exception

sealed class ReplyCreateRequestException(message: String) : CommentApplicationException(message) {
    class InvalidParentCommentIdException(message: String) : ReplyCreateRequestException(message)

    class InvalidPostIdException(message: String) : ReplyCreateRequestException(message)

    class InvalidMemberIdException(message: String) : ReplyCreateRequestException(message)

    class EmptyContentException(message: String) : ReplyCreateRequestException(message)
}
