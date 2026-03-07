package com.example.realtimechatapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.realtimechatapp.ui.components.BottomNavBar
import com.example.realtimechatapp.ui.components.MainTopAppBar
import com.example.realtimechatapp.ui.components.MessageTopAppBar
import com.example.realtimechatapp.ui.screens.auth.LoginScreen
import com.example.realtimechatapp.ui.screens.auth.SignupScreen
import com.example.realtimechatapp.ui.screens.groups.DetailGroupScreen
import com.example.realtimechatapp.ui.screens.groups.GroupScreen
import com.example.realtimechatapp.ui.screens.messages.DetailMessageScreen
import com.example.realtimechatapp.ui.screens.messages.MessageScreen
import com.example.realtimechatapp.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showMainBars = currentRoute in listOf(
        Screen.Messages.route,
        Screen.Groups.route,
        Screen.Profile.route,
        Screen.More.route
    )

    val showMessageTopAppBar = currentRoute in listOf(
        Screen.DetailMessage.route,
        Screen.DetailGroup.route
    )

    Scaffold(
        topBar = {
            when{
                showMainBars -> {
                    MainTopAppBar(
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }
                showMessageTopAppBar -> {
                    // add pair to specific more screen in the future
                    val title = when(currentRoute){
                        Screen.DetailGroup.route -> "Nhóm"
                        else -> "Tin Nhắn"
                    }

                    MessageTopAppBar(
                        title = title,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onMoreClick = {

                        }
                    )
                }
            }
        },
        bottomBar = {
            if (showMainBars) {
                BottomNavBar(navController, currentRoute ?: "")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }

            composable(Screen.Signup.route) {
                SignupScreen(navController)
            }

            composable(Screen.Messages.route) {
                MessageScreen(navController)
            }

            composable(
                Screen.DetailMessage.route,
                arguments = listOf(
                    navArgument(Screen.DetailMessage.ARG_FRIEND_ID){ type = NavType.StringType }
                )
            ) {
                DetailMessageScreen(navController)
            }

            composable(Screen.Groups.route) {
                GroupScreen(navController)
            }

            composable(
                Screen.DetailGroup.route,
                arguments = listOf(
                    navArgument(Screen.DetailGroup.ARG_GROUP_ID){ type = NavType.StringType }
                )
            ) {
                DetailGroupScreen(navController)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }

            composable(Screen.More.route) {
                MessageScreen(navController)
            }
        }
    }
}