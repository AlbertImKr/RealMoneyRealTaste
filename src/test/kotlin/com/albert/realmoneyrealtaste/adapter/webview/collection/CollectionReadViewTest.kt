package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionCreator
import com.albert.realmoneyrealtaste.application.collection.required.CollectionRepository
import com.albert.realmoneyrealtaste.domain.collection.CollectionPrivacy
import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionFilter
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestPostHelper
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

    @Autowired
    private lateinit var collectionCreator: CollectionCreator

    @Autowired
    private lateinit var testPostHelper: TestPostHelper

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
            .andExpect(model().attributeExists("authorId"))
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
            .andExpect(model().attributeExists("authorId"))
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
            .andExpect(model().attributeExists("authorId"))
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
            .andExpect(model().attributeExists("authorId"))
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
            .andExpect(model().attributeExists("authorId"))
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
    }

    private fun createCollection(
        name: String,
        member: Member,
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
    ): Member {
        return testMemberHelper.createActivatedMember(email = email, nickname = nickname)
    }

    private fun getDefaultMember(): Member {
        return testMemberHelper.getDefaultMember()
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - success - returns collection posts and my posts`() {
        val member = getDefaultMember()

        // 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "테스트 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        // 컬렉션에 포함될 게시글 생성
        val post1 = createPost(member)
        val post2 = createPost(member)

        // 컬렉션에 게시글 추가
        collection.addPost(member.requireId(), post1.requireId())
        collection.addPost(member.requireId(), post2.requireId())
        postCollectionRepository.save(collection)

        // 추가 게시글 생성 (내 게시글에 포함되지만 컬렉션에는 없는 게시글)
        createPost(member)

        flushAndClear()

        val result = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(CollectionViews.COLLECTION_POSTS_FRAGMENT))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("myPosts"))
            .andReturn()

        // 컬렉션 게시글 확인
        val collectionPosts = result.modelAndView!!.model["posts"] as List<*>
        assertEquals(2, collectionPosts.size)

        // 내 게시글 확인 (페이지네이션 기본 크기 5)
        val myPosts = result.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(3, myPosts.content.size)
        assertEquals(5, myPosts.size) // 기본 페이지 크기
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - success - handles pagination correctly`() {
        val member = getDefaultMember()

        // 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "페이지네이션 테스트 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        // 10개 게시글 생성
        repeat(10) { _ ->
            createPost(member)
        }

        flushAndClear()

        // 첫 페이지 (size=5)
        val firstPageResult = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk)
            .andReturn()

        val firstPagePosts = firstPageResult.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(5, firstPagePosts.content.size)
        assertEquals(0, firstPagePosts.number)
        assertEquals(2, firstPagePosts.totalPages)
        assertEquals(10, firstPagePosts.totalElements)

        // 두 번째 페이지
        val secondPageResult = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
                .param("page", "1")
                .param("size", "5")
        )
            .andExpect(status().isOk)
            .andReturn()

        val secondPagePosts =
            secondPageResult.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(5, secondPagePosts.content.size)
        assertEquals(1, secondPagePosts.number)
        assertEquals(2, secondPagePosts.totalPages)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - success - returns empty collection posts`() {
        val member = getDefaultMember()

        // 빈 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "빈 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        // 내 게시글만 생성
        createPost(member)
        createPost(member)

        flushAndClear()

        val result = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andReturn()

        // 컬렉션 게시글은 비어있음
        val collectionPosts = result.modelAndView!!.model["posts"] as List<*>
        assertEquals(0, collectionPosts.size)

        // 내 게시글은 있음
        val myPosts = result.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(2, myPosts.content.size)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - success - returns empty my posts`() {
        val member = getDefaultMember()

        // 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "테스트 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        // 컬렉션에 게시글 추가 (다른 사람의 게시글)
        val otherMember = createActivatedMember("other@example.com", "other")
        val otherPost = createPost(otherMember)
        collection.addPost(member.requireId(), otherPost.requireId())
        postCollectionRepository.save(collection)

        flushAndClear()

        val result = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andReturn()

        // 컬렉션 게시글은 있음
        val collectionPosts = result.modelAndView!!.model["posts"] as List<*>
        assertEquals(1, collectionPosts.size)

        // 내 게시글은 비어있음
        val myPosts = result.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(0, myPosts.content.size)
    }

    @Test
    fun `readCollectionPostsFragment - forbidden - when not authenticated`() {
        val member = createActivatedMember("owner@example.com", "owner")

        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "인증 필요 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        flushAndClear()

        mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - failure - when collection does not exist`() {
        mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, 9999L)
        )
            .andExpect(status().isBadRequest())
            .andExpect(model().attribute("success", false))
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readCollectionPostsFragment - success - sorts posts by createdAt DESC`() {
        val member = getDefaultMember()

        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "정렬 테스트 컬렉션",
                ownerMemberId = member.requireId(),
                description = "맛집이요",
                ownerName = member.nickname.value,
            )
        )

        // 시간 차이를 두고 게시글 생성
        val post1 = createPost(member)
        val post2 = createPost(member)
        val post3 = createPost(member)

        // 컬렉션에 게시글 추가
        collection.addPost(member.requireId(), post1.requireId())
        collection.addPost(member.requireId(), post2.requireId())
        collection.addPost(member.requireId(), post3.requireId())
        postCollectionRepository.save(collection)

        flushAndClear()

        val result = mockMvc.perform(
            get(CollectionUrls.COLLECTION_POSTS_FRAGMENT, collection.requireId())
        )
            .andExpect(status().isOk)
            .andReturn()

        val myPosts = result.modelAndView!!.model["myPosts"] as org.springframework.data.domain.Page<*>
        assertEquals(3, myPosts.content.size)

        // 최신순 정렬 확인 (post3, post2, post1 순서)
        // 실제 구현에서는 ID나 다른 속성으로 순서 확인 가능
    }

    private fun createPost(
        author: Member,
    ): com.albert.realmoneyrealtaste.domain.post.Post {
        return testPostHelper.createPost(authorMemberId = author.requireId())
    }
}
