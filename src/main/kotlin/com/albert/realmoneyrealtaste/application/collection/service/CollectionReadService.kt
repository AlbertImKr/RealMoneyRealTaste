package com.albert.realmoneyrealtaste.application.collection.service

import com.albert.realmoneyrealtaste.application.collection.exception.CollectionNotFoundException
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy.PUBLIC
import com.albert.realmoneyrealtaste.domain.collection.CollectionStatus
import com.albert.realmoneyrealtaste.domain.collection.CollectionStatus.ACTIVE
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CollectionReadService(
    private val collectionRepository: CollectionRepository,
) : CollectionReader {
    override fun readMyCollections(
        ownerMemberId: Long,
        pageRequest: Pageable,
    ): Page<PostCollection> {
        return collectionRepository.findByOwnerMemberIdAndStatus(
            ownerMemberId = ownerMemberId,
            status = ACTIVE,
            pageRequest = pageRequest,
        )
    }

    override fun readMyPublicCollections(
        ownerMemberId: Long,
        pageRequest: Pageable,
    ): Page<PostCollection> {
        return collectionRepository.findByOwnerMemberIdAndPrivacyAndStatus(
            ownerMemberId = ownerMemberId,
            privacy = PUBLIC,
            status = ACTIVE,
            pageRequest = pageRequest,
        )
    }

    override fun readById(collectionId: Long): PostCollection {
        return collectionRepository.findByIdAndStatusNot(collectionId, CollectionStatus.DELETED)
            ?: throw CollectionNotFoundException("컬렉션을 찾을 수 없습니다.")
    }
}
