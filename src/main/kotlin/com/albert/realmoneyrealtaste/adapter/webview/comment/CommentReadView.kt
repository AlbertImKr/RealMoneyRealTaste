package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.application.comment.provided.CommentReader
import com.albert.realmoneyrealtaste.domain.comment.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CommentReadView(
    private val commentReader: CommentReader,
) {

    @GetMapping("/comments/fragments/list")
    fun getCommentsFragment(
        @RequestParam postId: Long,
        @PageableDefault pageable: Pageable,
        model: Model,
    ): String {
        val comments: Page<Comment> = commentReader.getComments(postId, pageable)

        model.addAttribute("comments", comments)
        model.addAttribute("postId", postId)

        return "comment/comments-fragment :: comments-list"
    }

    @GetMapping("/comments/{commentId}/replies/fragments/list")
    fun getRepliesFragment(
        @PathVariable commentId: Long,
        @PageableDefault pageable: Pageable,
        model: Model,
    ): String {
        val replies: Page<Comment> = commentReader.getReplies(commentId, pageable)

        model.addAttribute("replies", replies)
        model.addAttribute("commentId", commentId)

        return "comment/replies-fragment :: replies-list"
    }

    @GetMapping("/comments/modal-fragments/list")
    fun getModalCommentsFragment(
        @RequestParam postId: Long,
        @PageableDefault pageable: Pageable,
        model: Model,
    ): String {
        val comments: Page<Comment> = commentReader.getComments(postId, pageable)

        model.addAttribute("comments", comments)
        model.addAttribute("postId", postId)

        return "comment/modal-comments :: modal-comments-list"
    }

    @GetMapping("/comment/modal-replies-fragment")
    fun getModalRepliesFragment(
        @RequestParam commentId: Long,
        @PageableDefault pageable: Pageable,
        model: Model,
    ): String {
        val replies: Page<Comment> = commentReader.getReplies(commentId, pageable)

        model.addAttribute("replies", replies)
        model.addAttribute("commentId", commentId)

        return "comment/modal-comments :: modal-replies-list"
    }
}
