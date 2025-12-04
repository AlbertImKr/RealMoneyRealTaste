package com.albert.realmoneyrealtaste.adapter.webview.member.converter

import com.albert.realmoneyrealtaste.domain.member.value.Email
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

/**
 * String을 Email 도메인 객체로 변환하는 컨버터
 */
@Component
class StringToEmailConverter : Converter<String, Email> {

    override fun convert(source: String): Email {
        return Email(source)
    }
}
