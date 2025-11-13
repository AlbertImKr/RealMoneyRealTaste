package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateException
import com.albert.realmoneyrealtaste.domain.comment.Comment

fun interface CommentUpdater {

    /**
     * 댓글 내용을 수정합니다.
     *
     * @param request 댓글 수정 요청 정보
     * @return 수정된 댓글
     * @throws CommentUpdateException 댓글 수정에 실패한 경우
     */
    fun updateComment(request: CommentUpdateRequest): Comment
}
