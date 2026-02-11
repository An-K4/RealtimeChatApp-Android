package com.example.realtimechatapp.ui.navigation

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object Signup: Screen("signup")
    object Home: Screen("home")
    object Chat: Screen("chat")
    object Account: Screen("account")
}