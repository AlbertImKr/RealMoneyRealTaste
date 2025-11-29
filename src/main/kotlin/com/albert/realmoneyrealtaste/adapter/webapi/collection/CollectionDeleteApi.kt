package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionDeleter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionDeleteApi(
    private val collectionDeleter: CollectionDeleter,
) {

    @DeleteMapping("/api/collections/{collectionId}")
    fun deleteCollection(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable collectionId: Long,
    ) {
        collectionDeleter.deleteCollection(
            collectionId = collectionId,
            ownerMemberId = principal.id,
        )
    }
}
