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
import com.example.realtimechatapp.ui.screens.BeginScreen

@Composable
fun MessageScreen(
    navController: NavController,
    messageViewModel: MessageViewModel = hiltViewModel()
){
    val uiState by messageViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        messageViewModel.getUsers()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        if (uiState.isLoading){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (uiState.users.isEmpty()){
                BeginScreen(false)
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.users,
                        key = { user -> user.id}
                    ){ user ->
                        ChatItem(
                            isGroup = false,
                            avatar = user.avatar,
                            name = user.fullName,
                            unreadCount = user.unreadCount,
                            lastMessage = user.lastMessage
                        )
                    }
                }
            }
        }
    }
}