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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.LifecycleEventObserver
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
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> detailMessageViewModel.onScreenEnter()
                Lifecycle.Event.ON_PAUSE -> detailMessageViewModel.onScreenExit()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val detailMessageState by detailMessageViewModel.detailMessageState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Camera URI state
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Remember stable callback for media selection
    val onMediaSelected = remember<(Uri) -> Unit> {
        { uri -> detailMessageViewModel.onSelectedMediaChange(uri) }
    }

    // Gallery launcher with stable callback
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let(onMediaSelected)
        }
    )

    // Camera launcher with stable callback
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            onMediaSelected(cameraImageUri!!)
        }
    }

    // Camera permission launcher with stable callback
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "camera/IMG_${System.currentTimeMillis()}.jpg")
            photoFile.parentFile?.mkdirs()
            cameraImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(cameraImageUri!!)
        } else {
            Toast.makeText(
                context,
                UiText.StringResource(R.string.camera_permission_denied).asString(context),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // LaunchedEffect with proper key (lifecycleOwner instead of Unit)
    LaunchedEffect(lifecycleOwner) {
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

    // Smart auto-scroll: only scroll if user is near the bottom (viewing recent messages)
    LaunchedEffect(detailMessageState.messages.size) {
        if (detailMessageState.messages.isNotEmpty() && listState.firstVisibleItemIndex <= 2) {
            // User is viewing recent messages (within 3 latest), auto-scroll to newest
            listState.animateScrollToItem(0)
        }
    }

    // Mark as seen only for messages that are actually visible on screen
    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        // Wait until scroll finishes to avoid marking while user is scrolling
        if (!listState.isScrollInProgress && detailMessageState.messages.isNotEmpty()) {
            // Get visible message indices (first 5 items visible)
            val visibleIndices = (listState.firstVisibleItemIndex until 
                minOf(listState.firstVisibleItemIndex + 5, detailMessageState.messages.size))
            
            // Check if there are unseen messages in visible range
            val hasUnseenVisibleMessages = visibleIndices.any { index ->
                val message = detailMessageState.messages.getOrNull(index)
                message != null && 
                message.senderId == detailMessageState.friendId &&
                message.seenUserIds?.contains(detailMessageState.currentUserId) != true
            }

            if (hasUnseenVisibleMessages) {
                detailMessageViewModel.markMessageAsSeen()
            }
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
                                key = { message -> message.id } // Key already present - good!
                            ) { item ->
                                // Remember callback to prevent MessageRenderItem recomposition
                                val onImageClick = remember(item.id) {
                                    { imageUrl: String ->
                                        detailMessageViewModel.showImagePreview(
                                            imageModel = imageUrl,
                                            allowClear = false
                                        )
                                    }
                                }

                                MessageRenderItem(
                                    senderAvatar = item.senderAvatar,
                                    senderName = item.senderName,
                                    message = item.content ?: "",
                                    attachments = item.attachments,
                                    time = item.createdAt,
                                    status = item.status,
                                    isGroup = false,
                                    fromCurrentUser = item.senderId == detailMessageState.currentUserId,
                                    onImageClick = onImageClick
                                )
                            }
                        }

                        if (detailMessageState.friendTypingStatus) {
                            Text(
                                text = UiText.StringResource(
                                    R.string.sb_is_typing,
                                    detailMessageState.friendName
                                ).asString(),
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

        // Remember all callbacks for MessageInput to prevent recomposition
        val onMessageTextChange = remember {
            { text: String -> detailMessageViewModel.onMessageInputChange(text) }
        }

        val onCameraClick = remember {
            { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
        }

        val onGalleryClick = remember {
            { galleryLauncher.launch("image/*") }
        }

        val onPreviewClick = remember {
            {
                detailMessageState.selectedImageUri?.let { uri ->
                    detailMessageViewModel.showImagePreview(
                        imageModel = uri,
                        allowClear = true
                    )
                }
                Unit
            }
        }

        val onSendClick = remember {
            { detailMessageViewModel.sendMessage() }
        }

        MessageInput(
            messageText = detailMessageState.messageInput,
            selectedImageUri = detailMessageState.selectedImageUri,
            onMessageTextChange = onMessageTextChange,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onPreviewClick = onPreviewClick,
            onSendClick = onSendClick
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