package com.albert.realmoneyrealtaste.domain.comment.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 잘못된 댓글 내용 예외
 */
class InvalidCommentContentException(message: String) : ValueObjectValidationException(message)
