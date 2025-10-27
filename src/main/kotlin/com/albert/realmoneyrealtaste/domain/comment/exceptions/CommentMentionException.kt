package com.albert.realmoneyrealtaste.domain.comment.exceptions

sealed class CommentMentionException(message: String) : CommentDomainException(message) {
    class InvalidMemberIdException(message: String) : CommentMentionException(message)

    class EmptyNicknameException(message: String) : CommentMentionException(message)

    class OversizedNicknameException(message: String) : CommentMentionException(message)

    class InvalidStartPositionException(message: String) : CommentMentionException(message)

    class InvalidEndPositionException(message: String) : CommentMentionException(message)
}
