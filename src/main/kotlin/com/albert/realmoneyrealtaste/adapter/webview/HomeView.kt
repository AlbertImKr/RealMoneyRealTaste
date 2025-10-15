package com.albert.realmoneyrealtaste.adapter.webview

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.PostCreateForm
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.provided.PostHeartReader
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeView(
    private val postReader: PostReader,
    private val memberReader: MemberReader,
    private val postHeartReader: PostHeartReader,
) {

    @GetMapping("/")
    fun home(
        model: Model,
        @PageableDefault(size = 10) pageable: Pageable,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
    ): String {
        model.addAttribute("postCreateForm", PostCreateForm())

        val posts = postReader.readAllPosts(pageable)
        model.addAttribute("posts", posts)

        val postIds = posts.content.map { it.requireId() }

        if (memberPrincipal != null) {
            val memberId = memberPrincipal.memberId
            val member = memberReader.readMemberById(memberId)
            model.addAttribute("member", member)

            val hearts = postHeartReader.findByMemberIdAndPostIds(memberId, postIds)
                .map(PostHeart::postId)
            model.addAttribute("hearts", hearts)
        }

        return "index"
    }
}
