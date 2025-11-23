package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.application.post.exception.PostCreateException
import com.albert.realmoneyrealtaste.application.post.exception.PostUpdateException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import kotlin.test.assertEquals

class PostExceptionHandlerTest {

    private lateinit var postExceptionHandler: PostExceptionHandler

    @Mock
    private lateinit var redirectAttributes: RedirectAttributes

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        postExceptionHandler = PostExceptionHandler()
    }

    @Test
    @DisplayName("PostUpdateException 핸들링 - 현재 URL로 리디렉션 및 에러 메시지 설정")
    fun `handlePostUpdateException - success - redirects to current URL with error message`() {
        val exception = PostUpdateException("수정 실패", RuntimeException("원인 예외"))
        val currentPath = "/posts/1/edit"
        val request = MockHttpServletRequest(HttpMethod.GET.name(), currentPath)

        val result = postExceptionHandler.handlePostUpdateException(exception, redirectAttributes, request)

        assertEquals("redirect:$currentPath", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostUpdateException 핸들링 - 쿼리 파라미터가 있는 URL")
    fun `handlePostUpdateException - success - handles URL with query parameters`() {
        val exception = PostUpdateException("수정 실패", RuntimeException("원인 예외"))
        val currentPath = "/posts/1/edit?param=value"
        val request = MockHttpServletRequest(HttpMethod.GET.name(), currentPath)

        val result = postExceptionHandler.handlePostUpdateException(exception, redirectAttributes, request)

        assertEquals("redirect:$currentPath", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostUpdateException 핸들링 - 다른 도메인 URL")
    fun `handlePostUpdateException - success - handles different domain URL`() {
        val exception = PostUpdateException("수정 실패", RuntimeException("원인 예외"))
        val currentPath = "/posts/1/edit"
        val request = MockHttpServletRequest(HttpMethod.GET.name(), currentPath)

        val result = postExceptionHandler.handlePostUpdateException(exception, redirectAttributes, request)

        assertEquals("redirect:$currentPath", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostCreateException 핸들링 - 생성 페이지로 리디렉션 및 에러 메시지 설정")
    fun `handlePostCreateException - success - redirects to create page with error message`() {
        val exception = PostCreateException("생성 실패", RuntimeException("원인 예외"))

        val result = postExceptionHandler.handlePostCreateException(exception, redirectAttributes)

        assertEquals("redirect:/posts/new", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 작성에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostCreateException 핸들링 - 다른 메시지를 가진 예외")
    fun `handlePostCreateException - success - handles exception with different message`() {
        val exception = PostCreateException("다른 생성 실패 메시지", IllegalStateException("다른 원인"))

        val result = postExceptionHandler.handlePostCreateException(exception, redirectAttributes)

        assertEquals("redirect:/posts/new", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 작성에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostUpdateException 핸들링 - 루트 경로 URL")
    fun `handlePostUpdateException - success - handles root path URL`() {
        val exception = PostUpdateException("수정 실패", RuntimeException("원인 예외"))
        val currentPath = "/"
        val request = MockHttpServletRequest(HttpMethod.POST.name(), currentPath)

        val result = postExceptionHandler.handlePostUpdateException(exception, redirectAttributes, request)

        assertEquals("redirect:$currentPath", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")
    }

    @Test
    @DisplayName("PostUpdateException 핸들링 - 복잡한 경로 URL")
    fun `handlePostUpdateException - success - handles complex path URL`() {
        val exception = PostUpdateException("수정 실패", RuntimeException("원인 예외"))
        val currentPath = "/members/123/posts/456/edit"
        val request = MockHttpServletRequest(HttpMethod.POST.name(), currentPath)

        val result = postExceptionHandler.handlePostUpdateException(exception, redirectAttributes, request)

        assertEquals("redirect:$currentPath", result)

        verify(redirectAttributes).addFlashAttribute("success", false)
        verify(redirectAttributes).addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")
    }
}
