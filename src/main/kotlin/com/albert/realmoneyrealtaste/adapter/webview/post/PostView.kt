package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
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
import org.springframework.web.bind.annotation.RequestParam

@Controller
class PostView(
    private val postCreator: PostCreator,
    private val postReader: PostReader,
    private val postUpdater: PostUpdater,
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

    /**
     * 현재 로그인한 사용자의 게시글 목록 페이지를 조회합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param pageable 페이징 정보 (기본: 생성일 내림차순, 10개씩)
     * @param model 뷰에 전달할 데이터 모델
     * @return 내 게시글 목록 뷰
     */
    @GetMapping(PostUrls.READ_MY_LIST)
    fun readMyPosts(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPostsByAuthor(
            authorId = memberPrincipal.id,
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

    /**
     * 특정 멤버의 게시글 목록 프래그먼트를 조회합니다.
     * 비동기 AJAX 요청에 사용됩니다.
     *
     * @param id 게시글 작성자 ID
     * @param memberPrincipal 현재 인증된 사용자 정보 (선택사항)
     * @param pageable 페이징 정보 (기본: 생성일 내림차순, 10개씩)
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 목록 프래그먼트 뷰
     */
    @GetMapping(PostUrls.READ_MEMBER_POSTS_FRAGMENT)
    fun readMemberPostsFragment(
        @PathVariable id: Long,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPostsByAuthor(id, pageable)

        model.addAttribute("authorId", id)
        model.addAttribute("posts", postsPage)
        model.addAttribute("member", memberPrincipal) // 현재 로그인한 사용자

        return PostViews.POSTS_CONTENT
    }

    /**
     * 현재 로그인한 사용자의 게시글 목록 프래그먼트를 조회합니다.
     * 비동기 AJAX 요청에 사용됩니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param pageable 페이징 정보 (기본: 생성일 내림차순, 10개씩)
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 목록 프래그먼트 뷰
     */
    @GetMapping(PostUrls.READ_MY_LIST_FRAGMENT)
    fun readMyPostsFragment(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPostsByAuthor(
            authorId = memberPrincipal.id,
            pageable = pageable,
        )
        model.addAttribute("posts", postsPage)
        model.addAttribute("member", memberPrincipal)
        model.addAttribute("author", memberPrincipal)
        return PostViews.POSTS_CONTENT
    }

    /**
     * 전체 게시글 목록 프래그먼트를 조회합니다.
     * 인증된 사용자와 비인증 사용자 모두 접근 가능합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보 (선택사항)
     * @param pageable 페이징 정보 (기본: 생성일 내림차순, 10개씩)
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 목록 프래그먼트 뷰
     */
    @GetMapping(PostUrls.READ_LIST_FRAGMENT)
    fun readPosts(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val postsPage = postReader.readPosts(pageable)
        if (memberPrincipal == null) {
            model.addAttribute("posts", postsPage)
            return PostViews.POSTS_CONTENT
        }
        model.addAttribute("posts", postsPage)
        model.addAttribute("member", memberPrincipal)
        model.addAttribute("author", memberPrincipal)
        return PostViews.POSTS_CONTENT
    }

    /**
     * 특정 게시글의 상세 페이지를 조회합니다.
     * 인증된 사용자와 비인증 사용자 모두 접근 가능합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보 (선택사항)
     * @param postId 조회할 게시글 ID
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 상세 뷰
     */
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

    /**
     * 게시글 상세 정보를 모달 형태로 조회합니다.
     * 인증된 사용자와 비인증 사용자 모두 접근 가능합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보 (선택사항)
     * @param postId 조회할 게시글 ID
     * @param model 뷰에 전달할 데이터 모델
     * @return 게시글 상세 모달 뷰
     */
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

    /**
     * 인증된 사용자를 위한 게시글 상세 페이지 모델 설정을 수행합니다.
     * 사용자 권한에 따른 접근 제어와 추가 정보를 설정합니다.
     *
     * @param memberPrincipal 현재 인증된 사용자 정보
     * @param postId 조회할 게시글 ID
     * @param model 뷰에 전달할 데이터 모델
     */
    private fun postDetailModelSetup(
        memberPrincipal: MemberPrincipal,
        postId: Long,
        model: Model,
    ) {
        val currentUserId = memberPrincipal.id
        val post = postReader.readPostById(currentUserId, postId)
        model.addAttribute("post", post)
        model.addAttribute("currentUserId", currentUserId)
        model.addAttribute("currentUserNickname", memberPrincipal.nickname.value)
    }

    /**
     * 컬렉션에 속한 게시글 목록 프래그먼트를 조회합니다.
     * 특정 컬렉션의 게시글들을 ID 목록으로 조회하여 표시합니다.
     *
     * @param postIds 조회할 게시글 ID 목록
     * @param collectionId 컬렉션 ID
     * @param authorId 게시글 작성자 ID
     * @param principal 현재 인증된 사용자 정보 (선택사항)
     * @param model 뷰에 전달할 데이터 모델
     * @return 컬렉션 게시글 목록 프래그먼트 뷰
     */
    @GetMapping(PostUrls.READ_COLLECTION_POSTS_FRAGMENT)
    fun readPostListFragment(
        @RequestParam postIds: List<Long>,
        @PathVariable collectionId: Long,
        @PathVariable authorId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        model: Model,
    ): String {
        if (principal == null) {
            val posts = postIds.map { postReader.readPostById(it) }
            model.addAttribute("posts", posts)
            model.addAttribute("authorId", authorId)
            model.addAttribute("collectionId", collectionId)
            return PostViews.POST_LIST_FRAGMENT
        }
        val posts = postIds.map { postReader.readPostById(principal.id, it) }

        model.addAttribute("posts", posts)
        model.addAttribute("authorId", authorId)
        model.addAttribute("collectionId", collectionId)
        model.addAttribute("member", principal)

        return PostViews.POST_LIST_FRAGMENT
    }
}
