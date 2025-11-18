package com.albert.realmoneyrealtaste.application.collection.service

import com.albert.realmoneyrealtaste.application.collection.provided.CollectionDeleter
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus

@Service
class CollectionDeleteService(
    private val collectionReader: CollectionReader,
) : CollectionDeleter {

    @ResponseStatus(NO_CONTENT)
    @Transactional
    override fun deleteCollection(
        collectionId: Long,
        ownerMemberId: Long,
    ) {
        val collection = collectionReader.readById(collectionId)
        collection.delete(ownerMemberId)
    }
}
