package com.albert.realmoneyrealtaste.domain.comment.event

import com.albert.realmoneyrealtaste.domain.common.DomainEvent

interface CommentDomainEvent : DomainEvent {
    val commentId: Long

    fun withCommentId(commentId: Long): CommentDomainEvent
}
