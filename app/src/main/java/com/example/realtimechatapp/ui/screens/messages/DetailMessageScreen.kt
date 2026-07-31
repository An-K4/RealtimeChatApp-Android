package com.example.realtimechatapp.ui.screens.messages

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.FullScreenImagePreviewDialog
import com.example.realtimechatapp.ui.components.WelcomePlaceholder
import com.example.realtimechatapp.ui.components.ContactHeader
import com.example.realtimechatapp.ui.components.MessageInput
import com.example.realtimechatapp.ui.components.MessageRenderItem
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import java.io.File

@Composable
fun DetailMessageScreen(
    navController: NavController,
    detailMessageViewModel: DetailMessageViewModel = hiltViewModel()
) {
    val detailMessageState by detailMessageViewModel.detailMessageState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Camera URI state
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { detailMessageViewModel.onSelectedMediaChange(it) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            detailMessageViewModel.onSelectedMediaChange(cameraImageUri!!)
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Tạo URI cho camera
            val photoFile = File(context.cacheDir, "camera/IMG_${System.currentTimeMillis()}.jpg")
            photoFile.parentFile?.mkdirs()
            cameraImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(cameraImageUri!!)
        } else {
            Toast.makeText(context, UiText.StringResource(R.string.camera_permission_denied).asString(context), Toast.LENGTH_SHORT).show()
        }
    }

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
                                    attachments = item.attachments,
                                    time = item.createdAt,
                                    isSeen = item.seenUserIds?.isNotEmpty() == true,
                                    isGroup = false,
                                    fromCurrentUser = item.senderId == detailMessageState.currentUserId,
                                    onImageClick = { imageUrl ->
                                        detailMessageViewModel.showImagePreview(
                                            imageModel = imageUrl,
                                            allowClear = false
                                        )
                                    }
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
            selectedImageUri = detailMessageState.selectedImageUri,
            isSending = detailMessageState.isSending,
            onMessageTextChange = { detailMessageViewModel.onMessageInputChange(it) },
            onCameraClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            },
            onPreviewClick = {
                detailMessageState.selectedImageUri?.let { uri ->
                    detailMessageViewModel.showImagePreview(
                        imageModel = uri,
                        allowClear = true
                    )
                }
            },
            onSendClick = { detailMessageViewModel.sendMessage() }
        )
    }

    // Render FullScreenImagePreviewDialog
    when (val dialogState = detailMessageState.dialogState) {
        is DetailMessageViewModel.ImagePreviewDialogState.Show -> {
            FullScreenImagePreviewDialog(
                imageModel = dialogState.imageModel,
                onDismiss = { detailMessageViewModel.dismissImagePreview() },
                onClear = if (dialogState.allowClear) {
                    { detailMessageViewModel.clearAndDismissImagePreview() }
                } else null
            )
        }
        DetailMessageViewModel.ImagePreviewDialogState.Dismiss -> {
            // Do nothing
        }
    }
}