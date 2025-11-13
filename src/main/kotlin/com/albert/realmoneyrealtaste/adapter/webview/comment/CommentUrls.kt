package com.albert.realmoneyrealtaste.adapter.webview.comment

object CommentUrls {
    const val COMMENT_UPDATE = "/comments/{commentId}"
    const val COMMENT_CREATE = "/posts/{postId}/comments"
    const val COMMENTS_FRAGMENTS_LIST = "/posts/{postId}/comments/fragments/list"
    const val REPLIES_FRAGMENTS_LIST = "/comments/{commentId}/replies/fragments/list"
    const val MODAL_COMMENTS_FRAGMENTS_LIST = "/posts/{postId}/comments/modal-fragments/list"
    const val MODAL_REPLIES_FRAGMENT = "/comments/{commentId}/modal-replies-fragment"
}
