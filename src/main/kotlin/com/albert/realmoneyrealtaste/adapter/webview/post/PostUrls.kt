package com.albert.realmoneyrealtaste.adapter.webview.post

object PostUrls {
    const val CREATE = "/posts/new"
    const val READ_DETAIL = "/posts/{postId}"
    const val UPDATE = "/posts/{postId}/edit"
    const val READ_DETAIL_MODAL = "/posts/{postId}/modal"
    const val READ_MY_LIST = "/posts/mine"
    const val READ_LIST_FRAGMENT = "/posts/fragment"

    const val REDIRECT_READ_DETAIL = "redirect:/posts/%d"

    const val READ_MY_LIST_FRAGMENT = "/posts/mine/fragment"
}
