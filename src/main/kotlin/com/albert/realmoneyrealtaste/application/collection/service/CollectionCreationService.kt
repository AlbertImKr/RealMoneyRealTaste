package com.albert.realmoneyrealtaste.application.collection.service

import com.albert.realmoneyrealtaste.application.collection.exception.CollectionCreateException
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionCreator
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CollectionCreationService(
    private val collectionRepository: CollectionRepository,
    private val memberReader: MemberReader,
) : CollectionCreator {

    companion object {
        const val ERROR_CREATING_COLLECTION = "컬렉션 생성 중 오류가 발생했습니다."
    }

    override fun createCollection(createCommand: CollectionCreateCommand): PostCollection {
        try {
            memberReader.readActiveMemberById(createCommand.ownerMemberId)

            val collection = PostCollection.create(createCommand)

            return collectionRepository.save(collection)
        } catch (e: IllegalArgumentException) {
            throw CollectionCreateException(ERROR_CREATING_COLLECTION, e)
        }
    }
}
