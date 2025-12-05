package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.domain.event.MemberEvent

/**
 * 회원 이벤트 생성 기능을 제공하는 인터페이스
 */
interface MemberEventCreator {

    /**
     * 새로운 회원 이벤트를 생성합니다.
     *
     * @param memberId 회원 ID
     * @param eventType 이벤트 타입
     * @param title 이벤트 제목
     * @param message 이벤트 메시지
     * @param relatedMemberId 관련 회원 ID (선택사항)
     * @param relatedPostId 관련 게시물 ID (선택사항)
     * @param relatedCommentId 관련 댓글 ID (선택사항)
     * @return 생성된 회원 이벤트
     */
    fun createEvent(
        memberId: Long,
        eventType: com.albert.realmoneyrealtaste.domain.event.MemberEventType,
        title: String,
        message: String,
        relatedMemberId: Long? = null,
        relatedPostId: Long? = null,
        relatedCommentId: Long? = null,
    ): MemberEvent
}
