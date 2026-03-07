package com.example.realtimechatapp.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.ui.components.BeginScreen
import com.example.realtimechatapp.ui.components.ChatItem
import com.example.realtimechatapp.ui.components.ContactHeader
import com.example.realtimechatapp.ui.components.MessageInput
import com.example.realtimechatapp.ui.components.MessageRenderItem
import javax.inject.Inject

@Composable
fun DetailMessageScreen(
    navController: NavController,
    detailMessageViewModel: DetailMessageViewModel = hiltViewModel()
){
    val detailMessageState by detailMessageViewModel.detailMessageState.collectAsStateWithLifecycle()
    val dialogState by remember { mutableStateOf<DetailMessageViewModel.DetailMessageEvent?>(null) }

    LaunchedEffect(Unit) {
        detailMessageViewModel.getMessages()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        // in development
        ContactHeader(
            avatarContactPreview = null,
            contactName = "Vũ Quốc An",
            contactAdditionalInfo = "Đang hoạt động",
            onVideoCallClick = {},
            onVoiceCallClick = {}
        )

        Box(
            modifier = Modifier
                .weight(1f)
        ){
            if (detailMessageState.isLoading){
                CircularProgressIndicator()
            } else {
                if (detailMessageState.messages.isEmpty()){
                    BeginScreen(isGroup = false, inDetailScreen = true)
                } else {
                    LazyColumn(
                        state = rememberLazyListState(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.background(color = Color.Gray)
                    ) {
                        items(
                            items = detailMessageState.messages,
                            key = { message -> message.id }
                        ){ item ->
                            MessageRenderItem(
                                message = item.content?:"",
                                time = item.createdAt.formatToTime(toHourMinute = true),
                                isSeen = item.seenUserIds?.isNotEmpty() == true,
                                fromCurrentUser = item.senderId == detailMessageState.friendId
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
            onSendClick = {}
        )
    }
}