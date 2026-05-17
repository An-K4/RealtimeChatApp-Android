package com.example.realtimechatapp.ui.screens.messages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.WelcomePlaceholder
import com.example.realtimechatapp.ui.components.ContactHeader
import com.example.realtimechatapp.ui.components.MessageInput
import com.example.realtimechatapp.ui.components.MessageRenderItem
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import timber.log.Timber

@Composable
fun DetailMessageScreen(
    navController: NavController,
    detailMessageViewModel: DetailMessageViewModel = hiltViewModel()
) {
    val detailMessageState by detailMessageViewModel.detailMessageState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            detailMessageViewModel.detailMessageEvent.collect { event ->
                when (event) {
                    is DetailMessageViewModel.DetailMessageEvent.GetMessageSuccess -> {
                        Toast.makeText(
                            context,
                            R.string.get_messages_success_notification,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DetailMessageViewModel.DetailMessageEvent.Failure -> {
                        Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(detailMessageState.messages.size) {
        if (detailMessageState.messages.isNotEmpty()) {
            val hasUnseenMessages = detailMessageState.messages.any { message ->
                message.senderId == detailMessageState.friendId
                        && message.seenUserIds?.contains(detailMessageState.currentUserId) != true
            }

            if (hasUnseenMessages) {
                Timber.d("Có tin nhắn chưa đọc, gọi mark message as seen.")
                detailMessageViewModel.markMessageAsSeen()
            }

            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // in development
        ContactHeader(
            avatarContactPreview = detailMessageState.friendAvatar,
            contactName = detailMessageState.friendName ?: "",
            contactAdditionalInfo = detailMessageState.friendStatus,
            onVideoCallClick = {},
            onVoiceCallClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (detailMessageState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (detailMessageState.messages.isEmpty()) {
                    WelcomePlaceholder(isGroup = false, inDetailScreen = true)
                } else {
                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                        LazyColumn(
                            state = listState,
                            reverseLayout = true,
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = detailMessageState.messages,
                                key = { message -> message.id }
                            ) { item ->
                                MessageRenderItem(
                                    senderAvatar = item.senderAvatar,
                                    senderName = item.senderName,
                                    message = item.content ?: "",
                                    time = item.createdAt,
                                    isSeen = item.seenUserIds?.isNotEmpty() == true,
                                    isGroup = false,
                                    fromCurrentUser = item.senderId == detailMessageState.currentUserId
                                )
                            }
                        }

                        if (detailMessageState.friendTypingStatus) {
                            Text(
                                text = UiText.StringResource(R.string.sb_is_typing, detailMessageState.friendName).asString(),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start,
                                color = RealtimeGreen,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(0.5f),
                                        shape = RoundedCornerShape(
                                            topStart = 0.dp,
                                            topEnd = 10.dp,
                                            bottomStart = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                    )
                                    .padding(horizontal = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        MessageInput(
            messageText = detailMessageState.messageInput ?: "",
            onMessageTextChange = { detailMessageViewModel.onMessageInputChange(it) },
            onCameraClick = {},
            onGalleryClick = {},
            onSendClick = { detailMessageViewModel.sendMessage() }
        )
    }
}