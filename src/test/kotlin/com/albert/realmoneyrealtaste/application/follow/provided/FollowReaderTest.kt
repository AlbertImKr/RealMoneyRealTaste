package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.exception.FollowNotFoundException
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FollowReaderTest(
    private val followReader: FollowReader,
    private val followCreator: FollowCreator,
    private val followRepository: FollowRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Test
    fun `findActiveFollow - success - returns follow when exists and active`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "active-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "active-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())

        val result = followReader.findActiveFollow(follower.requireId(), following.requireId())

        assertAll(
            { assertEquals(follow.requireId(), result.requireId()) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) },
            { assertEquals(FollowStatus.ACTIVE, result.status) }
        )
    }

    @Test
    fun `findActiveFollow - failure - throws exception when follow does not exist`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "noexist-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "noexist-following@test.com",
            nickname = "following"
        )

        assertFailsWith<FollowNotFoundException> {
            followReader.findActiveFollow(follower.requireId(), following.requireId())
        }.let {
            assertEquals("팔로우 관계를 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `findActiveFollow - failure - throws exception when follow is unfollowed`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "unfollowed-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "unfollowed-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())
        follow.unfollow()
        followRepository.save(follow)

        assertFailsWith<FollowNotFoundException> {
            followReader.findActiveFollow(follower.requireId(), following.requireId())
        }.let {
            assertEquals("팔로우 관계를 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `findFollowById - success - returns follow when exists`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "byid-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "byid-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())

        val result = followReader.findFollowById(follow.requireId())

        assertAll(
            { assertEquals(follow.requireId(), result.requireId()) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) }
        )
    }

    @Test
    fun `findFollowById - failure - throws exception when follow does not exist`() {
        val nonExistentId = 999999L

        assertFailsWith<FollowNotFoundException> {
            followReader.findFollowById(nonExistentId)
        }.let {
            assertEquals("팔로우 관계를 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `findFollowersByMemberId - success - returns followers with member nicknames`() {
        val target = testMemberHelper.createActivatedMember(
            email = "target@test.com",
            nickname = "target"
        )
        val follower1 = testMemberHelper.createActivatedMember(
            email = "follower1@test.com",
            nickname = "follower1"
        )
        val follower2 = testMemberHelper.createActivatedMember(
            email = "follower2@test.com",
            nickname = "follower2"
        )

        // 두 명이 target을 팔로우
        createActiveFollow(follower1.requireId(), target.requireId())
        createActiveFollow(follower2.requireId(), target.requireId())

        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())
        val result = followReader.findFollowersByMemberId(target.requireId(), pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertFalse(result.isEmpty) },
            { assertTrue(result.content.any { it.followerNickname == "follower1" }) },
            { assertTrue(result.content.any { it.followerNickname == "follower2" }) },
            { assertTrue(result.content.all { it.status == FollowStatus.ACTIVE }) }
        )
    }

    @Test
    fun `findFollowersByMemberId - success - returns empty page when no followers`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nofollowers@test.com",
            nickname = "lonely"
        )

        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowersByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertTrue(result.isEmpty) }
        )
    }

    @Test
    fun `findFollowingsByMemberId - success - returns followings with member nicknames`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "follower@test.com",
            nickname = "follower"
        )
        val following1 = testMemberHelper.createActivatedMember(
            email = "following1@test.com",
            nickname = "following1"
        )
        val following2 = testMemberHelper.createActivatedMember(
            email = "following2@test.com",
            nickname = "following2"
        )

        // follower가 두 명을 팔로우
        createActiveFollow(follower.requireId(), following1.requireId())
        createActiveFollow(follower.requireId(), following2.requireId())

        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())
        val result = followReader.findFollowingsByMemberId(follower.requireId(), pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertFalse(result.isEmpty) },
            { assertTrue(result.content.any { it.followingNickname == "following1" }) },
            { assertTrue(result.content.any { it.followingNickname == "following2" }) },
            { assertTrue(result.content.all { it.status == FollowStatus.ACTIVE }) }
        )
    }

    @Test
    fun `checkIsFollowing - success - returns true when active follow exists`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "check-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "check-following@test.com",
            nickname = "following"
        )

        createActiveFollow(follower.requireId(), following.requireId())

        val follow = followReader.findActiveFollow(follower.requireId(), following.requireId())
        val result = followReader.checkIsFollowing(follower.requireId(), following.requireId())

        assertTrue(result)
    }

    @Test
    fun `checkIsFollowing - success - returns false when follow does not exist`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "nofollow-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "nofollow-following@test.com",
            nickname = "following"
        )

        val result = followReader.checkIsFollowing(follower.requireId(), following.requireId())

        assertFalse(result)
    }

    @Test
    fun `checkIsFollowing - success - returns false when follow is not active`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "inactive-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "inactive-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())
        follow.unfollow()
        followRepository.save(follow)

        val result = followReader.checkIsFollowing(follower.requireId(), following.requireId())

        assertFalse(result)
    }

    @Test
    fun `checkIsMutualFollow - success - returns true when both follow each other`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "mutual1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "mutual2@test.com",
            nickname = "member2"
        )

        // 서로 팔로우
        createActiveFollow(member1.requireId(), member2.requireId())
        createActiveFollow(member2.requireId(), member1.requireId())

        val result = followReader.checkIsMutualFollow(member1.requireId(), member2.requireId())

        assertTrue(result)
    }

    @Test
    fun `checkIsMutualFollow - success - returns false when only one direction exists`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "onesided1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "onesided2@test.com",
            nickname = "member2"
        )

        // 한쪽만 팔로우
        createActiveFollow(member1.requireId(), member2.requireId())

        val result = followReader.checkIsMutualFollow(member1.requireId(), member2.requireId())

        assertFalse(result)
    }

    @Test
    fun `checkIsMutualFollow - success - returns false when follows are not active`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "member1@test.com",
            nickname = "member1",
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "member2@test.com",
            nickname = "member2",
        )

        val result = followReader.checkIsMutualFollow(member1.requireId(), member2.requireId())

        assertFalse(result)
    }

    @Test
    fun `existsActiveFollow - success - returns true when active follow exists`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "exists-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "exists-following@test.com",
            nickname = "following"
        )

        createActiveFollow(follower.requireId(), following.requireId())

        val result = followReader.existsActiveFollow(follower.requireId(), following.requireId())

        assertTrue(result)
    }

    @Test
    fun `existsActiveFollow - success - returns false when follow does not exist`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "notexists-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "notexists-following@test.com",
            nickname = "following"
        )

        val result = followReader.existsActiveFollow(follower.requireId(), following.requireId())

        assertFalse(result)
    }

    @Test
    fun `getFollowStats - success - returns correct followers and following counts`() {
        val target = testMemberHelper.createActivatedMember(
            email = "stats-target@test.com",
            nickname = "target"
        )
        val follower1 = testMemberHelper.createActivatedMember(
            email = "stats-follower1@test.com",
            nickname = "follower1"
        )
        val follower2 = testMemberHelper.createActivatedMember(
            email = "stats-follower2@test.com",
            nickname = "follower2"
        )
        val following1 = testMemberHelper.createActivatedMember(
            email = "stats-following1@test.com",
            nickname = "following1"
        )

        // target이 follower1, follower2에게 팔로우받음 (followers: 2)
        createActiveFollow(follower1.requireId(), target.requireId())
        createActiveFollow(follower2.requireId(), target.requireId())

        // target이 following1을 팔로우 (following: 1)
        createActiveFollow(target.requireId(), following1.requireId())

        val result = followReader.getFollowStats(target.requireId())

        assertAll(
            { assertEquals(target.requireId(), result.memberId) },
            { assertEquals(2L, result.followersCount) },
            { assertEquals(1L, result.followingCount) }
        )
    }

    @Test
    fun `getFollowStats - success - returns zero counts when no follows`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nostats@test.com",
            nickname = "nostats"
        )

        val result = followReader.getFollowStats(member.requireId())

        assertAll(
            { assertEquals(member.requireId(), result.memberId) },
            { assertEquals(0L, result.followersCount) },
            { assertEquals(0L, result.followingCount) }
        )
    }

    @Test
    fun `findFollowersByMemberId - success - handles pagination correctly`() {
        val target = testMemberHelper.createActivatedMember(
            email = "pagination-target@test.com",
            nickname = "target"
        )

        // 5개의 팔로워 생성
        val followers = (1..5).map { index ->
            testMemberHelper.createActivatedMember(
                email = "pagefollower$index@test.com",
                nickname = "pagefollower$index"
            )
        }

        followers.forEach { follower ->
            createActiveFollow(follower.requireId(), target.requireId())
        }

        // 페이지 크기 2로 첫 번째 페이지 조회
        val firstPage = PageRequest.of(0, 2, Sort.by("createdAt").ascending())
        val firstResult = followReader.findFollowersByMemberId(target.requireId(), firstPage)

        // 두 번째 페이지 조회
        val secondPage = PageRequest.of(1, 2, Sort.by("createdAt").ascending())
        val secondResult = followReader.findFollowersByMemberId(target.requireId(), secondPage)

        assertAll(
            { assertEquals(5, firstResult.totalElements) },
            { assertEquals(2, firstResult.content.size) },
            { assertEquals(5, secondResult.totalElements) },
            { assertEquals(2, secondResult.content.size) },
            { assertTrue(firstResult.hasNext()) },
            { assertTrue(secondResult.hasNext()) }
        )
    }

    @Test
    fun `findFollowersByMemberId - success - excludes non-active follows`() {
        val target = testMemberHelper.createActivatedMember(
            email = "exclude-target@test.com",
            nickname = "target"
        )
        val activeFollower = testMemberHelper.createActivatedMember(
            email = "active-follower@test.com",
            nickname = "active"
        )
        val unfollowedFollower = testMemberHelper.createActivatedMember(
            email = "unfollowed-follower@test.com",
            nickname = "unfollowed"
        )

        // 하나는 활성, 하나는 언팔로우 상태
        createActiveFollow(activeFollower.requireId(), target.requireId())
        val unfollowedFollow = createActiveFollow(unfollowedFollower.requireId(), target.requireId())
        unfollowedFollow.unfollow()
        followRepository.save(unfollowedFollow)

        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowersByMemberId(target.requireId(), pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("active", result.content.first().followerNickname) },
            { assertEquals(FollowStatus.ACTIVE, result.content.first().status) }
        )
    }

    @Test
    fun `mapToFollowResponses - success - handles empty follow list`() {
        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowersByMemberId(999999L, pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertTrue(result.isEmpty) }
        )
    }

    @Test
    fun `mapToFollowResponses - success - maps multiple follows correctly`() {
        val target = testMemberHelper.createActivatedMember(
            email = "target@test.com",
            nickname = "target",
        )
        val follower1 = testMemberHelper.createActivatedMember(
            email = "follower1@test.com",
            nickname = "follower1",
        )
        val follower2 = testMemberHelper.createActivatedMember(
            email = "follower@test.com",
            nickname = "follower2",
        )

        createActiveFollow(follower1.requireId(), target.requireId())
        createActiveFollow(follower2.requireId(), target.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowersByMemberId(target.requireId(), pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.any { it.followerNickname == "follower1" }) },
            { assertTrue(result.content.any { it.followerNickname == "follower2" }) }
        )
    }

    @Test
    fun `getFollowStats - success - excludes unfollowed relationships`() {
        val member = testMemberHelper.createActivatedMember(
            email = "stats-exclude@test.com",
            nickname = "member"
        )
        val follower = testMemberHelper.createActivatedMember(
            email = "stats-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "stats-following@test.com",
            nickname = "following"
        )

        // 팔로우 생성 후 언팔로우
        val followerFollow = createActiveFollow(follower.requireId(), member.requireId())
        val followingFollow = createActiveFollow(member.requireId(), following.requireId())

        followerFollow.unfollow()
        followingFollow.unfollow()
        followRepository.save(followerFollow)
        followRepository.save(followingFollow)

        val result = followReader.getFollowStats(member.requireId())

        assertAll(
            { assertEquals(0L, result.followersCount) },
            { assertEquals(0L, result.followingCount) }
        )
    }

    @Test
    fun `findFollowByRelationship - success - returns follow when exists`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "relationship-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "relationship-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())

        val result = followReader.findFollowByRelationship(follower.requireId(), following.requireId())!!

        assertAll(
            { assertEquals(follow.requireId(), result.requireId()) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) }
        )
    }

    @Test
    fun `findFollowByRelationship - success - returns null when follow does not exist`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "no-relationship1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "no-relationship2@test.com",
            nickname = "member2"
        )

        val result = followReader.findFollowByRelationship(member1.requireId(), member2.requireId())

        assertEquals(null, result)
    }

    @Test
    fun `searchFollowers - success - returns followers matching keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "search-target@test.com",
            nickname = "target"
        )
        val matchingFollower1 = testMemberHelper.createActivatedMember(
            email = "match1@test.com",
            nickname = "searchable"
        )
        val matchingFollower2 = testMemberHelper.createActivatedMember(
            email = "match2@test.com",
            nickname = "searchable2"
        )
        val nonMatchingFollower = testMemberHelper.createActivatedMember(
            email = "nomatch@test.com",
            nickname = "other"
        )

        // 팔로우 관계 생성
        createActiveFollow(matchingFollower1.requireId(), member.requireId())
        createActiveFollow(matchingFollower2.requireId(), member.requireId())
        createActiveFollow(nonMatchingFollower.requireId(), member.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowers(member.requireId(), "search", pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.all { it.followerNickname.contains("search") }) },
            { assertFalse(result.content.any { it.followerNickname == "other" }) }
        )
    }

    @Test
    fun `searchFollowers - success - returns empty when no followers match keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "empty-search@test.com",
            nickname = "member"
        )
        val follower = testMemberHelper.createActivatedMember(
            email = "follower@test.com",
            nickname = "follower"
        )

        createActiveFollow(follower.requireId(), member.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowers(member.requireId(), "nonexistent", pageable)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `searchFollowings - success - returns followings matching keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "searching@test.com",
            nickname = "searcher"
        )
        val matchingFollowing1 = testMemberHelper.createActivatedMember(
            email = "following1@test.com",
            nickname = "searchable"
        )
        val matchingFollowing2 = testMemberHelper.createActivatedMember(
            email = "following2@test.com",
            nickname = "searchable2"
        )
        val nonMatchingFollowing = testMemberHelper.createActivatedMember(
            email = "following3@test.com",
            nickname = "other"
        )

        // 팔로우 관계 생성
        createActiveFollow(member.requireId(), matchingFollowing1.requireId())
        createActiveFollow(member.requireId(), matchingFollowing2.requireId())
        createActiveFollow(member.requireId(), nonMatchingFollowing.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowings(member.requireId(), "search", pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.all { it.followerNickname.contains("search") }) },
            { assertFalse(result.content.any { it.followerNickname == "other" }) }
        )
    }

    @Test
    fun `findFollowings - success - returns only followed member IDs`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "bulk-follower@test.com",
            nickname = "follower"
        )
        val members = (1..5).map { index ->
            testMemberHelper.createActivatedMember(
                email = "target$index@test.com",
                nickname = "target$index"
            )
        }

        // 일부 멤버만 팔로우
        createActiveFollow(follower.requireId(), members[0].requireId())
        createActiveFollow(follower.requireId(), members[2].requireId())
        createActiveFollow(follower.requireId(), members[4].requireId())

        val targetIds = members.map { it.requireId() }
        val result = followReader.findFollowings(follower.requireId(), targetIds)

        assertAll(
            { assertEquals(3, result.size) },
            { assertTrue(result.contains(members[0].requireId())) },
            { assertTrue(result.contains(members[2].requireId())) },
            { assertTrue(result.contains(members[4].requireId())) },
            { assertFalse(result.contains(members[1].requireId())) },
            { assertFalse(result.contains(members[3].requireId())) }
        )
    }

    @Test
    fun `findFollowings - success - returns empty list when no follows`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "no-follow@test.com",
            nickname = "follower"
        )
        val members = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "target$index@test.com",
                nickname = "target$index"
            )
        }

        val targetIds = members.map { it.requireId() }
        val result = followReader.findFollowings(follower.requireId(), targetIds)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchFollowings - success - returns empty when no followings match keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "empty-following-search@test.com",
            nickname = "member"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "following@test.com",
            nickname = "following"
        )

        createActiveFollow(member.requireId(), following.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowings(member.requireId(), "nonexistent", pageable)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `findFollowings - success - excludes unfollowed relationships`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "unfollow-test@test.com",
            nickname = "follower"
        )
        val members = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "target$index@test.com",
                nickname = "target$index"
            )
        }

        // 팔로우 생성
        val follow1 = createActiveFollow(follower.requireId(), members[0].requireId())
        val follow2 = createActiveFollow(follower.requireId(), members[1].requireId())
        val follow3 = createActiveFollow(follower.requireId(), members[2].requireId())

        // 하나 언팔로우
        follow2.unfollow()
        followRepository.save(follow2)

        val targetIds = members.map { it.requireId() }
        val result = followReader.findFollowings(follower.requireId(), targetIds)

        assertAll(
            { assertEquals(2, result.size) },
            { assertTrue(result.contains(members[0].requireId())) },
            { assertTrue(result.contains(members[2].requireId())) },
            { assertFalse(result.contains(members[1].requireId())) }
        )
    }

    @Test
    fun `findFollowersByMemberId - success - excludes deactivated members`() {
        val member = testMemberHelper.createActivatedMember(
            email = "deactivated-follower@test.com",
            nickname = "member"
        )
        val activeFollower = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )
        val deactivatedFollower = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )

        // 팔로우 관계 생성
        createActiveFollow(activeFollower.requireId(), member.requireId())
        createActiveFollow(deactivatedFollower.requireId(), member.requireId())

        // 비활성화
        deactivatedFollower.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowersByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(activeFollower.requireId(), result.content.first().followerId) },
            { assertFalse(result.content.any { it.followerId == deactivatedFollower.requireId() }) }
        )
    }

    @Test
    fun `findFollowingsByMemberId - success - excludes deactivated members`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "deactivated-following@test.com",
            nickname = "follower"
        )
        val activeFollowing = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )
        val deactivatedFollowing = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )

        // 팔로우 관계 생성
        createActiveFollow(follower.requireId(), activeFollowing.requireId())
        createActiveFollow(follower.requireId(), deactivatedFollowing.requireId())

        // 비활성화
        deactivatedFollowing.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = followReader.findFollowingsByMemberId(follower.requireId(), pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(activeFollowing.requireId(), result.content.first().followingId) },
            { assertFalse(result.content.any { it.followingId == deactivatedFollowing.requireId() }) }
        )
    }

    @Test
    fun `searchFollowers - success - excludes deactivated members`() {
        val member = testMemberHelper.createActivatedMember(
            email = "deactivated-search@test.com",
            nickname = "member"
        )
        val activeFollower = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "activeSearch"
        )
        val deactivatedFollower = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivatedSearch"
        )

        // 팔로우 관계 생성
        createActiveFollow(activeFollower.requireId(), member.requireId())
        createActiveFollow(deactivatedFollower.requireId(), member.requireId())

        // 비활성화
        deactivatedFollower.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowers(member.requireId(), "Search", pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(activeFollower.requireId(), result.content.first().followerId) },
            { assertFalse(result.content.any { it.followerId == deactivatedFollower.requireId() }) }
        )
    }

    @Test
    fun `searchFollowings - success - excludes deactivated members`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "deactivated-following@test.com",
            nickname = "follower"
        )
        val activeFollowing = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "activeSearch"
        )
        val deactivatedFollowing = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivatedSearch"
        )

        // 팔로우 관계 생성
        createActiveFollow(follower.requireId(), activeFollowing.requireId())
        createActiveFollow(follower.requireId(), deactivatedFollowing.requireId())

        // 비활성화
        deactivatedFollowing.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = followReader.searchFollowings(follower.requireId(), "Search", pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(activeFollowing.requireId(), result.content.first().followingId) },
            { assertFalse(result.content.any { it.followingId == deactivatedFollowing.requireId() }) }
        )
    }

    private fun createActiveFollow(followerId: Long, followingId: Long): Follow {
        val request = FollowCreateRequest(followerId, followingId)
        return followCreator.follow(request)
    }
}
