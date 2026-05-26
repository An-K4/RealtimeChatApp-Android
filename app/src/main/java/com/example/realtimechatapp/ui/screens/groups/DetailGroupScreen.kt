package com.example.realtimechatapp.ui.screens.groups

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.ui.components.WelcomePlaceholder
import com.example.realtimechatapp.ui.components.ContactHeader
import com.example.realtimechatapp.ui.components.MessageInput
import com.example.realtimechatapp.ui.components.MessageRenderItem

@Composable
fun DetailGroupScreen(
    navController: NavController,
    detailGroupViewModel: DetailGroupViewModel = hiltViewModel()
) {
    val detailGroupState by detailGroupViewModel.detailGroupState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            detailGroupViewModel.detailGroupEvent.collect { event ->
                when (event) {
                    is DetailGroupViewModel.DetailGroupEvent.Success -> {
                        Toast.makeText(
                            context,
                            R.string.get_group_messages_success_notification,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DetailGroupViewModel.DetailGroupEvent.Failure -> {
                        Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(detailGroupState.groupMessages.size) {
        if (detailGroupState.groupMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // in development
        ContactHeader(
            avatarContactPreview = detailGroupState.groupAvatar,
            contactName = detailGroupState.groupName ?: "",
            contactAdditionalInfo = detailGroupState.groupStatus,
            onVideoCallClick = {},
            onVoiceCallClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (detailGroupState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (detailGroupState.groupMessages.isEmpty()) {
                    WelcomePlaceholder(isGroup = true, inDetailScreen = true)
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                        items(
                            items = detailGroupState.groupMessages,
                            key = { groupMessage -> groupMessage.id }
                        ) { groupMessage ->
                            MessageRenderItem(
                                senderAvatar = groupMessage.senderAvatar,
                                senderName = groupMessage.senderName,
                                message = groupMessage.content ?: "",
                                time = groupMessage.createdAt,
                                isSeen = groupMessage.seenUserIds != null,
                                isGroup = true,
                                fromCurrentUser = groupMessage.senderId == detailGroupState.currentUserId
                            )
                        }
                    }
                }
            }
        }

        MessageInput(
            messageText = detailGroupState.messageInput ?: "",
            onMessageTextChange = { detailGroupViewModel.onGroupMessageInputChange(it) },
            onCameraClick = {},
            onGalleryClick = {},
            onSendClick = { detailGroupViewModel.sendGroupMessage() }
        )
    }
}