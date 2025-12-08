package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

/**
 * 회원 관리를 위한 리포지토리 인터페이스
 */
interface MemberRepository : Repository<Member, Long> {

    /**
     * 회원 저장
     *
     * @param member 저장할 회원 엔티티
     * @return 저장된 회원 엔티티
     */
    fun save(member: Member): Member

    /**
     * 이메일로 회원 조회
     *
     * @param email 조회할 이메일
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findByEmail(email: Email): Member?

    /**
     * ID로 회원 조회
     *
     * @param id 조회할 회원 ID
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findById(id: Long): Member?

    /**
     * ID와 상태로 회원 조회 (특정 상태가 아닌 경우)
     *
     * @param id 조회할 회원 ID
     * @param status 제외할 회원 상태
     * @return 조회된 회원 엔티티 또는 null
     */
    fun findByIdAndStatus(id: Long, status: MemberStatus): Member?

    /**
     * 프로필 주소로 회원 존재 여부 확인
     *
     * @param profileAddress 확인할 프로필 주소
     * @return 존재 여부
     */
    fun existsByDetailProfileAddress(profileAddress: ProfileAddress): Boolean

    /**
     * ID와 상태로 회원 존재 여부 확인 (특정 상태가 아닌 경우)
     *
     * @param id 확인할 회원 ID
     * @param status 제외할 회원 상태
     * @return 존재 여부
     */
    fun existsByIdAndStatus(id: Long, status: MemberStatus): Boolean

    /**
     * 이메일로 회원 존재 여부 확인
     *
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    fun existsByEmail(email: Email): Boolean

    /**
     * 여러 ID와 상태로 회원 목록 조회
     *
     * @param memberIds 조회할 회원 ID 목록
     * @param status 제외할 회원 상태
     * @return 조회된 회원 엔티티 목록
     */
    fun findAllByIdInAndStatus(memberIds: List<Long>, status: MemberStatus): List<Member>

    @Query(
        """
        SELECT m FROM Member m
        WHERE  m.status = :status
          AND m.id != :memberId
        ORDER BY FUNCTION('RAND')
        LIMIT :limit
        """
    )
    fun findSuggestedMembers(memberId: Long, status: MemberStatus, limit: Long): List<Member>

    /**
     * 회원의 게시글 수를 증가시킵니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param memberId 회원 ID
     */
    @Modifying
    @Query("UPDATE Member m SET m.postCount = m.postCount + 1 WHERE m.id = :memberId")
    fun incrementPostCount(memberId: Long)

    /**
     * 회원의 게시글 수를 감소시킵니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param memberId 회원 ID
     */
    @Modifying
    @Query("UPDATE Member m SET m.postCount = m.postCount - 1 WHERE m.id = :memberId AND m.postCount > 0")
    fun decrementPostCount(memberId: Long)

    /**
     * 회원의 팔로워 수를 업데이트합니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param memberId 회원 ID
     * @param count 새 팔로워 수
     */
    @Modifying
    @Query("UPDATE Member m SET m.followersCount = :count WHERE m.id = :memberId")
    fun updateFollowersCount(memberId: Long, count: Long)

    /**
     * 회원의 팔로잉 수를 업데이트합니다.
     * 동시성 문제를 방지하기 위해 DB 레벨에서 직접 업데이트합니다.
     *
     * @param memberId 회원 ID
     * @param count 새 팔로잉 수
     */
    @Modifying
    @Query("UPDATE Member m SET m.followingsCount = :count WHERE m.id = :memberId")
    fun updateFollowingsCount(memberId: Long, count: Long)
}
