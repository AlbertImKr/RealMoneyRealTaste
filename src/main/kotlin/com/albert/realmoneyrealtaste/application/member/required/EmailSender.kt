package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Email

/**
 * 이메일 전송을 담당하는 인터페이스
 */
fun interface EmailSender {

    /**
     * 이메일을 전송하는 메서드
     *
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param content 이메일 본문 내용
     * @param isHtml 본문이 HTML 형식인지 여부
     */
    fun send(to: Email, subject: String, content: String, isHtml: Boolean)
}
