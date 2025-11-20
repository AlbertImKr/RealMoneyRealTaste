package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.post.provided.PostCreator
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.application.post.provided.PostUpdater
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
    @PostMapping(PostUrls.CREATE)
    fun createPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute form: PostCreateForm,
    ): String {
        val post = postCreator.createPost(memberPrincipal.memberId, form.toPostCreateRequest())
        return PostUrls.REDIRECT_READ_DETAIL.format(post.requireId())
    }

    @GetMapping(PostUrls.READ_MY_LIST)
    fun readMyPosts(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPostsByAuthor(
            authorId = memberPrincipal.memberId,
            pageable = pageable,
        )
        model.addAttribute("postCreateForm", PostCreateForm())
        model.addAttribute("posts", postsPage)
        // author: 프로필 페이지의 주인 (게시물 작성자)
        model.addAttribute("author", memberPrincipal)
        // member: 현재 로그인한 사용자 (뷰에서 권한 확인용)
        model.addAttribute("member", memberPrincipal)
        return PostViews.MY_LIST
    }

    @GetMapping(PostUrls.READ_MY_LIST_FRAGMENT)
    fun readMyPostsFragment(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPostsByAuthor(
            authorId = memberPrincipal.memberId,
            pageable = pageable,
        )
        model.addAttribute("posts", postsPage)
        model.addAttribute("member", memberPrincipal)
        return PostViews.POSTS_CONTENT
    }

    @GetMapping(PostUrls.READ_LIST_FRAGMENT)
    fun readPosts(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        if (memberPrincipal == null) {
            val postsPage = postReader.readPosts(pageable)
            model.addAttribute("posts", postsPage)
            return PostViews.POSTS_CONTENT
        }
        val postsPage = postReader.readPostsByAuthor(
            authorId = memberPrincipal.memberId,
            pageable = pageable,
        )
        model.addAttribute("posts", postsPage)
        model.addAttribute("member", memberPrincipal)
        return PostViews.POSTS_CONTENT
    }

    @GetMapping(PostUrls.READ_DETAIL)
    fun readPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        if (memberPrincipal == null) {
            val post = postReader.readPostById(postId)
            model.addAttribute("post", post)
            return PostViews.DETAIL
        }
        postDetailModelSetup(memberPrincipal, postId, model)
        return PostViews.DETAIL
    }

    @GetMapping(PostUrls.UPDATE)
    fun editPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        val post = postReader.readPostByAuthorAndId(memberPrincipal.memberId, postId)
        model.addAttribute("postEditForm", PostEditForm.fromPost(post))
        return PostViews.EDIT
    }

    @PostMapping(PostUrls.UPDATE)
    fun updatePost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        @Valid @ModelAttribute postEditForm: PostEditForm,
    ): String {
        postUpdater.updatePost(postId, memberPrincipal.memberId, postEditForm.toPostEditRequest())
        return PostUrls.REDIRECT_READ_DETAIL.format(postId)
    }

    @GetMapping(PostUrls.READ_DETAIL_MODAL)
    fun readPostDetailModal(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        if (memberPrincipal == null) {
            val post = postReader.readPostById(postId)
            model.addAttribute("post", post)
            return PostViews.DETAIL_MODAL
        }
        postDetailModelSetup(memberPrincipal, postId, model)

        return PostViews.DETAIL_MODAL
    }

    private fun postDetailModelSetup(
        memberPrincipal: MemberPrincipal,
        postId: Long,
        model: Model,
    ) {
        val currentUserId = memberPrincipal.memberId
        val post = postReader.readPostById(currentUserId, postId)
        model.addAttribute("post", post)
        model.addAttribute("currentUserId", currentUserId)
        model.addAttribute("currentUserNickname", memberPrincipal.nickname.value)
    }
}
