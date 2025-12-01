package com.albert.realmoneyrealtaste.adapter.webview.member.config

import com.albert.realmoneyrealtaste.adapter.webview.member.converter.StringToEmailConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class MemberWeConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToEmailConverter())
    }
}
