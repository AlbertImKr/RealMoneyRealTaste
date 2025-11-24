package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionCreator
import com.albert.realmoneyrealtaste.application.collection.provided.CollectionReader
import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertFailsWith

class CollectionDeleteApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var collectionCreator: CollectionCreator

    @Autowired
    private lateinit var collectionReader: CollectionReader

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `deleteCollection - success - deletes own collection`() {
        val owner = testMemberHelper.getDefaultMember()

        // 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "삭제할 컬렉션",
                description = "이 컬렉션은 삭제될 것입니다",
                ownerMemberId = owner.requireId(),
                ownerName = owner.nickname.value
            )
        )

        // 삭제 전 확인
        collectionReader.readById(collection.requireId())

        // 컬렉션 삭제
        mockMvc.perform(
            delete("/api/collections/${collection.requireId()}")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().string(""))

        // 삭제 후 확인
        assertFailsWith<IllegalArgumentException> {
            collectionReader.readById(collection.requireId())
        }
    }

    @Test
    fun `deleteCollection - forbidden - when not authenticated`() {
        mockMvc.perform(
            delete("/api/collections/1")
                .with(csrf())
        )
            .andExpect(status().isForbidden())
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `deleteCollection - failure - when trying to delete other's collection`() {
        val owner = testMemberHelper.createActivatedMember("other@user.com")

        // 다른 사용자의 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "다른 사람의 컬렉션",
                description = "이 컬렉션은 삭제할 수 없습니다",
                ownerMemberId = owner.requireId(),
                ownerName = owner.nickname.value,
            )
        )

        // 다른 사람의 컬렉션 삭제 시도
        mockMvc.perform(
            delete("/api/collections/${collection.requireId()}")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("컬렉션 삭제 중 오류가 발생했습니다: 컬렉션을 삭제할 수 없습니다."))
    }

    @WithMockMember
    @Test
    fun `deleteCollection - failure - when collection does not exist`() {
        mockMvc.perform(
            delete("/api/collections/9999")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("컬렉션 삭제 중 오류가 발생했습니다: 컬렉션을 삭제할 수 없습니다."))
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `deleteCollection - success - can delete private collections`() {
        val owner = testMemberHelper.getDefaultMember()

        // 비공개 컬렉션 생성
        val collection = collectionCreator.createCollection(
            CollectionCreateCommand(
                name = "비공개 컬렉션",
                description = "비공개 컬렉션입니다",
                ownerMemberId = owner.requireId(),
                ownerName = owner.nickname.value,
            )
        )

        // 비공개 컬렉션 삭제
        mockMvc.perform(
            delete("/api/collections/${collection.requireId()}")
                .with(csrf())
        )
            .andExpect(status().isOk)

        // 삭제 확인
        assertFailsWith<IllegalArgumentException> {
            collectionReader.readById(collection.requireId())
        }
    }

    @WithMockMember
    @Test
    fun `deleteCollection - failure - when collection ID is invalid`() {
        mockMvc.perform(
            delete("/api/collections/0")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(false))
    }

    @WithMockMember
    @Test
    fun `deleteCollection - failure - when collection ID is negative`() {
        mockMvc.perform(
            delete("/api/collections/-1")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(false))
    }
}
