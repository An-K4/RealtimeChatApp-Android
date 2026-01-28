package com.example.realtimechatapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.realtimechatapp.ui.screens.auth.LoginScreen
import com.example.realtimechatapp.ui.screens.home.HomeScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route){
        composable(Screen.Login.route){
            LoginScreen(navController)
        }

        composable(Screen.Home.route){
            HomeScreen(navController)
        }
    }
}