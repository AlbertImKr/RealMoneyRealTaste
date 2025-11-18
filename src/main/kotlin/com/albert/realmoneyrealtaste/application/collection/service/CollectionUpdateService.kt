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

    companion object {
        const val ERROR_READING_COLLECTION = "해당 컬렉션을 찾을 수 없거나 권한이 없습니다."
        const val ERROR_UPDATING_COLLECTION = "컬렉션 정보 업데이트 중 오류가 발생했습니다."
    }

    override fun updateInfo(
        request: CollectionUpdateRequest,
    ): PostCollection {
        try {
            val collection = collectionRepository.findByIdAndOwnerMemberId(
                collectionId = request.collectionId,
                ownerMemberId = request.ownerMemberId,
            ) ?: throw IllegalArgumentException(ERROR_READING_COLLECTION)

            collection.updateInfo(
                memberId = request.ownerMemberId,
                newInfo = request.newInfo,
            )
            return collection
        } catch (e: IllegalArgumentException) {
            throw CollectionUpdateException(ERROR_UPDATING_COLLECTION, e)
        }
    }

    override fun addPost(collectionId: Long, postId: Long, ownerMemberId: Long) {
        val collection = collectionRepository.findByIdAndOwnerMemberId(
            collectionId = collectionId,
            ownerMemberId = ownerMemberId,
        ) ?: throw IllegalArgumentException(ERROR_READING_COLLECTION)

        collection.addPost(
            memberId = ownerMemberId,
            postId = postId,
        )
    }

    override fun removePost(collectionId: Long, postId: Long, ownerMemberId: Long) {
        val collection = collectionRepository.findByIdAndOwnerMemberId(
            collectionId = collectionId,
            ownerMemberId = ownerMemberId,
        ) ?: throw IllegalArgumentException(ERROR_READING_COLLECTION)

        collection.removePost(
            memberId = ownerMemberId,
            postId = postId,
        )
    }
}
