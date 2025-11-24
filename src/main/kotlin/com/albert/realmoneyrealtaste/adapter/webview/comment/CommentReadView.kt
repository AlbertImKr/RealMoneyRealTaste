package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.comment.provided.CommentReader
import com.albert.realmoneyrealtaste.domain.comment.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class CommentReadView(
    private val commentReader: CommentReader,
) {

    @GetMapping(CommentUrls.COMMENTS_FRAGMENTS_LIST)
    fun getCommentsFragment(
        @PathVariable postId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val comments: Page<Comment> = commentReader.getComments(postId, pageable)

        model.addAttribute("comments", comments)
        model.addAttribute("postId", postId)

        return CommentViews.COMMENTS_LIST_FRAGMENT
    }

    @GetMapping(CommentUrls.REPLIES_FRAGMENTS_LIST)
    fun getRepliesFragment(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PathVariable commentId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val replies: Page<Comment> = commentReader.getReplies(commentId, pageable)

        model.addAttribute("replies", replies)
        model.addAttribute("commentId", commentId)
        model.addAttribute("member", memberPrincipal)

        return CommentViews.REPLIES_LIST_FRAGMENT
    }

    @GetMapping(CommentUrls.MODAL_COMMENTS_FRAGMENTS_LIST)
    fun getModalCommentsFragment(
        @PathVariable postId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val comments: Page<Comment> = commentReader.getComments(postId, pageable)

        model.addAttribute("comments", comments)
        model.addAttribute("postId", postId)

        return CommentViews.MODAL_COMMENTS_LIST_FRAGMENT
    }

    @GetMapping(CommentUrls.MODAL_REPLIES_FRAGMENT)
    fun getModalRepliesFragment(
        @PathVariable commentId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val replies: Page<Comment> = commentReader.getReplies(commentId, pageable)

        model.addAttribute("replies", replies)
        model.addAttribute("commentId", commentId)

        return CommentViews.MODAL_REPLIES_FRAGMENT
    }
}
