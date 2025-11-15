package com.albert.realmoneyrealtaste.adapter.webapi.comment

import com.albert.realmoneyrealtaste.application.comment.provided.CommentReader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentApi(
    private val commentReader: CommentReader,
) {

    @GetMapping("/api/posts/{postId}/comments/count")
    fun getCommentCount(
        @PathVariable postId: Long,
    ): Long {
        return commentReader.getCommentCount(postId)
    }
}
