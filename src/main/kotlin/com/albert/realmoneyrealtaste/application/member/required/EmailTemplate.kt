package com.albert.realmoneyrealtaste.application.member.required

/**
 * 이메일 템플릿 생성을 담당하는 인터페이스
 */
fun interface EmailTemplate {

    /**
     * 회원 활성화 이메일 템플릿을 생성합니다.
     *
     * @param nickname 수신자의 닉네임
     * @param activationLink 활성화 링크
     * @param expirationHours 링크 만료 시간 (시간 단위)
     * @return 생성된 이메일 템플릿 문자열
     */
    fun buildActivationEmail(nickname: String, activationLink: String, expirationHours: Long): String
}
