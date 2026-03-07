package com.example.realtimechatapp.ui.screens.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.ui.components.ChatItem
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.components.BeginScreen

@Composable
fun MessageScreen(
    navController: NavController,
    messageViewModel: MessageViewModel = hiltViewModel()
){
    val messageState by messageViewModel.messageState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        messageViewModel.getUsers()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        if (messageState.isLoading){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (messageState.users.isEmpty()){
                BeginScreen(isGroup = false, inDetailScreen = false)
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = messageState.users,
                        key = { user -> user.id}
                    ){ user ->
                        ChatItem(
                            isGroup = false,
                            avatar = user.avatar,
                            name = user.fullName,
                            unreadCount = user.unreadCount,
                            lastMessage = user.lastMessage,
                            onItemClicked = {
                                navController.navigate(Screen.DetailMessage.createRoute(user.id))
                            }
                        )
                    }
                }
            }
        }
    }
}