package com.albert.realmoneyrealtaste.application.collection.provided

import com.albert.realmoneyrealtaste.application.collection.dto.CollectionUpdateRequest
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection

interface CollectionUpdater {

    /**
     * 컬렉션 정보 업데이트
     *
     * @param request 업데이트 요청 정보
     * @return 업데이트된 컬렉션 정보
     * @throws CollectionUpdateException 업데이트 실패 시 발생
     */
    fun updateInfo(request: CollectionUpdateRequest): PostCollection

    /**
     * 컬렉션에 게시글 추가
     *
     * @param collectionId 컬렉션 ID
     * @param postId 추가할 게시글 ID
     * @param ownerMemberId 컬렉션 소유자 멤버 ID
     */
    fun addPost(collectionId: Long, postId: Long, ownerMemberId: Long)

    /**
     * 컬렉션에서 게시글 제거
     *
     * @param collectionId 컬렉션 ID
     * @param postId 제거할 게시글 ID
     * @param ownerMemberId 컬렉션 소유자 멤버 ID
     */
    fun removePost(collectionId: Long, postId: Long, ownerMemberId: Long)

    fun updatePrivacy(collectionId: Long, ownerMemberId: Long, privacy: CollectionPrivacy): PostCollection
}
