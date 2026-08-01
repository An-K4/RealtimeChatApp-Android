package com.example.realtimechatapp.ui.screens.groups

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupTypingUser
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.usecase.group.GetGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.group.GetGroupMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.KickedFromGroupInfo
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.EmitGroupTypingStartUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.EmitGroupTypingStopUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupTypingUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.SeenGroupMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.SendGroupMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveKickedFromGroupUseCase
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class DetailGroupViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getGroupMessageUseCase: GetGroupMessageUseCase,
    private val getGroupInfoUseCase: GetGroupInfoUseCase,
    private val observeGroupInfoUseCase: ObserveGroupInfoUseCase,
    private val observeGroupMessageUseCase: ObserveGroupMessageUseCase,
    private val observeGroupTypingUseCase: ObserveGroupTypingUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase,
    private val seenGroupMessageUseCase: SeenGroupMessageUseCase,
    private val emitGroupTypingStartUseCase: EmitGroupTypingStartUseCase,
    private val emitGroupTypingStopUseCase: EmitGroupTypingStopUseCase,
    private val observeKickedFromGroupUseCase: ObserveKickedFromGroupUseCase
) : ViewModel() {
    sealed interface ImagePreviewDialogState {
        object Dismiss : ImagePreviewDialogState
        data class Show(val imageModel: Any, val allowClear: Boolean) : ImagePreviewDialogState
    }

    data class DetailGroupState(
        val currentUserId: String = "",
        val groupName: String = "",
        val groupStatus: UiText? = null,
        val groupTypingStatus: UiText? = null,
        val groupAvatar: String = "",
        val groupMessages: List<Message> = emptyList(),
        val groupMembers: List<Member> = emptyList(),
        val messageInput: String? = null,
        val selectedImageUri: Uri? = null,
        val dialogState: ImagePreviewDialogState = ImagePreviewDialogState.Dismiss,
        val isLoading: Boolean = false,
        val isKicked: Boolean = false
    )

    sealed interface DetailGroupEvent {
        object Success : DetailGroupEvent
        data class Failure(val message: UiText) : DetailGroupEvent
    }

    private data class DetailGroupContext(
        val currentUserId: String,
        val groupHeaderInfo: Group?,
        val isKicked: Boolean
    )

    private data class GroupData(
        val groupMessages: List<Message>,
        val groupTypingUsers: List<GroupTypingUser>
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

    private val groupId: String = checkNotNull(savedStateHandle[Screen.DetailGroup.ARG_GROUP_ID])
    private val _messageInput = MutableStateFlow("")
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    private val _imagePreviewDialogState =
        MutableStateFlow<ImagePreviewDialogState>(ImagePreviewDialogState.Dismiss)
    private val _isLoading = MutableStateFlow(true)

    private val detailGroupContextFlow =
        combine(
            currentUserId,
            observeGroupInfoUseCase(groupId).catch { exception ->
                Timber.e("Lỗi luồng theo dõi thông tin nhóm: ${exception.getErrorMessage()}")
                emit(null)
            },
            observeKickedFromGroupUseCase()
                .onStart {
                    // Emit giá trị mặc định ngay lập tức để combine có thể chạy
                    emit(
                        KickedFromGroupInfo(
                            groupId = "",
                            groupName = "",
                            removedBy = ""
                        )
                    )
                }
                .catch { exception ->
                    Timber.e("Lỗi luồng theo dõi sự kiện bị kick: ${exception.getErrorMessage()}")
                    emit(
                        KickedFromGroupInfo(
                            groupId = "",
                            groupName = "",
                            removedBy = ""
                        )
                    )
                }
        ) { currentUserId, groupHeaderInfo, kickInfo ->
            DetailGroupContext(
                currentUserId = currentUserId,
                groupHeaderInfo = groupHeaderInfo,
                isKicked = kickInfo.groupId == groupId
            )
        }

    // Gộp các socket/observe flows thành 1 intermediate flow (giống DetailMessageViewModel)
    private val groupDataFlow = combine(
        observeGroupMessageUseCase(groupId).catch { exception ->
            Timber.e("Lỗi luồng lấy tin nhắn nhóm: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        observeGroupTypingUseCase(groupId).catch { exception ->
            Timber.e("Lỗi luồng lấy người dùng đang nhập trong nhóm: ${exception.getErrorMessage()}")
            emit(emptyList())
        }
    ) { groupMessages, groupTypingUsers ->
        GroupData(
            groupMessages = groupMessages,
            groupTypingUsers = groupTypingUsers
        )
    }

    private val messageInputAndImageStateFlow =
        combine(_messageInput, _selectedImageUri) { messageInput, selectedImageUri ->
            MessageInputAndImageState(
                messageInput = messageInput,
                selectedImageUri = selectedImageUri
            )
        }

    private val dialogAndLoadingStateFlow =
        combine(_imagePreviewDialogState, _isLoading) { dialogState, isLoading ->
            DialogAndLoadingState(
                dialogState = dialogState,
                isLoading = isLoading
            )
        }

    val detailGroupState = combine(
        detailGroupContextFlow,
        groupDataFlow,
        messageInputAndImageStateFlow,
        dialogAndLoadingStateFlow,
    ) { detailGroupContext, groupData, messageInputAndImageState, dialogAndLoadingState ->
        val otherTypingUsers =
            groupData.groupTypingUsers.filter { it.senderId != detailGroupContext.currentUserId }

        DetailGroupState(
            currentUserId = detailGroupContext.currentUserId,
            groupName = detailGroupContext.groupHeaderInfo?.name ?: "",
            groupStatus = UiText.StringResource(
                R.string.group_status,
                detailGroupContext.groupHeaderInfo?.members?.size ?: 0
            ),
            groupTypingStatus = when (otherTypingUsers.size) {
                3 -> UiText.StringResource(
                    R.string.many_users_is_typing,
                    otherTypingUsers[0].senderName,
                    otherTypingUsers[1].senderName,
                    otherTypingUsers.size - 2
                )

                2 -> UiText.StringResource(
                    R.string.two_users_is_typing,
                    otherTypingUsers[0].senderName,
                    otherTypingUsers[1].senderName
                )

                1 -> UiText.StringResource(
                    R.string.sb_is_typing,
                    otherTypingUsers[0].senderName
                )

                else -> null
            },
            groupAvatar = detailGroupContext.groupHeaderInfo?.avatar ?: "",
            groupMessages = groupData.groupMessages,
            groupMembers = detailGroupContext.groupHeaderInfo?.members ?: emptyList(),
            messageInput = messageInputAndImageState.messageInput,
            selectedImageUri = messageInputAndImageState.selectedImageUri,
            dialogState = dialogAndLoadingState.dialogState,
            isLoading = dialogAndLoadingState.isLoading && groupData.groupMessages.isEmpty(),
            isKicked = detailGroupContext.isKicked
        )
    }.catch { exception ->
        Timber.e(exception, "Lỗi luồng màn hình nhắn nhóm chi tiết")
        _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailGroupState(isLoading = true)
    )

    private val _detailGroupEvent = Channel<DetailGroupEvent>()
    val detailGroupEvent = _detailGroupEvent.receiveAsFlow()

    // init after state variables
    init {
        getGroupInfo()
        getGroupMessage()
        markGroupMessageAsSeen()
    }

    fun markGroupMessageAsSeen() {
        viewModelScope.launch {
            seenGroupMessageUseCase(groupId)
        }
    }

    fun getGroupMessage() {
        viewModelScope.launch {
            _isLoading.value = true
            Timber.d("DetailGroup: Bắt đầu lấy tin nhắn nhóm, groupId=$groupId")

            val result = getGroupMessageUseCase(groupId)
            result.onSuccess {
                Timber.d("DetailGroup: Lấy tin nhắn nhóm thành công")
                // do nothing, saved db before, wait observe
            }.onFailure { exception ->
                Timber.e(exception, "DetailGroup: Lỗi lấy tin nhắn nhóm")
                _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
            }

            _isLoading.value = false
            Timber.d("DetailGroup: Set _isLoading = false")
        }
    }

    fun getGroupInfo() {
        viewModelScope.launch {
            getGroupInfoUseCase(groupId).onSuccess {
                // do nothing, delegate to observe group info use case
            }.onFailure { exception ->
                Timber.e(exception, "DetailGroup: Lỗi lấy thông tin nhóm")
                _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
            }
        }
    }

    private var groupTypingJob: Job? = null
    fun onGroupMessageInputChange(newValue: String) {
        _messageInput.value = newValue

        if (newValue.isEmpty()) {
            if (groupTypingJob?.isActive == true) {
                groupTypingJob?.cancel()
                groupTypingJob = null
                viewModelScope.launch { emitGroupTypingStopUseCase(groupId) }
            }
            return
        } else {
            if (groupTypingJob?.isActive != true) {
                viewModelScope.launch { emitGroupTypingStartUseCase(groupId) }
            }

            groupTypingJob?.cancel()
            groupTypingJob = viewModelScope.launch {
                delay(3000)
                emitGroupTypingStopUseCase(groupId)
            }
        }
    }

    fun onSelectedMediaChange(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun showImagePreview(imageModel: Any, allowClear: Boolean) {
        _imagePreviewDialogState.value = ImagePreviewDialogState.Show(imageModel, allowClear)
    }

    fun dismissImagePreview() {
        _imagePreviewDialogState.value = ImagePreviewDialogState.Dismiss
    }

    fun clearAndDismissImagePreview() {
        _selectedImageUri.value = null
        _imagePreviewDialogState.value = ImagePreviewDialogState.Dismiss
    }

    fun sendGroupMessage() {
        // Save to temp vars
        val tempContent = _messageInput.value.trim()
        val tempImageUri = _selectedImageUri.value
        
        // Validate
        if (tempContent.isEmpty() && tempImageUri == null) return

        // Clear input IMMEDIATELY (optimistic UI - user sees response instantly)
        _messageInput.value = ""
        _selectedImageUri.value = null
        groupTypingJob?.cancel()
        groupTypingJob = null
        viewModelScope.launch { emitGroupTypingStopUseCase(groupId) }

        // THEN call useCase with temp vars (background operation)
        viewModelScope.launch {
            try {
                val result = sendGroupMessageUseCase(
                    content = tempContent,
                    groupId = groupId,
                    selectedImageUri = tempImageUri
                )
                
                result.onSuccess { tempMessageId ->
                    Timber.d("Group message sent with tempId: $tempMessageId")
                }.onFailure { error ->
                    Timber.e(error, "Failed to send group message")
                    _detailGroupEvent.send(DetailGroupEvent.Failure(error.getErrorMessage()))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error in sendGroupMessage")
                _detailGroupEvent.send(DetailGroupEvent.Failure(e.getErrorMessage()))
            }
        }
    }
}