package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreationException
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.exceptions.CommentNotFoundException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentStatusException

/**
 * 댓글 생성 기능을 제공하는 인터페이스
 */
interface CommentCreator {

    /**
     * 새 댓글을 생성합니다.
     *
     * @param request 댓글 생성 요청 데이터
     * @return 생성된 댓글
     * @throws CommentCreationException 댓글 생성에 실패한 경우
     */
    fun createComment(request: CommentCreateRequest): Comment

    /**
     * 대댓글을 생성합니다.
     *
     * @param request 댓글 생성 요청 데이터 (parentCommentId 포함)
     * @return 생성된 대댓글
     * @throws CommentNotFoundException 부모 댓글을 찾을 수 없는 경우
     * @throws InvalidCommentStatusException 부모 댓글이 공개 상태가 아닌 경우
     * @throws CommentCreationException 대댓글 생성에 실패한 경우
     */
    fun createReply(request: ReplyCreateRequest): Comment
}
