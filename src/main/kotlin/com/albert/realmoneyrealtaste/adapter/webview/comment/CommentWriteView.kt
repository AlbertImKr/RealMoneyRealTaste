package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest
import com.albert.realmoneyrealtaste.application.comment.provided.CommentCreator
import com.albert.realmoneyrealtaste.application.comment.provided.CommentUpdater
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CommentWriteView(
    private val commentCreator: CommentCreator,
    private val commentUpdater: CommentUpdater,
) {

    @PostMapping("/comments")
    fun createComment(
        @RequestParam postId: Long,
        @RequestParam content: String,
        @RequestParam(required = false) parentCommentId: Long?,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {

        val comment = when {
            // 답글 작성인 경우
            parentCommentId != null -> {
                commentCreator.createReply(
                    ReplyCreateRequest(
                        parentCommentId = parentCommentId,
                        content = content,
                        memberId = memberPrincipal.memberId,
                        postId = postId
                    )
                )
            }

            // 일반 댓글 작성인 경우
            else -> {
                commentCreator.createComment(
                    CommentCreateRequest(
                        postId = postId,
                        content = content,
                        memberId = memberPrincipal.memberId
                    )
                )
            }
        }

        model.addAttribute("comment", comment)

        // 답글과 일반 댓글에 따라 다른 프래그먼트 반환
        return if (parentCommentId != null) {
            "comment/replies-fragment :: comments-list"
        } else {
            "comment/comments-fragment :: replies-list"
        }
    }

    @PostMapping("/comments/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestParam content: String,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        val updatedComment = commentUpdater.updateComment(
            CommentUpdateRequest(
                commentId = commentId,
                content = content,
                memberId = memberPrincipal.memberId
            )
        )

        model.addAttribute("comment", updatedComment)

        // 수정된 댓글이 답글인지 일반 댓글인지에 따라 다른 프래그먼트 반환
        return if (updatedComment.isReply()) {
            "comment/replies-fragment :: reply-item"
        } else {
            "comment/comments-fragment :: comment-item"
        }
    }
}
