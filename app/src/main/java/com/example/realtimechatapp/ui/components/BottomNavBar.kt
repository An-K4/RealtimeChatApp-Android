package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import com.example.realtimechatapp.ui.theme.RealtimeGreen

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String
) {
    val items = listOf(
        Screen.Messages,
        Screen.Groups,
        Screen.Profile,
        Screen.More
    )

    NavigationBar(containerColor = RealtimeGreen) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon!!),
                        contentDescription = item.title?.asString()
                    )
                },
                label = { Text(item.title!!.asString()) },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIndicatorColor = MaterialTheme.colorScheme.background,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    disabledIconColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledTextColor = MaterialTheme.colorScheme.primaryContainer
                ),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Messages.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavBar() {
    val items = listOf(
        Screen.Messages,
        Screen.Groups,
        Screen.Profile,
        Screen.More
    )

    RealtimeChatAppTheme {
        NavigationBar(
            containerColor = RealtimeGreen
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(item.icon!!),
                            contentDescription = item.title?.asString()
                        )
                    },
                    colors = NavigationBarItemColors(
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.background,
                        selectedIndicatorColor = MaterialTheme.colorScheme.background,
                        unselectedIconColor = MaterialTheme.colorScheme.background,
                        unselectedTextColor = MaterialTheme.colorScheme.background,
                        disabledIconColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    label = { Text(item.title!!.asString()) },
                    selected = item.route == Screen.Messages.route,
                    onClick = {}
                )
            }
        }
    }
}