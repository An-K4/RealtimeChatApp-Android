package com.example.realtimechatapp.ui.screens.groups

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
fun GroupScreen(
    navController: NavController,
    groupViewModel: GroupViewModel = hiltViewModel()
){
    val uiState by groupViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        groupViewModel.getGroup()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        if (uiState.isLoading){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (uiState.groups.isEmpty()){
                BeginScreen(true)
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.groups,
                        key = { group -> group.id}
                    ){ group ->
                        ChatItem(
                            isGroup = true,
                            avatar = group.avatar,
                            name = group.name,
                            unreadCount = group.unreadCount,
                            lastMessage = group.lastMessage
                        )
                    }
                }
            }
        }
    }
}