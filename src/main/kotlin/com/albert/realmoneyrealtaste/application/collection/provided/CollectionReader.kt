package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CollectionReader {

    fun readMyCollections(ownerMemberId: Long, pageRequest: Pageable): Page<PostCollection>

    fun readMyPublicCollections(ownerMemberId: Long, pageRequest: Pageable): Page<PostCollection>
}
