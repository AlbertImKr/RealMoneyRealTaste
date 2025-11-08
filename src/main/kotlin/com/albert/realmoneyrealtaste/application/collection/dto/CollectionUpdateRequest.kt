package com.albert.realmoneyrealtaste.application.collection.dto

import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo

data class CollectionUpdateRequest(
    val ownerMemberId: Long,
    val collectionId: Long,
    val newInfo: CollectionInfo,
)
