package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand

fun interface CollectionCreator {
    fun createCollection(createCommand: CollectionCreateCommand): PostCollection
}
