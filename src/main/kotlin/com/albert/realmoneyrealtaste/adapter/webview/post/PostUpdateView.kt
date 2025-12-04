package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.form.PostEditForm
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

/**
 * 게시글 수정을 담당하는 뷰 컨트롤러
 * 단일 책임 원칙에 따라 게시글 수정 관련 기능만 처리합니다.
 */
@Controller
class PostUpdateView(
    private val postReader: PostReader,
    private val postUpdater: PostUpdater,
) {

    /**
     * 게시글 수정 페이지를 조회합니다.
     * 게시글 작성자만 접근 가능합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param postId 수정할 게시글 ID
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 수정 뷰
     */
    @GetMapping(PostUrls.UPDATE)
    fun editPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        model: Model,
    ): String {
        val post = postReader.readPostByAuthorAndId(memberPrincipal.id, postId)
        model.addAttribute("postEditForm", PostEditForm.fromPost(post))
        return PostViews.EDIT
    }

    /**
     * 게시글을 수정합니다.
     * 게시글 작성자만 수정 가능합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param postId 수정할 게시글 ID
     * @param postEditForm 게시글 수정 폼 데이터 (유효성 검증됨)
     * @return 수정된 게시글 상세 페이지로 리다이렉트
     */
    @PostMapping(PostUrls.UPDATE)
    fun updatePost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PathVariable postId: Long,
        @Valid @ModelAttribute postEditForm: PostEditForm,
    ): String {
        postUpdater.updatePost(postId, memberPrincipal.id, postEditForm.toPostEditRequest())
        return PostUrls.REDIRECT_READ_DETAIL.format(postId)
    }
}
