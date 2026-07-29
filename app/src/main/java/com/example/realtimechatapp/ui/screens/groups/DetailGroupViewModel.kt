package com.example.realtimechatapp.ui.screens.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.usecase.group.GetGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.group.GetGroupMessageUseCase
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
    data class DetailGroupState(
        val currentUserId: String = "",
        val groupName: String = "",
        val groupStatus: UiText? = null,
        val groupTypingStatus: UiText? = null,
        val groupAvatar: String = "",
        val groupMessages: List<Message> = emptyList(),
        val groupMembers: List<Member> = emptyList(),
        val messageInput: String? = null,
        val isLoading: Boolean = false,
        val isKicked: Boolean = false
    )

    sealed interface DetailGroupEvent {
        object Success : DetailGroupEvent
        data class Failure(val message: UiText) : DetailGroupEvent
    }

    private data class DetailGroupContext(
        val currentUserId: String,
        val groupHeaderInfo: Group?
    )

    private data class InputAndLoadingState(
        val messageInput: String,
        val isLoading: Boolean
    )

    private val currentUserId = flow { emit(getCurrentUserIdUseCase().getOrThrow()) }

    private val groupId: String = checkNotNull(savedStateHandle[Screen.DetailGroup.ARG_GROUP_ID])
    private val _messageInput = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _isKicked = MutableStateFlow(false)

    private val detailGroupContextFlow =
        combine(currentUserId, observeGroupInfoUseCase(groupId)) { currentUserId, groupHeaderInfo ->
            DetailGroupContext(
                currentUserId = currentUserId,
                groupHeaderInfo = groupHeaderInfo
            )
        }

    private val inputAndLoadingStateFlow =
        combine(_messageInput, _isLoading) { messageInput, isLoading ->
            InputAndLoadingState(
                messageInput = messageInput,
                isLoading = isLoading
            )
        }

    val detailGroupState = combine(
        detailGroupContextFlow.catch { exception ->
            Timber.e("Lỗi luồng lấy thông tin nhóm: ${exception.getErrorMessage()}")
            emit(DetailGroupContext("", null))
        },
        observeGroupMessageUseCase(groupId).catch { exception ->
            Timber.e("Lỗi luồng lấy tin nhắn nhóm: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        observeGroupTypingUseCase(groupId).catch { exception ->
            Timber.e("Lỗi luồng lấy người dùng đang nhập trong nhóm: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        inputAndLoadingStateFlow,
        _isKicked
    ) { detailGroupContext, groupMessages, groupTypingUsers, inputAndLoadingState, isKicked ->
        val otherTypingUsers =
            groupTypingUsers.filter { it.senderId != detailGroupContext.currentUserId }

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
            groupMessages = groupMessages,
            groupMembers = detailGroupContext.groupHeaderInfo?.members ?: emptyList(),
            messageInput = inputAndLoadingState.messageInput,
            isLoading = inputAndLoadingState.isLoading && groupMessages.isEmpty(),
            isKicked = isKicked
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
        observeKickEvents()
    }
    
    private fun observeKickEvents() {
        viewModelScope.launch {
            observeKickedFromGroupUseCase().collect { kickInfo ->
                if (kickInfo.groupId == groupId) {
                    _isKicked.value = true
                }
            }
        }
    }

    fun markGroupMessageAsSeen() {
        viewModelScope.launch {
            seenGroupMessageUseCase(groupId)
        }
    }

    fun getGroupMessage() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = getGroupMessageUseCase(groupId)
            result.onSuccess {
                // do nothing, saved db before, wait observe
            }.onFailure { exception ->
                _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
            }

            _isLoading.value = false
        }
    }

    fun getGroupInfo() {
        viewModelScope.launch {
            getGroupInfoUseCase(groupId).onSuccess {
                // do nothing, delegate to observe group info use case
            }.onFailure { exception ->
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

    fun sendGroupMessage() {
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            sendGroupMessageUseCase(
                content = content,
                groupId = groupId
            )

            groupTypingJob?.cancel()
            groupTypingJob = null
            viewModelScope.launch { emitGroupTypingStopUseCase(groupId) }

            _messageInput.value = ""
        }
    }
}