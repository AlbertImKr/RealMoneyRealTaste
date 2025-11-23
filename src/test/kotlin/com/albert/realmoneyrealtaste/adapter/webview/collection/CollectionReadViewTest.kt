package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionFilter
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionReadViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var postCollectionRepository: CollectionRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMyCollections - success - returns all collections when filter is null`() {
        val member = getDefaultMember()

        // 여러 컬렉션 생성
        createCollection("컬렉션1", member)
        createCollection("컬렉션2", member, isPublic = false)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.MY_LIST_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.MY_LIST))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("author"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMyCollections - success - returns public collections when filter is PUBLIC`() {
        val member = getDefaultMember()

        // 공개 컬렉션과 비공개 컬렉션 생성
        createCollection("공개 컬렉션", member, isPublic = true)
        createCollection("비공개 컬렉션", member, isPublic = false)

        val result = mockMvc.perform(
            get(CollectionUrls.MY_LIST_FRAGMENT)
                .param("filter", CollectionFilter.PUBLIC.name)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.MY_LIST))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // PUBLIC 필터 시 공개 컬렉션만 반환되는지 확인
        val collections = result.modelAndView!!.model["collections"] as org.springframework.data.domain.Page<*>
        assertEquals(1, collections.content.size)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMyCollections - success - returns all collections when filter is ALL`() {
        val member = getDefaultMember()

        // 공개 컬렉션과 비공개 컬렉션 생성
        createCollection("공개 컬렉션", member, isPublic = true)
        createCollection("비공개 컬렉션", member, isPublic = false)
        flushAndClear()

        val result = mockMvc.perform(
            get(CollectionUrls.MY_LIST_FRAGMENT)
                .param("filter", CollectionFilter.ALL.name)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.MY_LIST))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // ALL 필터 시 모든 컬렉션이 반환되는지 확인
        val collections = result.modelAndView!!.model["collections"] as org.springframework.data.domain.Page<*>
        assertEquals(2, collections.content.size)
    }

    @Test
    fun `readMyCollections - forbidden - when not authenticated`() {
        mockMvc.perform(
            get(CollectionUrls.MY_LIST_FRAGMENT)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    fun `readMyCollections - forbidden - when not authenticated with filter`() {
        mockMvc.perform(
            get(CollectionUrls.MY_LIST_FRAGMENT)
                .param("filter", CollectionFilter.PUBLIC.name)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionDetailEditFragment - success - returns collection edit fragment`() {
        val member = getDefaultMember()
        val collection = createCollection("수정할 컬렉션", member)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.DETAIL_EDIT_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.DETAIL_EDIT_FRAGMENT))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionDetailFragment - success - returns collection detail fragment`() {
        val member = getDefaultMember()
        val collection = createCollection("상세 컬렉션", member)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.DETAIL_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.DETAIL_FRAGMENT))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `writeCollection - forbidden - when not authenticated`() {
        mockMvc.perform(
            get(CollectionUrls.WRITE_FRAGMENT)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `writeCollection - forbidden - when authenticated`() {
        mockMvc.perform(
            get(CollectionUrls.WRITE_FRAGMENT)
        )
            .andExpect(status().isOk())
            .andExpect(view().name(CollectionViews.WRITE_FRAGMENT))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMemberCollections - success - returns member collections when authenticated`() {
        val otherMember = createActivatedMember("other@example.com", "other")

        createCollection("다른 사용자 컬렉션1", otherMember)
        createCollection("다른 사용자 컬렉션2", otherMember)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.MEMBER_COLLECTIONS_FRAGMENT, otherMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.MY_LIST))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("author"))
    }

    @Test
    fun `readMemberCollections - success - returns member collections without authentication`() {
        val otherMember = createActivatedMember("other@example.com", "other")

        createCollection("공개 컬렉션", otherMember)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.MEMBER_COLLECTIONS_FRAGMENT, otherMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.MY_LIST))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeDoesNotExist("member"))
            .andExpect(model().attributeExists("author"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMemberCollectionDetailFragment - success - returns member collection detail when authenticated`() {
        val otherMember = createActivatedMember("other@example.com", "other")
        val collection = createCollection("다른 사용자 상세 컬렉션", otherMember)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.MEMBER_COLLECTION_DETAIL_FRAGMENT, otherMember.requireId(), collection.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.DETAIL_FRAGMENT))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("author"))
    }

    @Test
    fun `readMemberCollectionDetailFragment - success - returns member collection detail without authentication`() {
        val otherMember = createActivatedMember("other@example.com", "other")
        val collection = createCollection("공개 상세 컬렉션", otherMember)
        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.MEMBER_COLLECTION_DETAIL_FRAGMENT, otherMember.requireId(), collection.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.DETAIL_FRAGMENT))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeDoesNotExist("member"))
            .andExpect(model().attributeExists("author"))
    }

    private fun createCollection(
        name: String,
        member: com.albert.realmoneyrealtaste.domain.member.Member,
        isPublic: Boolean = true,
    ): PostCollection {
        val command = CollectionCreateCommand(
            ownerMemberId = member.requireId(),
            name = name,
            description = "$name 에 대한 설명",
            ownerName = member.nickname.value,
            privacy = if (isPublic) CollectionPrivacy.PUBLIC else CollectionPrivacy.PRIVATE
        )
        val collection = PostCollection.create(command)
        return postCollectionRepository.save(collection)
    }

    private fun createActivatedMember(
        email: String,
        nickname: String,
    ): com.albert.realmoneyrealtaste.domain.member.Member {
        return testMemberHelper.createActivatedMember(email = email, nickname = nickname)
    }

    private fun getDefaultMember(): com.albert.realmoneyrealtaste.domain.member.Member {
        return testMemberHelper.getDefaultMember()
    }
}
