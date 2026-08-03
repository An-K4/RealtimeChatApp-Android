package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.usecase.group.GetGroupMessagesWithAttachmentsUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GroupMediaGridViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGroupMessagesWithAttachmentsUseCase: GetGroupMessagesWithAttachmentsUseCase
) : ViewModel() {

    data class GroupMediaGridState(
        val groupId: String = "",
        val mediaMessages: List<Message> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
        val selectedImageUrl: String? = null,
        val hasMore: Boolean = true,
        val currentOffset: Int = 0
    )

    private val _state = MutableStateFlow(GroupMediaGridState())
    val state = _state.asStateFlow()

    private val pageSize = 30

    init {
        val groupId = savedStateHandle.get<String>(Screen.GroupMediaGrid.ARG_GROUP_ID) ?: ""
        _state.update { it.copy(groupId = groupId) }
        loadMedia()
    }

    fun loadMedia() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = getGroupMessagesWithAttachmentsUseCase(
                groupId = _state.value.groupId,
                limit = pageSize,
                offset = 0
            )

            result.fold(
                onSuccess = { messages ->
                    _state.update {
                        it.copy(
                            mediaMessages = messages,
                            isLoading = false,
                            hasMore = messages.size >= pageSize,
                            currentOffset = messages.size
                        )
                    }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to load group media")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error"
                        )
                    }
                }
            )
        }
    }

    fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            val result = getGroupMessagesWithAttachmentsUseCase(
                groupId = _state.value.groupId,
                limit = pageSize,
                offset = _state.value.currentOffset
            )

            result.fold(
                onSuccess = { newMessages ->
                    _state.update {
                        it.copy(
                            mediaMessages = it.mediaMessages + newMessages,
                            isLoadingMore = false,
                            hasMore = newMessages.size >= pageSize,
                            currentOffset = it.currentOffset + newMessages.size
                        )
                    }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to load more group media")
                    _state.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    fun showImagePreview(imageUrl: String) {
        _state.update { it.copy(selectedImageUrl = imageUrl) }
    }

    fun dismissImagePreview() {
        _state.update { it.copy(selectedImageUrl = null) }
    }
}
