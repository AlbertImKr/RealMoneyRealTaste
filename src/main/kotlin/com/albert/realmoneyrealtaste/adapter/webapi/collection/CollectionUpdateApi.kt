package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webapi.collection.request.CollectionUpdateApiRequest
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionUpdater
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionUpdateApi(
    private val collectionUpdater: CollectionUpdater,
) {

    @PutMapping("/api/collections/{collectionId}")
    fun updateInfo(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable collectionId: Long,
        @Valid @RequestBody request: CollectionUpdateApiRequest,
    ): ResponseEntity<Map<String, Any>> {
        val collection = collectionUpdater.updateInfo(
            request.toServiceDto(collectionId = collectionId, ownerMemberId = principal.id)
        )

        return ResponseEntity.ok(
            mapOf(
                "success" to true, "collectionId" to collection.requireId(),
                "message" to "컬렉션 정보가 성공적으로 업데이트되었습니다."
            )
        )
    }

    @PostMapping("/api/collections/{collectionId}/posts/{postId}")
    fun addPost(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable collectionId: Long,
        @PathVariable postId: Long,
    ) {
        collectionUpdater.addPost(
            collectionId = collectionId,
            postId = postId,
            ownerMemberId = principal.id,
        )
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/collections/{collectionId}/posts/{postId}")
    fun removePost(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable collectionId: Long,
        @PathVariable postId: Long,
    ) {
        collectionUpdater.removePost(
            collectionId = collectionId,
            postId = postId,
            ownerMemberId = principal.id,
        )
    }
}
