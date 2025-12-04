package com.albert.realmoneyrealtaste.adapter.webview.member.message

/**
 * Member 관련 모든 메시지 상수
 */
object MemberMessages {

    // ========== 인증 관련 메시지 ==========
    object Auth {
        const val ACTIVATION_EMAIL_RESENT = "활성화 이메일이 재전송되었습니다."
        const val PASSWORD_RESET_EMAIL_SENT = "비밀번호 재설정 이메일이 전송되었습니다."
        const val PASSWORD_RESET_SUCCESS = "비밀번호가 성공적으로 재설정되었습니다."

        const val INVALID_EMAIL_FORMAT = "올바른 이메일 형식을 입력해주세요."
        const val INVALID_PASSWORD_FORMAT = "비밀번호 형식이 올바르지 않습니다."
        const val PASSWORD_MISMATCH = "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
    }

    // ========== 설정 관련 메시지 ==========
    object Settings {
        const val ACCOUNT_UPDATE_SUCCESS = "계정 정보가 성공적으로 업데이트되었습니다."
        const val PASSWORD_UPDATE_SUCCESS = "비밀번호가 성공적으로 변경되었습니다."
        const val ACCOUNT_DELETE_SUCCESS = "계정이 성공적으로 삭제되었습니다."

        const val INVALID_INPUT_ERROR = "입력값이 올바르지 않습니다."
        const val INVALID_PASSWORD_FORMAT = "비밀번호 형식이 올바르지 않습니다."
        const val PASSWORD_MISMATCH = "비밀번호 확인이 일치하지 않습니다."
        const val ACCOUNT_DELETE_NOT_CONFIRMED = "계정 삭제 확인이 필요합니다."
    }

    // ========== 시스템 에러 메시지 ==========
    object Error {
        const val MEMBER_ACTIVATE_FAILED = "회원 활성화에 실패했습니다. 다시 시도해주세요."
        const val RESEND_ACTIVATION_EMAIL_FAILED = "활성화 이메일 재전송에 실패했습니다. 다시 시도해주세요."
        const val MEMBER_UPDATE_FAILED = "회원 정보 수정에 실패했습니다. 다시 시도해주세요."
        const val MEMBER_DEACTIVATE_FAILED = "회원 탈퇴에 실패했습니다. 다시 시도해주세요."
        const val PASSWORD_RESET_FAILED = "비밀번호 재설정에 실패했습니다. 다시 시도해주세요."
        const val MEMBER_NOT_FOUND = "회원 정보를 찾을 수 없습니다."
    }
}
