package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.application.collection.dto.CollectionUpdateRequest
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import com.albert.realmoneyrealtaste.domain.collection.PostCollection

fun interface CollectionUpdater {

    /**
     * 컬렉션 정보 업데이트
     *
     * @param request 업데이트 요청 정보
     * @return 업데이트된 컬렉션 정보
     * @throws CollectionUpdateException 업데이트 실패 시 발생
     */
    fun updateInfo(request: CollectionUpdateRequest): PostCollection
}
