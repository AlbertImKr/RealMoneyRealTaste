package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.form.PostCreateForm
import com.albert.realmoneyrealtaste.application.post.provided.PostCreator
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

/**
 * 게시글 생성을 담당하는 뷰 컨트롤러
 * 단일 책임 원칙에 따라 게시글 생성 관련 기능만 처리합니다.
 */
@Controller
class PostCreationView(
    private val postCreator: PostCreator,
) {

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param form 게시글 생성 폼 데이터 (유효성 검증됨)
     * @return 생성된 게시글 상세 페이지로 리다이렉트
     */
    @PostMapping(PostUrls.CREATE)
    fun createPost(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute form: PostCreateForm,
    ): String {
        val post = postCreator.createPost(memberPrincipal.id, form.toPostCreateRequest())
        return PostUrls.REDIRECT_READ_DETAIL.format(post.requireId())
    }
}
