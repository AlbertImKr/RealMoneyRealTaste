package com.albert.realmoneyrealtaste.domain.event

/**
 * 회원 이벤트 타입 enum
 */
enum class MemberEventType {
    // 친구 관련
    FRIEND_REQUEST_SENT,      // 친구 요청을 보냈습니다
    FRIEND_REQUEST_RECEIVED,  // 친구 요청을 받았습니다
    FRIEND_REQUEST_ACCEPTED,  // 친구 요청이 수락되었습니다
    FRIEND_REQUEST_REJECTED,  // 친구 요청이 거절되었습니다
    FRIENDSHIP_TERMINATED,    // 친구 관계가 해제되었습니다

    // 게시물 관련
    POST_CREATED,             // 새 게시물을 작성했습니다
    POST_DELETED,             // 게시물을 삭제했습니다
    POST_COMMENTED,           // 게시물에 댓글이 달렸습니다

    // 댓글 관련
    COMMENT_CREATED,          // 댓글을 작성했습니다
    COMMENT_DELETED,          // 댓글을 삭제했습니다
    COMMENT_REPLIED,          // 대댓글이 달렸습니다

    // 프로필 관련
    PROFILE_UPDATED,          // 프로필이 업데이트되었습니다

    // 시스템 관련
    ACCOUNT_ACTIVATED,        // 계정이 활성화되었습니다
    ACCOUNT_DEACTIVATED,      // 계정이 비활성화되었습니다
}
