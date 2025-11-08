package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webapi.collection.request.CollectionCreateApiRequest
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionCreator
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionCreateApi(
    private val collectionCreator: CollectionCreator,
) {

    /**
     * 컬렉션 생성
     */
    @PostMapping("/api/collections")
    fun createCollection(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @Valid @RequestBody request: CollectionCreateApiRequest,
    ): ResponseEntity<Map<String, Any>> {
        val collection = collectionCreator.createCollection(
            request.toCommand(ownerMemberId = principal.memberId)
        )

        return ResponseEntity.ok(
            mapOf(
                "success" to true, "collectionId" to collection.requireId(),
                "message" to "컬렉션이 성공적으로 생성되었습니다."
            )
        )
    }
}
