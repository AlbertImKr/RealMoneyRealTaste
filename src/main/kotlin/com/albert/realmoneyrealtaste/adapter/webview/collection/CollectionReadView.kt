package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionFilter
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CollectionReadView(
    private val collectionReader: CollectionReader,
    private val memberReader: MemberReader,
    private val postReader: PostReader,
) {

    @GetMapping(CollectionUrls.MY_LIST_FRAGMENT)
    fun readMyCollections(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false) filter: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val collectionFilter = CollectionFilter.from(filter)
        val collections = when (collectionFilter) {
            CollectionFilter.PUBLIC -> collectionReader.readMyPublicCollections(
                ownerMemberId = principal.id,
                pageRequest = pageRequest,
            )

            CollectionFilter.ALL -> collectionReader.readMyCollections(
                ownerMemberId = principal.id,
                pageRequest = pageRequest,
            )
        }
        model.addAttribute("collections", collections)
        setCommonModelAttributes(model, principal, principal.id)
        return CollectionViews.MY_LIST
    }

    @GetMapping(CollectionUrls.COLLECTION_POSTS_FRAGMENT)
    fun readCollectionPostsFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC, size = 5) pageRequest: Pageable,
    ): String {
        val collection = collectionReader.readById(collectionId)

        setCommonModelAttributes(model, principal)

        model.addAttribute("collection", collection)

        model.addAttribute("posts", postReader.readPostsByIds(collection.posts.postIds))
        val myPosts = postReader.readPostsByAuthor(principal.id, pageRequest)
        model.addAttribute("myPosts", myPosts)


        return CollectionViews.COLLECTION_POSTS_FRAGMENT
    }

    @GetMapping(CollectionUrls.DETAIL_EDIT_FRAGMENT)
    fun readCollectionDetailEditFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val collection = collectionReader.readById(collectionId)
        model.addAttribute("collection", collection)
        setCommonModelAttributes(model, principal)
        return CollectionViews.DETAIL_EDIT_FRAGMENT
    }

    @GetMapping(CollectionUrls.DETAIL_FRAGMENT)
    fun readCollectionDetailFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val collection = collectionReader.readById(collectionId)

        model.addAttribute("collection", collection)
        setCommonModelAttributes(model, principal)
        return CollectionViews.DETAIL_FRAGMENT
    }

    @GetMapping(CollectionUrls.WRITE_FRAGMENT)
    fun writeCollection(): String {
        return CollectionViews.WRITE_FRAGMENT
    }

    /**
     * 특정 사용자의 컬렉션 목록 조회
     */
    @GetMapping(CollectionUrls.MEMBER_COLLECTIONS_FRAGMENT)
    fun readMemberCollections(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val collections = collectionReader.readMyCollections(
            ownerMemberId = id,
            pageRequest = pageRequest
        )

        model.addAttribute("collections", collections)
        setCommonModelAttributes(model, principal, id)
        return CollectionViews.MY_LIST
    }

    /**
     * 특정 사용자의 컬렉션 상세 프래그먼트 조회
     */
    @GetMapping(CollectionUrls.MEMBER_COLLECTION_DETAIL_FRAGMENT)
    fun readMemberCollectionDetailFragment(
        @PathVariable memberId: Long,
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        model: Model,
    ): String {
        val collection = collectionReader.readById(collectionId)

        model.addAttribute("collection", collection)
        setCommonModelAttributes(model, principal, memberId)
        return CollectionViews.DETAIL_FRAGMENT
    }

    /**
     * 공통 Model 속성 설정
     */
    private fun setCommonModelAttributes(
        model: Model,
        principal: MemberPrincipal?,
        authorId: Long? = null,
    ) {
        model.addAttribute("member", principal)
        authorId?.let {
            model.addAttribute("author", memberReader.readMemberById(it))
        }
    }
}
