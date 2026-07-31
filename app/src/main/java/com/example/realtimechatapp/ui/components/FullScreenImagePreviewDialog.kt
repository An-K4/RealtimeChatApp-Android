package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun FullScreenImagePreviewDialog(
    imageModel: Any,
    onDismiss: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            // X button (top left) - chỉ đóng dialog, KHÔNG xoá ảnh
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Trash icon button (top right) - CHỈ hiện nếu onClear != null (ảnh sắp gửi)
            if (onClear != null) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete image",
                        tint = Color.White
                    )
                }
            }

            // Image center
            AsyncImage(
                model = imageModel,
                contentDescription = "Full screen preview",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar),
                fallback = painterResource(R.drawable.default_avatar),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FullScreenImagePreviewDialogPreview() {
    RealtimeChatAppTheme {
        FullScreenImagePreviewDialog(
            imageModel = "",
            onDismiss = {},
            onClear = {}
        )
    }
}
