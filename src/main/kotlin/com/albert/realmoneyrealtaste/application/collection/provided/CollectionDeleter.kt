package com.albert.realmoneyrealtaste.application.collection.provided

fun interface CollectionDeleter {

    fun deleteCollection(
        collectionId: Long,
        ownerMemberId: Long,
    )
}
