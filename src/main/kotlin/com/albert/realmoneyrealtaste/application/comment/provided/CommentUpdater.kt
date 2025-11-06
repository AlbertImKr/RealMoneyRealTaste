package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.exceptions.UnauthorizedCommentOperationException

fun interface CommentUpdater {

    /**
     * 댓글 내용을 수정합니다.
     *
     * @param request 댓글 수정 요청 정보
     * @return 수정된 댓글
     * @throws UnauthorizedCommentOperationException 댓글 수정 권한이 없는 경우
     */
    fun updateComment(request: CommentUpdateRequest): Comment
}
