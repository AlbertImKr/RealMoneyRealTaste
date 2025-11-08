package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webapi.collection.request.CollectionUpdateApiRequest
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionUpdater
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
            request.toServiceDto(collectionId = collectionId, ownerMemberId = principal.memberId)
        )

        return ResponseEntity.ok(
            mapOf(
                "success" to true, "collectionId" to collection.requireId(),
                "message" to "컬렉션 정보가 성공적으로 업데이트되었습니다."
            )
        )
    }
}
