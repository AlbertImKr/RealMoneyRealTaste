package com.albert.realmoneyrealtaste.application.post.listener

import com.albert.realmoneyrealtaste.application.post.provided.PostUpdater
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartAddedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostHeartRemovedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostViewedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PostEventListener(
    private val postUpdater: PostUpdater,
) {

    @Async
    @EventListener
    fun handlePostHeartAdded(event: PostHeartAddedEvent) {
        postUpdater.incrementHeartCount(event.postId)
    }

    @Async
    @EventListener
    fun handlePostHeartRemoved(event: PostHeartRemovedEvent) {
        postUpdater.decrementHeartCount(event.postId)
    }

    @Async
    @EventListener
    fun handlePostViewed(event: PostViewedEvent) {
        postUpdater.incrementViewCount(event.postId)
    }
}
