package com.albert.realmoneyrealtaste.application.collection.required

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import org.springframework.data.repository.Repository

interface CollectionRepository : Repository<PostCollection, Long> {

    /**
     * 컬렉션 저장
     */
    fun save(collection: PostCollection): PostCollection

    /**
     * 컬렉션 아이디로 조회
     */
    fun findById(collectionId: Long): PostCollection?

    /**
     * 소유자 멤버 아이디로 컬렉션 개수 조회
     */
    fun countByOwnerMemberId(ownerMemberId: Long): Long

    /**
     * 컬렉션 삭제
     */
    fun findByOwnerMemberIdAndPrivacy(ownerMemberId: Long, privacy: CollectionPrivacy): List<PostCollection>

    /**
     * 컬렉션 아이디 및 소유자 멤버 아이디로 조회
     */
    fun findByIdAndOwnerMemberId(collectionId: Long, ownerMemberId: Long): PostCollection?
}
