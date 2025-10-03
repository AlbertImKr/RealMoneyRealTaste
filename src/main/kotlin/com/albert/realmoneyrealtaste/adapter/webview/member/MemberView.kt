package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class MemberView(
    private val memberActivate: MemberActivate,
) {

    @GetMapping("/members/activate")
    fun activate(
        @RequestParam("token") token: String,
        model: Model,
    ): String {
        val member = memberActivate.activate(token)

        model.addAttribute("nickname", member.nickname.value)
        model.addAttribute("success", true)

        return MEMBER_ACTIVATE_VIEW_NAME
    }

    companion object {
        const val MEMBER_ACTIVATE_VIEW_NAME = "member/activate"
        const val MEMBER_ACTIVATION_VIEW_NAME = "member/activation"
    }
}
