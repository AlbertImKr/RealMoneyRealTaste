package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.post.provided.PostCreator
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.application.post.provided.PostUpdater
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class PostView(
    private val postCreator: PostCreator,
    private val postReader: PostReader,
    private val postUpdater: PostUpdater,
) {
    @PostMapping("/posts/new")
    fun createPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute form: PostCreateForm,
    ): String {
        postCreator.createPost(memberPrincipal.memberId, form.toPostCreateRequest())
        return "redirect:/"
    }

    @GetMapping("/posts/{postId}")
    fun readPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        val currentUserId = memberPrincipal.memberId
        val post = postReader.readPostById(currentUserId, postId)
        model.addAttribute("post", post)
        model.addAttribute("currentUserId", currentUserId)
        return POST_DETAIL_VIEW_NAME
    }

    @GetMapping("/posts/{postId}/edit")
    fun editPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        val post = postReader.readPostByAuthorAndId(memberPrincipal.memberId, postId)
        model.addAttribute("postEditForm", PostEditForm.fromPost(post))
        return "post/edit"
    }

    @PostMapping("/posts/{postId}/edit")
    fun updatePost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        @Valid @ModelAttribute postEditForm: PostEditForm,
    ): String {
        postUpdater.updatePost(postId, memberPrincipal.memberId, postEditForm.toPostEditRequest())
        return "redirect:/posts/$postId"
    }

    @GetMapping("/posts/{postId}/modal")
    fun readPostDetailModal(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        val currentUserId = memberPrincipal.memberId
        val post = postReader.readPostById(currentUserId, postId)
        model.addAttribute("post", post)

        return "post/modal-detail :: post-detail-modal"
    }

    companion object {
        const val POST_DETAIL_VIEW_NAME = "post/detail"
    }
}
