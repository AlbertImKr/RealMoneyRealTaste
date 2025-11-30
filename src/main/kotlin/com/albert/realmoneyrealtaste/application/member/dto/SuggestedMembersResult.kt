package com.albert.realmoneyrealtaste.application.member.dto

import com.albert.realmoneyrealtaste.domain.member.Member

/**
 * 추천 회원 조회 결과
 */
data class SuggestedMembersResult(
    val suggestedUsers: List<Member>,
    val followingIds: List<Long>,
)
