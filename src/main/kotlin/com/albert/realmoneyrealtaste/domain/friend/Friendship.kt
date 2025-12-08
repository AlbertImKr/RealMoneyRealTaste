package com.albert.realmoneyrealtaste.domain.friend

import com.albert.realmoneyrealtaste.domain.common.AggregateRoot
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendDomainEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.friend.value.FriendRelationship
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 * 친구 관계 애그리거트 루트
 *
 * 비즈니스 규칙:
 * - 친구 관계는 양방향이며 대칭적이다
 * - 같은 회원끼리는 한 번만 친구가 될 수 있다
 * - 친구 요청 -> 수락 -> 친구 관계 성립
 * - 친구 관계 해제 시 양방향 모두 삭제
 */
@Entity
@Table(
    name = "friendships",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["member_id", "friend_member_id"])
    ],
    indexes = [
        Index(name = "idx_friendship_member_id", columnList = "member_id"),
        Index(name = "idx_friendship_friend_member_id", columnList = "friend_member_id"),
        Index(name = "idx_friendship_friend_nickname", columnList = "friend_nickname"),
        Index(name = "idx_friendship_status", columnList = "status"),
        Index(name = "idx_friendship_created_at", columnList = "created_at")
    ]
)
class Friendship protected constructor(
    relationShip: FriendRelationship,

    status: FriendshipStatus,

    createdAt: LocalDateTime,

    updatedAt: LocalDateTime,
) : BaseEntity(), AggregateRoot {

    companion object {
        const val ERROR_ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED = "대기 중인 친구 요청만 수락할 수 있습니다"
        const val ERROR_ONLY_PENDING_REQUESTS_CAN_BE_REJECTED = "대기 중인 친구 요청만 거절할 수 있습니다"
        const val ERROR_ONLY_FRIENDS_CAN_UNFRIEND = "친구 관계인 경우만 해제할 수 있습니다"
        const val ERROR_CANNOT_SEND_REQUEST_TO_SELF = "자기 자신에게는 친구 요청을 보낼 수 없습니다"

        /**
         * 친구 요청 생성 (기존 호환성용)
         *
         *  @throws IllegalArgumentException 자기 자신에게 친구 요청을 보낼 수 없는 경우
         */
        fun request(requestCommand: FriendRequestCommand): Friendship {
            require(requestCommand.fromMemberId != requestCommand.toMemberId) { ERROR_CANNOT_SEND_REQUEST_TO_SELF }

            val now = LocalDateTime.now()
            val friendship = Friendship(
                relationShip = FriendRelationship.of(requestCommand),
                status = FriendshipStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )

            // 도메인 이벤트 발행
            friendship.addDomainEvent(
                FriendRequestSentEvent(
                    friendshipId = 0L, // drainDomainEvents에서 실제 ID로 설정
                    fromMemberId = requestCommand.fromMemberId,
                    toMemberId = requestCommand.toMemberId,
                    occurredAt = now,
                )
            )

            return friendship
        }
    }

    @Embedded
    var relationShip: FriendRelationship = relationShip
        protected set

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: FriendshipStatus = status
        protected set

    @Column(nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    @Transient
    private var domainEvents: MutableList<FriendDomainEvent> = mutableListOf()

    /**
     * 다시 친구 요청
     *
     * @throws IllegalArgumentException  친구 요청을 다시 보낼 수 없는 상태인 경우
     */
    fun rePending() {
        require(status == FriendshipStatus.UNFRIENDED || status == FriendshipStatus.REJECTED) {
            "친구 요청을 다시 보낼 수 없는 상태입니다. 현재 상태: $status"
        }
        status = FriendshipStatus.PENDING
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            FriendRequestSentEvent(
                friendshipId = requireId(),
                fromMemberId = relationShip.memberId,
                toMemberId = relationShip.friendMemberId,
                occurredAt = updatedAt,
            )
        )
    }

    /**
     * 친구 요청 수락
     *
     * @throws IllegalArgumentException 대기 중인 친구 요청이 아닌 경우
     */
    fun accept() {
        require(status == FriendshipStatus.PENDING) { ERROR_ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED }
        status = FriendshipStatus.ACCEPTED
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            FriendRequestAcceptedEvent(
                friendshipId = requireId(),
                fromMemberId = relationShip.memberId,
                toMemberId = relationShip.friendMemberId,
                occurredAt = updatedAt
            )
        )
    }

    /**
     * 친구 요청 거절
     *
     * @throws IllegalArgumentException 대기 중인 친구 요청이 아닌 경우
     */
    fun reject() {
        require(status == FriendshipStatus.PENDING) { ERROR_ONLY_PENDING_REQUESTS_CAN_BE_REJECTED }
        status = FriendshipStatus.REJECTED
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            FriendRequestRejectedEvent(
                friendshipId = requireId(),
                fromMemberId = relationShip.memberId,
                toMemberId = relationShip.friendMemberId
            )
        )
    }

    /**
     * 친구 관계 해제
     *
     * @throws IllegalArgumentException 친구 관계가 아닌 경우
     */
    fun unfriend() {
        require(status == FriendshipStatus.ACCEPTED) { ERROR_ONLY_FRIENDS_CAN_UNFRIEND }
        status = FriendshipStatus.UNFRIENDED
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            FriendshipTerminatedEvent(
                friendshipId = requireId(),
                memberId = relationShip.memberId,
                friendMemberId = relationShip.friendMemberId,
                occurredAt = updatedAt
            )
        )
    }

    /**
     * 특정 회원이 친구 요청을 받은 건인지 확인
     */
    fun isReceivedBy(memberId: Long): Boolean = relationShip.friendMemberId == memberId

    /**
     * 특정 회원이 친구 요청을 보낸 건인지 확인
     */
    fun isSentBy(memberId: Long): Boolean = relationShip.memberId == memberId

    /**
     * 특정 회원과 관련된 친구 관계인지 확인
     */
    fun isRelatedTo(memberId: Long): Boolean {
        return relationShip.memberId == memberId || relationShip.friendMemberId == memberId
    }

    /**
     * 회원 정보를 업데이트합니다 (크로스 도메인 이벤트 처리용)
     *
     * @param memberId 업데이트할 회원 ID
     * @param nickname 새 닉네임 (null이면 업데이트하지 않음)
     * @param imageId 새 이미지 ID (null이면 업데이트하지 않음)
     */
    fun updateMemberInfo(memberId: Long, nickname: String?, imageId: Long?) {
        if (relationShip.memberId == memberId) {
            // 요청자 정보 업데이트
            nickname?.let { relationShip = relationShip.copy(memberNickname = it) }
            imageId?.let { relationShip = relationShip.copy(memberProfileImageId = it) }
        } else if (relationShip.friendMemberId == memberId) {
            // 친구 정보 업데이트
            nickname?.let { relationShip = relationShip.copy(friendNickname = it) }
            imageId?.let { relationShip = relationShip.copy(friendProfileImageId = it) }
        } else {
            return
        }

        if (nickname != null || imageId != null) {
            updatedAt = LocalDateTime.now()
        }
    }

    /**
     * 도메인 이벤트 추가
     */
    private fun addDomainEvent(event: FriendDomainEvent) {
        domainEvents.add(event)
    }

    /**
     * 도메인 이벤트를 조회 및 초기화하고 ID를 설정합니다.
     */
    override fun drainDomainEvents(): List<FriendDomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()

        // 이벤트의 friendshipId를 실제 ID로 설정
        return events.map { event -> event.withFriendshipId(requireId()) }
    }
}
