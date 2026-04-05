package com.example.realtimechatapp.ui.screens.messages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.ui.components.BeginScreen
import com.example.realtimechatapp.ui.components.ContactHeader
import com.example.realtimechatapp.ui.components.MessageInput
import com.example.realtimechatapp.ui.components.MessageRenderItem

@Composable
fun DetailMessageScreen(
    navController: NavController,
    detailMessageViewModel: DetailMessageViewModel = hiltViewModel()
){
    val detailMessageState by detailMessageViewModel.detailMessageState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(lifecycleOwner.lifecycle) {
        detailMessageViewModel.detailMessageEvent.collect{ event ->
            when(event){
                is DetailMessageViewModel.DetailMessageEvent.GetMessageSuccess -> {
                    Toast.makeText(context, "Lấy tin nhắn từ db thành công", Toast.LENGTH_SHORT).show()
                }
                is DetailMessageViewModel.DetailMessageEvent.Failure -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(detailMessageState.messages.size){
        if (detailMessageState.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        // in development
        ContactHeader(
            avatarContactPreview = detailMessageState.friendAvatar,
            contactName = detailMessageState.friendName ?: "",
            contactAdditionalInfo = detailMessageState.friendStatus ?: "",
            onVideoCallClick = {},
            onVoiceCallClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ){
            if (detailMessageState.isLoading){
                CircularProgressIndicator()
            } else {
                if (detailMessageState.messages.isEmpty()){
                    BeginScreen(isGroup = false, inDetailScreen = true)
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = Color.Gray)
                    ) {
                        items(
                            items = detailMessageState.messages,
                            key = { message -> message.id }
                        ){ item ->
                            MessageRenderItem(
                                senderAvatar = item.senderAvatar,
                                senderName = item.senderName,
                                message = item.content?:"",
                                time = item.createdAt,
                                isSeen = item.seenUserIds?.isNotEmpty() == true,
                                isGroup = false,
                                fromCurrentUser = item.senderId != detailMessageState.friendId
                            )
                        }
                    }
                }
            }
        }

        MessageInput(
            messageText = detailMessageState.messageInput?:"",
            onMessageTextChange = { detailMessageViewModel.onMessageInputChange(it) },
            onCameraClick = {},
            onGalleryClick = {},
            onSendClick = { detailMessageViewModel.sendMessage() }
        )
    }
}