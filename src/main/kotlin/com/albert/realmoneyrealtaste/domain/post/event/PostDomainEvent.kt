package com.albert.realmoneyrealtaste.domain.post.event

import com.albert.realmoneyrealtaste.domain.common.DomainEvent

interface PostDomainEvent : DomainEvent {
    val postId: Long

    fun withPostId(postId: Long): PostDomainEvent
}
