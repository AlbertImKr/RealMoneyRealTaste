package com.albert.realmoneyrealtaste.adapter.webview

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.PostCreateForm
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeView {

    @GetMapping("/")
    fun home(
        model: Model,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
    ): String {
        model.addAttribute("postCreateForm", PostCreateForm())
        model.addAttribute("member", memberPrincipal)
        return "index"
    }
}
