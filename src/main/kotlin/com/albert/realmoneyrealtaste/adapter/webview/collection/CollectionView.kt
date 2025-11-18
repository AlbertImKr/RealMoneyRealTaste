package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CollectionView(
    private val collectionReader: CollectionReader,
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
        model.addAttribute("member", principal)
        return CollectionViews.MY_LIST
    }

    @GetMapping(CollectionUrls.WRITE_FRAGMENT)
    fun writeCollection(): String {
        return CollectionViews.WRITE_FRAGMENT
    }
}
