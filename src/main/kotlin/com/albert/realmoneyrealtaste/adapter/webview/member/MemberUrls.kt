package com.albert.realmoneyrealtaste.adapter.webview.member

/**
 * Member 관련 URL 상수
 */
object MemberUrls {
    // ========== 프로필 관련 ==========
    const val PROFILE = "/members/{id}"

    // ========== 인증 관련 ==========
    const val ACTIVATION = "/members/activate"
    const val RESEND_ACTIVATION = "/members/resend-activation"
    const val PASSWORD_FORGOT = "/members/password-forgot"
    const val PASSWORD_RESET = "/members/password-reset"

    // ========== 설정 관련 ==========
    const val SETTING = "/members/setting"
    const val SETTING_ACCOUNT = "/members/setting/account"
    const val SETTING_PASSWORD = "/members/setting/password"
    const val SETTING_DELETE = "/members/setting/delete"

    // ========== 프래그먼트 관련 ==========
    const val FRAGMENT_SUGGEST_USERS_SIDEBAR = "/fragments/members/suggest-users-sidebar"
    const val FRAGMENT_MEMBER_PROFILE = "/fragments/member-profile"
}
