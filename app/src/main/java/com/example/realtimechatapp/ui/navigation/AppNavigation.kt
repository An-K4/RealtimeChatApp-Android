package com.example.realtimechatapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.BottomNavBar
import com.example.realtimechatapp.ui.components.MainTopAppBar
import com.example.realtimechatapp.ui.components.MessageTopAppBar
import com.example.realtimechatapp.ui.screens.auth.LoginScreen
import com.example.realtimechatapp.ui.screens.auth.SignupScreen
import com.example.realtimechatapp.ui.screens.groups.CreateGroupScreen
import com.example.realtimechatapp.ui.screens.groups.DetailGroupScreen
import com.example.realtimechatapp.ui.screens.groups.GroupScreen
import com.example.realtimechatapp.ui.screens.messages.DetailMessageScreen
import com.example.realtimechatapp.ui.screens.messages.MessageScreen
import com.example.realtimechatapp.ui.screens.more.MoreScreen
import com.example.realtimechatapp.ui.screens.profile.ProfileScreen
import com.example.realtimechatapp.ui.screens.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
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

    val showNormalTopBar = currentRoute in listOf(
        Screen.CreateGroup.route
    )

    Scaffold(
        topBar = {
            when {
                showMainBars -> {
                    MainTopAppBar(
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onCreateGroupClick = {
                            navController.navigate(Screen.CreateGroup.route)
                        }
                    )
                }

                showMessageTopAppBar -> {
                    // add pair to specific more screen in the future
                    val title = when (currentRoute) {
                        Screen.DetailGroup.route -> UiText.StringResource(R.string.groups)
                            .asString()

                        else -> UiText.StringResource(R.string.messages).asString()
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

                showNormalTopBar -> {
                    val title = when (currentRoute) {
                        Screen.CreateGroup.route -> UiText.StringResource(R.string.create_group).asString()
                        else -> ""
                    }

                    TopAppBar(
                        title = {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "back"
                                )
                            }
                        },
                        colors = TopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        },
        bottomBar = {
            if (showMainBars) {
                BottomNavBar(navController, currentRoute ?: "")
            }
        },
        modifier = Modifier.imePadding()
        // imePadding() here so the keyboard pushes the layout up correctly (especially for message input)
        // adjustResize doesn't work with edge-to-edge (1 day to find that)
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
                    navArgument(Screen.DetailMessage.ARG_FRIEND_ID) { type = NavType.StringType }
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
                    navArgument(Screen.DetailGroup.ARG_GROUP_ID) { type = NavType.StringType }
                )
            ) {
                DetailGroupScreen(navController)
            }

            composable(Screen.CreateGroup.route) {
                CreateGroupScreen(navController)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }

            composable(Screen.More.route) {
                MoreScreen(navController)
            }

            composable(Screen.Search.route) {
                SearchScreen(navController)
            }
        }
    }
}