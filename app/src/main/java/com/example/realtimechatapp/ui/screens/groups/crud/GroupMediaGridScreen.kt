package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.FullScreenImagePreviewDialog
import com.example.realtimechatapp.ui.components.MediaGridContent

@Composable
fun GroupMediaGridScreen(
    navController: NavController,
    viewModel: GroupMediaGridViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            MediaGridContent(
                messages = state.mediaMessages,
                isLoadingMore = state.isLoadingMore,
                hasMore = state.hasMore,
                emptyText = UiText.StringResource(R.string.no_media_files),
                onImageClick = { viewModel.showImagePreview(it) },
                onLoadMore = { viewModel.loadMore() }
            )
        }
    }

    // Image preview dialog
    state.selectedImageUrl?.let { imageUrl ->
        FullScreenImagePreviewDialog(
            imageModel = imageUrl,
            onDismiss = { viewModel.dismissImagePreview() }
        )
    }
}
