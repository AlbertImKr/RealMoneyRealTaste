package com.albert.realmoneyrealtaste.domain.comment

/**
 * 댓글 상태
 */
enum class CommentStatus {
    /**
     * 공개 상태
     */
    PUBLISHED,

    /**
     * 삭제 상태 (Soft Delete)
     */
    DELETED
}
