package com.example.realtimechatapp.ui.screens.messages

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.message.GetHeaderInfoUseCase
import com.example.realtimechatapp.domain.usecase.message.GetMessageUseCase
import com.example.realtimechatapp.domain.usecase.notification.CancelMessageNotificationUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.EmitTypingStartUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.EmitTypingStopUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveTypingUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.SeenMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.SendMessageUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetailMessageViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getMessageUseCase: GetMessageUseCase,
    private val getHeaderInfoUseCase: GetHeaderInfoUseCase,
    private val observeMessageUseCase: ObserveMessageUseCase,
    private val observeOnlineUserUseCase: ObserveOnlineUserUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val seenMessageUseCase: SeenMessageUseCase,
    private val emitTypingStartUseCase: EmitTypingStartUseCase,
    private val emitTypingStopUseCase: EmitTypingStopUseCase,
    private val cancelMessageNotificationUseCase: CancelMessageNotificationUseCase
) : ViewModel() {
    data class DetailMessageState(
        val currentUserId: String = "",
        val friendId: String = "",
        val friendName: String? = null,
        val friendStatus: UiText? = null,
        val friendTypingStatus: Boolean = false,
        val friendAvatar: String? = null,
        val messages: List<Message> = emptyList(),
        val messageInput: String? = null,
        val isLoading: Boolean = false,
        val isSending: Boolean = false,
        val selectedImageUri: Uri? = null,
        val dialogState: ImagePreviewDialogState = ImagePreviewDialogState.Dismiss
    )

    sealed interface ImagePreviewDialogState {
        object Dismiss : ImagePreviewDialogState
        data class Show(
            val imageModel: Any,
            val allowClear: Boolean = false
        ) : ImagePreviewDialogState
    }

    sealed interface DetailMessageEvent {
        object GetMessageSuccess : DetailMessageEvent
        data class Failure(val message: UiText) : DetailMessageEvent
    }

    private data class DetailMessageContext(
        val currentUserId: String,
        val friendUser: User?,
    )

    private data class SocketData(
        val messages: List<Message>,
        val onlineUserIds: Set<String>,
        val typingUserIds: Set<String>
    )

    private data class MessageInputAndImageState(
        val messageInput: String,
        val selectedImageUri: Uri?
    )

    private data class DialogAndLoadingState(
        val dialogState: ImagePreviewDialogState,
        val isLoading: Boolean
    )

    private val currentUserId = flow { emit(getCurrentUserIdUseCase().getOrThrow()) }
    private val friendId: String =
        checkNotNull(savedStateHandle[Screen.DetailMessage.ARG_FRIEND_ID])
    private val _messageInput = MutableStateFlow("")
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    private val _imagePreviewDialogState = MutableStateFlow<ImagePreviewDialogState>(ImagePreviewDialogState.Dismiss)
    private val _isLoading = MutableStateFlow(true)
    private val _headerInfo = MutableStateFlow<User?>(null)
    private val detailMessageContextFlow =
        combine(currentUserId, _headerInfo) { currentUserId, headerInfo ->
            DetailMessageContext(
                currentUserId = currentUserId,
                friendUser = headerInfo
            )
        }

    private val socketDataFlow = combine(
        observeMessageUseCase(friendId).catch { exception ->
            Timber.e(exception, "Lỗi luồng DB Message")
            emit(emptyList())
        },
        observeOnlineUserUseCase().catch { exception ->
            Timber.e(exception, "Lỗi luồng user online")
            emit(emptySet())
        },
        observeTypingUseCase().catch { exception ->
            Timber.e(exception, "Lỗi luồng user typing")
            emit(emptySet())
        }
    ) { messages, onlineUserIds, typingUserIds ->
        SocketData(
            messages = messages,
            onlineUserIds = onlineUserIds,
            typingUserIds = typingUserIds
        )
    }

    // mutable state flows don't throw exception
    private val messageInputAndImageStateFlow = combine(
        _messageInput,
        _selectedImageUri
    ) { messageInput, selectedImageUri ->
        MessageInputAndImageState(
            messageInput = messageInput,
            selectedImageUri = selectedImageUri
        )
    }

    private val dialogAndLoadingStateFlow = combine(
        _imagePreviewDialogState,
        _isLoading
    ) { dialogState, isLoading ->
        DialogAndLoadingState(
            dialogState = dialogState,
            isLoading = isLoading
        )
    }

    val detailMessageState = combine(
        detailMessageContextFlow,
        socketDataFlow,
        messageInputAndImageStateFlow,
        dialogAndLoadingStateFlow
    ) { detailMessageContext, socketData, messageInputAndImageState, dialogAndLoadingState ->
        DetailMessageState(
            currentUserId = detailMessageContext.currentUserId,
            friendId = friendId,
            friendName = detailMessageContext.friendUser?.fullName ?: "",
            friendStatus = if (socketData.onlineUserIds.contains(friendId))
                UiText.StringResource(R.string.online)
            else
                UiText.StringResource(R.string.offline),
            friendTypingStatus = socketData.typingUserIds.contains(friendId),
            friendAvatar = detailMessageContext.friendUser?.avatar ?: "",
            messages = socketData.messages,
            messageInput = messageInputAndImageState.messageInput,
            isLoading = dialogAndLoadingState.isLoading && socketData.messages.isEmpty(),
            selectedImageUri = messageInputAndImageState.selectedImageUri,
            dialogState = dialogAndLoadingState.dialogState
        )
    }.catch { exception ->
        Timber.e(exception, "Lỗi luồng màn hình nhắn chi tiết")
        _detailMessageEvent.send(DetailMessageEvent.Failure(exception.getErrorMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailMessageState(isLoading = true)
    )

    private val _detailMessageEvent = Channel<DetailMessageEvent>()
    val detailMessageEvent = _detailMessageEvent.receiveAsFlow()

    // init after state variables
    init {
        cancelMessageNotificationUseCase(friendId)
        getHeaderInfo()
        getMessages()
        markMessageAsSeen()
    }

    private fun getHeaderInfo() {
        viewModelScope.launch {
            val result = getHeaderInfoUseCase(friendId)

            result.onSuccess { user ->
                _headerInfo.value = user
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
            }
        }
    }

    private fun getMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getMessageUseCase(friendId)

            result.onSuccess {
                // Success: messages will be emitted via observeMessageUseCase
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
            }

            _isLoading.value = false
        }
    }

    fun markMessageAsSeen() {
        viewModelScope.launch {
            seenMessageUseCase(friendId)
        }
    }

    private var typingJob: Job? = null
    fun onMessageInputChange(newValue: String) {
        _messageInput.value = newValue

        if (newValue.isEmpty()) {
            if (typingJob?.isActive == true) {
                // clear job and return
                typingJob?.cancel()
                typingJob = null
                viewModelScope.launch { emitTypingStopUseCase(friendId) }
            }
            return
        } else {
            if (typingJob?.isActive != true) {
                viewModelScope.launch { emitTypingStartUseCase(friendId) }
            }

            // reset old timer
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3000)
                emitTypingStopUseCase(friendId)
            }
        }
    }

    fun onSelectedMediaChange(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun showImagePreview(imageModel: Any, allowClear: Boolean = false) {
        _imagePreviewDialogState.value = ImagePreviewDialogState.Show(imageModel, allowClear)
    }

    fun dismissImagePreview() {
        _imagePreviewDialogState.value = ImagePreviewDialogState.Dismiss
    }

    fun clearAndDismissImagePreview() {
        _selectedImageUri.value = null
        _imagePreviewDialogState.value = ImagePreviewDialogState.Dismiss
    }

    fun sendMessage() {
        // Save to temp vars
        val tempContent = _messageInput.value.trim()
        val tempImageUri = _selectedImageUri.value
        
        // Validate
        if (tempContent.isEmpty() && tempImageUri == null) return

        // Clear input IMMEDIATELY (optimistic UI - user sees response instantly)
        _messageInput.value = ""
        _selectedImageUri.value = null
        typingJob?.cancel()
        typingJob = null
        viewModelScope.launch { emitTypingStopUseCase(friendId) }

        // THEN call useCase with temp vars (background operation)
        viewModelScope.launch {
            try {
                val result = sendMessageUseCase(
                    content = tempContent,
                    receiverId = friendId,
                    selectedImageUri = tempImageUri
                )
                
                result.onSuccess { tempMessageId ->
                    Timber.d("Message sent with tempId: $tempMessageId")
                }.onFailure { error ->
                    Timber.e(error, "Failed to send message")
                    _detailMessageEvent.send(DetailMessageEvent.Failure(error.getErrorMessage()))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error in sendMessage")
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
            }
        }
    }
}
