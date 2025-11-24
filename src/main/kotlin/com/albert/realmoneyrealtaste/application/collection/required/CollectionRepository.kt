package com.albert.realmoneyrealtaste.application.collection.required

import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.CollectionStatus
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun findByIdAndStatusNot(
        collectionId: Long,
        status: CollectionStatus,
    ): PostCollection?

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

    /**
     * 소유자 멤버 아이디로 컬렉션 목록 조회
     */
    fun findByOwnerMemberId(ownerMemberId: Long, pageRequest: Pageable): Page<PostCollection>

    fun findByOwnerMemberIdAndStatus(
        ownerMemberId: Long,
        status: CollectionStatus,
        pageRequest: Pageable,
    ): Page<PostCollection>

    fun findByOwnerMemberIdAndPrivacy(
        ownerMemberId: Long,
        privacy: CollectionPrivacy,
        pageRequest: Pageable,
    ): Page<PostCollection>

    fun findByOwnerMemberIdAndPrivacyAndStatus(
        ownerMemberId: Long,
        privacy: CollectionPrivacy,
        status: CollectionStatus,
        pageRequest: Pageable,
    ): Page<PostCollection>
}
