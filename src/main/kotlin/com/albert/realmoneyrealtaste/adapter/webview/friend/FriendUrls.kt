package com.albert.realmoneyrealtaste.adapter.webview.friend

/**
 * Friends 관련 URL 상수
 */
object FriendUrls {
    const val FRIENDS = "/friends"
    const val FRIEND_WIDGET = "/members/{memberId}/friends/widget/fragment"
    const val FRIEND_REQUESTS_FRAGMENT = "/friends/requests/fragment"
    const val SEND_FRIEND_REQUEST = "/friendships"
    const val RESPOND_TO_FRIEND_REQUEST = "/friendships/{friendshipId}"
    const val UNFRIEND = "/friendships/{friendshipId}"
}
