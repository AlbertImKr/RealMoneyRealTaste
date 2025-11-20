package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
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
class CollectionView(
    private val collectionReader: CollectionReader,
    private val postReader: PostReader,
    private val memberReader: MemberReader,
) {

    @GetMapping(CollectionUrls.MY_LIST_FRAGMENT)
    fun readMyCollections(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false, defaultValue = "all") filter: String,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val collections = when (filter) {
            "public" -> collectionReader.readMyPublicCollections(
                ownerMemberId = principal.memberId,
                pageRequest = pageRequest,
            )

            else -> collectionReader.readMyCollections(
                ownerMemberId = principal.memberId,
                pageRequest = pageRequest,
            )
        }
        model.addAttribute("collections", collections)
        model.addAttribute("author", memberReader.readMemberById(principal.memberId))
        model.addAttribute("member", principal)
        return CollectionViews.MY_LIST
    }

    @GetMapping(CollectionUrls.DETAIL_EDIT_FRAGMENT)
    fun readCollectionDetailEditFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val collection = collectionReader.readById(collectionId)
        model.addAttribute("collection", collection)
        model.addAttribute("member", principal)
        return CollectionViews.DETAIL_EDIT_FRAGMENT
    }

    @GetMapping(CollectionUrls.DETAIL_FRAGMENT)
    fun readCollectionDetailFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val collection = collectionReader.readById(collectionId)
        val posts = postReader.readPostsByIds(
            postIds = collection.posts.postIds
        )

        model.addAttribute("posts", posts)
        model.addAttribute("collection", collection)
        model.addAttribute("member", principal)
        return CollectionViews.DETAIL_FRAGMENT
    }

    @GetMapping(CollectionUrls.WRITE_FRAGMENT)
    fun writeCollection(): String {
        return CollectionViews.WRITE_FRAGMENT
    }

    @GetMapping(CollectionUrls.COLLECTION_POSTS_FRAGMENT)
    fun readCollectionPostsFragment(
        @PathVariable collectionId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC, size = 5) pageRequest: Pageable,
    ): String {
        val collection = collectionReader.readById(collectionId)
        val posts = postReader.readPostsByIds(
            postIds = collection.posts.postIds
        )
        val myPosts = postReader.readPostsByAuthor(
            authorId = principal.memberId,
            pageable = pageRequest,
        )

        model.addAttribute("collection", collection)
        model.addAttribute("member", principal)
        model.addAttribute("posts", posts)
        model.addAttribute("myPosts", myPosts)

        return CollectionViews.COLLECTION_POSTS_FRAGMENT
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

        val member = memberReader.readMemberById(id)

        model.addAttribute("collections", collections)
        model.addAttribute("author", member)
        model.addAttribute("member", principal)
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
        val posts = postReader.readPostsByIds(
            postIds = collection.posts.postIds
        )

        val member = memberReader.readMemberById(memberId)

        model.addAttribute("collection", collection)
        model.addAttribute("posts", posts)
        model.addAttribute("author", member)
        model.addAttribute("member", principal)
        return CollectionViews.DETAIL_FRAGMENT
    }
}
