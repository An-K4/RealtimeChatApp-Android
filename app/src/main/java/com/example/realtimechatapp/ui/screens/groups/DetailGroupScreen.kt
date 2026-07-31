package com.example.realtimechatapp.ui.screens.groups

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
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import java.io.File

@Composable
fun DetailGroupScreen(
    navController: NavController,
    detailGroupViewModel: DetailGroupViewModel = hiltViewModel()
) {
    val detailGroupState by detailGroupViewModel.detailGroupState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Camera URI state
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Remember stable callback for media selection
    val onMediaSelected = remember<(Uri) -> Unit> {
        { uri -> detailGroupViewModel.onSelectedMediaChange(uri) }
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
            Toast.makeText(context, UiText.StringResource(R.string.camera_permission_denied).asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    // LaunchedEffect with proper key (lifecycleOwner instead of Unit)
    LaunchedEffect(lifecycleOwner) {
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
                        Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    // Auto-scroll logic remains the same
    LaunchedEffect(detailGroupState.groupMessages.size) {
        if (detailGroupState.groupMessages.isNotEmpty()) {
            val hasUnseenMessages = detailGroupState.groupMessages.any { message ->
                message.senderId != detailGroupState.currentUserId
                        && message.seenUserIds?.contains(detailGroupState.currentUserId) != true
            }

            if (hasUnseenMessages) {
                detailGroupViewModel.markGroupMessageAsSeen()
            }

            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Kicked Dialog
        if (detailGroupState.isKicked) {
            NotificationDialog(
                title = UiText.StringResource(R.string.warning).asString(),
                message = "Bạn đã bị xóa khỏi nhóm. Liên hệ admin để biết thêm thông tin.",
                isSuccess = false,
                onDismiss = { navController.popBackStack() }
            )
        }

        // in development
        ContactHeader(
            avatarContactPreview = detailGroupState.groupAvatar,
            contactName = detailGroupState.groupName,
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
                                items = detailGroupState.groupMessages,
                                key = { groupMessage -> groupMessage.id } // Key already present - good!
                            ) { groupMessage ->
                                // Remember callback to prevent MessageRenderItem recomposition
                                val onImageClick = remember(groupMessage.id) {
                                    { imageUrl: String ->
                                        detailGroupViewModel.showImagePreview(
                                            imageModel = imageUrl,
                                            allowClear = false
                                        )
                                    }
                                }
                                
                                MessageRenderItem(
                                    senderAvatar = groupMessage.senderAvatar,
                                    senderName = groupMessage.senderName,
                                    message = groupMessage.content ?: "",
                                    attachments = groupMessage.attachments,
                                    time = groupMessage.createdAt,
                                    isSeen = groupMessage.seenUserIds?.isNotEmpty() == true,
                                    isGroup = true,
                                    fromCurrentUser = groupMessage.senderId == detailGroupState.currentUserId,
                                    onImageClick = onImageClick
                                )
                            }
                        }

                        detailGroupState.groupTypingStatus?.let {
                            Text(
                                text = it.asString(),
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
            { text: String -> detailGroupViewModel.onGroupMessageInputChange(text) }
        }
        
        val onCameraClick = remember {
            { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
        }
        
        val onGalleryClick = remember {
            { galleryLauncher.launch("image/*") }
        }
        
        val onPreviewClick = remember {
            {
                detailGroupState.selectedImageUri?.let { uri ->
                    detailGroupViewModel.showImagePreview(
                        imageModel = uri,
                        allowClear = true
                    )
                }
                Unit
            }
        }
        
        val onSendClick = remember {
            { detailGroupViewModel.sendGroupMessage() }
        }

        MessageInput(
            messageText = detailGroupState.messageInput,
            selectedImageUri = detailGroupState.selectedImageUri,
            isSending = detailGroupState.isSending,
            onMessageTextChange = onMessageTextChange,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onPreviewClick = onPreviewClick,
            onSendClick = onSendClick
        )
    }

    // Render FullScreenImagePreviewDialog
    when (val dialogState = detailGroupState.dialogState) {
        is DetailGroupViewModel.ImagePreviewDialogState.Show -> {
            FullScreenImagePreviewDialog(
                imageModel = dialogState.imageModel,
                onDismiss = { detailGroupViewModel.dismissImagePreview() },
                onClear = if (dialogState.allowClear) {
                    { detailGroupViewModel.clearAndDismissImagePreview() }
                } else null
            )
        }
        DetailGroupViewModel.ImagePreviewDialogState.Dismiss -> {
            // Do nothing
        }
    }
}