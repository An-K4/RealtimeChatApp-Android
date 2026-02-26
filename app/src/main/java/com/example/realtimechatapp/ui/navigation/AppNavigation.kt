package com.example.realtimechatapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.realtimechatapp.ui.components.BottomNavBar
import com.example.realtimechatapp.ui.components.MainTopAppBar
import com.example.realtimechatapp.ui.screens.auth.LoginScreen
import com.example.realtimechatapp.ui.screens.auth.SignupScreen
import com.example.realtimechatapp.ui.screens.groups.GroupScreen
import com.example.realtimechatapp.ui.screens.messages.MessageScreen
import com.example.realtimechatapp.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute in listOf(
        Screen.Messages.route,
        Screen.Groups.route,
        Screen.Profile.route,
        Screen.More.route
    )

    Scaffold(
        topBar = {
            if (showBars){
                MainTopAppBar(
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
        },
        bottomBar = {
            if (showBars){
                BottomNavBar(navController, currentRoute ?: "")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(Screen.Login.route){
                LoginScreen(navController)
            }

            composable(Screen.Signup.route){
                SignupScreen(navController)
            }

            composable(Screen.Messages.route){
                MessageScreen(navController)
            }

            composable(Screen.Groups.route){
                GroupScreen(navController)
            }

            composable(Screen.Profile.route){
                ProfileScreen(navController)
            }

            composable(Screen.More.route){
                MessageScreen(navController)
            }
        }
    }
}