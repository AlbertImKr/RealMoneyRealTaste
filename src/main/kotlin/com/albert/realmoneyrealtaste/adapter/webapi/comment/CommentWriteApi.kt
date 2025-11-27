package com.albert.realmoneyrealtaste.adapter.webapi.comment

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest
import com.albert.realmoneyrealtaste.application.comment.provided.CommentCreator
import com.albert.realmoneyrealtaste.application.comment.provided.CommentUpdater
import com.albert.realmoneyrealtaste.domain.comment.Comment
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentWriteApi(
    private val commentCreator: CommentCreator,
    private val commentUpdater: CommentUpdater,
) {
    @PostMapping("/api/posts/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @RequestParam content: String,
        @RequestParam(required = false) parentCommentId: Long?,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
    ): ResponseEntity<Comment> {
        val comment = when {
            // 답글 작성인 경우
            parentCommentId != null -> {
                commentCreator.createReply(
                    ReplyCreateRequest(
                        parentCommentId = parentCommentId,
                        content = content,
                        memberId = memberPrincipal.id,
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
                        memberId = memberPrincipal.id
                    )
                )
            }
        }
        return ResponseEntity.ok(comment)
    }

    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    fun updateComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @RequestParam content: String,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
    ): ResponseEntity<Comment> {
        val updatedComment = commentUpdater.updateComment(
            CommentUpdateRequest(
                commentId = commentId,
                content = content,
                memberId = memberPrincipal.id
            )
        )
        return ResponseEntity.ok(updatedComment)
    }
}
