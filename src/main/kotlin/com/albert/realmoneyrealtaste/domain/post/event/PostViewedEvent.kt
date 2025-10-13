package com.albert.realmoneyrealtaste.domain.post.event

data class PostViewedEvent(val postId: Long, val viewerMemberId: Long, val authorMemberId: Long)
