package com.example.realtimechatapp.ui.navigation

import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText

sealed class Screen(
    val route: String,
    val title: UiText? = null,
    val icon: Int? = null
) {
    object Login: Screen("login")
    object Signup: Screen("signup")
    object Search: Screen("search")

    // screens using bottom navigation bar and top app bar
    object Messages: Screen("messages", UiText.StringResource(R.string.messages), R.drawable.ic_message)
    object Groups: Screen("group", UiText.StringResource(R.string.groups), R.drawable.ic_group)
    object Profile: Screen("profile", UiText.StringResource(R.string.account), R.drawable.ic_profile)
    object More: Screen("more", UiText.StringResource(R.string.more), R.drawable.ic_menu)

    // screens using message top app bar
    object DetailMessage: Screen("detail_message/{friendId}", UiText.StringResource(R.string.messages)){
        const val ARG_FRIEND_ID = "friendId"
        fun createRoute(friendId: String) = "detail_message/$friendId"
    }
    object DetailGroup: Screen("detail_group/{groupId}", UiText.StringResource(R.string.groups)){
        const val ARG_GROUP_ID = "groupId"
        fun createRoute(groupId: String) = "detail_group/$groupId"
    }

    // screens using normal top app bar
    object MessageAction: Screen("message_action/{friendId}", UiText.StringResource(R.string.actions)){
        const val ARG_FRIEND_ID = "friendId"
        fun createRoute(friendId: String) = "message_action/$friendId"
    }

    object GroupMessageAction: Screen("group_message_action/{groupId}", UiText.StringResource(R.string.actions)){
        const val ARG_GROUP_ID = "groupId"
        fun createRoute(groupId: String) = "group_message_action/$groupId"
    }

    object MemberManagement: Screen("member_management/{groupId}", UiText.StringResource(R.string.member)){
        const val ARG_GROUP_ID = "groupId"
        fun createRoute(groupId: String) = "member_management/$groupId"
    }

    object CreateGroup: Screen("create_group", UiText.StringResource(R.string.create_group))
}