package com.albert.realmoneyrealtaste.application.collection.service

import com.albert.realmoneyrealtaste.application.collection.dto.CollectionUpdateRequest
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionUpdater
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
class CollectionUpdateService(
    private val collectionRepository: CollectionRepository,
) : CollectionUpdater {

    override fun updateInfo(
        request: CollectionUpdateRequest,
    ): PostCollection {
        try {
            val collection = collectionRepository.findByIdAndOwnerMemberId(
                collectionId = request.collectionId,
                ownerMemberId = request.ownerMemberId,
            ) ?: throw IllegalArgumentException("해당 컬렉션을 찾을 수 없거나 권한이 없습니다.")

            collection.updateInfo(
                memberId = request.ownerMemberId,
                newInfo = request.newInfo,
            )
            return collection
        } catch (e: IllegalArgumentException) {
            throw CollectionUpdateException("컬렉션 정보 업데이트 중 오류가 발생했습니다: ${e.message}", e)
        }
    }
}
