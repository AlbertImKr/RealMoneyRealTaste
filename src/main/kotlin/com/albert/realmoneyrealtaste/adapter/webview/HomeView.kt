package com.albert.realmoneyrealtaste.adapter.webview

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.PostCreateForm
import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.provided.PostHeartReader
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeView(
    private val postReader: PostReader,
    private val memberReader: MemberReader,
    private val postHeartReader: PostHeartReader,
    private val followReader: FollowReader,
) {

    @GetMapping("/")
    fun home(
        model: Model,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
    ): String {
        model.addAttribute("postCreateForm", PostCreateForm())

        val posts = addPosts(pageable, model)

        val postIds = posts.content.map { it.requireId() }

        if (memberPrincipal != null) {
            val memberId = memberPrincipal.memberId
            // 사용자 정보 추가
            addMemberInfo(memberId, model)

            // 사용자 통계 정보 추가
            addMemberStats(model, memberId)

            // 좋아요 정보 추가
            addHeartsStats(memberId, postIds, model)

            // 추천 사용자 목록 추가 (옵션)
            val addSuggestedMembers = addSuggestedMembers(model, memberId)
            val suggestedMemberIds = addSuggestedMembers.map { it.requireId() }

            // following
            addFollowingStatus(model, memberPrincipal.memberId, suggestedMemberIds)
        }

        return "index"
    }

    private fun addPosts(
        pageable: Pageable,
        model: Model,
    ): Page<Post> {
        val posts = postReader.readAllPosts(pageable)
        model.addAttribute("posts", posts)
        return posts
    }

    private fun addMemberInfo(memberId: Long, model: Model) {
        val member = memberReader.readMemberById(memberId)
        model.addAttribute("member", member)
    }

    private fun addHeartsStats(
        memberId: Long,
        postIds: List<Long>,
        model: Model,
    ) {
        val hearts = postHeartReader.findByMemberIdAndPostIds(memberId, postIds)
            .map(PostHeart::postId)
        model.addAttribute("hearts", hearts)
    }

    private fun addMemberStats(model: Model, memberId: Long) {
        try {
            val followStats = followReader.getFollowStats(memberId)
            model.addAttribute("followStats", followStats)

            val postCount = postReader.countPostsByMemberId(memberId)
            model.addAttribute("postCount", postCount)
        } catch (e: IllegalArgumentException) {
            model.addAttribute("followStats", createDefaultFollowStats(memberId))
            model.addAttribute("postCount", 0L)
        }
    }

    private fun addSuggestedMembers(model: Model, memberId: Long): List<Member> {
        try {
            val suggestedUsers = memberReader.findSuggestedMembers(memberId, 5)
            model.addAttribute("suggestedUsers", suggestedUsers)
            return suggestedUsers
        } catch (e: IllegalArgumentException) {
            model.addAttribute("suggestedUsers", emptyList<Member>())
            return emptyList()
        }
    }

    private fun addFollowingStatus(model: Model, followerId: Long, suggestedIds: List<Long>) {
        val followings = followReader.findFollowings(followerId, suggestedIds)
        model.addAttribute("followings", followings)
    }

    private fun createDefaultFollowStats(memberId: Long): FollowStatsResponse {
        return FollowStatsResponse(
            memberId = memberId,
            followersCount = 0L,
            followingCount = 0L
        )
    }
}
