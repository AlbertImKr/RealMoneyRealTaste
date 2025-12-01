package com.albert.realmoneyrealtaste.adapter.webview.util

import org.springframework.validation.BindingResult

/**
 * BindingResult 관련 유틸리티 클래스
 */
object BindingResultUtils {

    /**
     * BindingResult에서 첫 번째 에러 메시지만 추출
     *
     * @param bindingResult 검증 결과
     * @return 첫 번째 에러 메시지 또는 빈 문자열
     */
    fun extractFirstErrorMessage(bindingResult: BindingResult): String {
        return bindingResult.allErrors.firstOrNull { it.defaultMessage != null }?.defaultMessage.orEmpty()
    }
}
