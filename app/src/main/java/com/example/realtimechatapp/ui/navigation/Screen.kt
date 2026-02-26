package com.example.realtimechatapp.ui.navigation

import com.example.realtimechatapp.R

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: Int? = null
) {
    object Login: Screen("login")
    object Signup: Screen("signup")
    object Search: Screen("search")

    // screens using bottom navigation bar and top app bar
    object Messages: Screen("messages", "Tin Nhắn", R.drawable.ic_message)
    object Groups: Screen("group", "Nhóm", R.drawable.ic_group)
    object Profile: Screen("profile", "Tài Khoản", R.drawable.ic_profile)
    object More: Screen("more", "Khác", R.drawable.ic_menu)
}