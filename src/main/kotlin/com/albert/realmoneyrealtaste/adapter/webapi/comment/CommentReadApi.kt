package com.albert.realmoneyrealtaste.adapter.webapi.comment

import com.albert.realmoneyrealtaste.application.comment.provided.CommentReader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentReadApi(
    private val commentReader: CommentReader,
) {

    @GetMapping("/api/comments/count")
    fun getCommentCount(@RequestParam postId: Long): Long {
        return commentReader.getCommentCount(postId)
    }
}
