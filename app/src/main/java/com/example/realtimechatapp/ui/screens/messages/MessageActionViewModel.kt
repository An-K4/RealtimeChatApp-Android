package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.usecase.contact.UpdateMessageContactMuteStatusUseCase
import com.example.realtimechatapp.domain.usecase.group.AddMembersUseCase
import com.example.realtimechatapp.domain.usecase.socket.GetLocalGroupsUseCase
import com.example.realtimechatapp.domain.usecase.user.GetUserWithMutedStatusUseCase
import com.example.realtimechatapp.domain.usecase.user.PerformSearchUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MessageActionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserWithMutedStatusUseCase: GetUserWithMutedStatusUseCase,
    private val performSearchUseCase: PerformSearchUseCase,
    private val addMembersUseCase: AddMembersUseCase,
    private val getLocalGroupsUseCase: GetLocalGroupsUseCase,
    private val updateMessageContactMuteStatusUseCase: UpdateMessageContactMuteStatusUseCase
) : ViewModel() {

    data class MessageActionState(
        val userId: String = "",
        val avatar: String = "",
        val fullName: String = "",
        val email: String = "",
        val muteNotifications: Boolean = false,
        val isLoading: Boolean = false,
        val dialogState: MessageActionDialogState = MessageActionDialogState.Dismiss,
        val sheetState: MessageActionSheetState = MessageActionSheetState.Dismiss
    )

    sealed interface MessageActionEvent {
        data class Failure(val message: UiText) : MessageActionEvent
    }

    sealed interface MessageActionDialogState {
        data object Dismiss : MessageActionDialogState
        data class AddToGroupConfirm(val group: Group) : MessageActionDialogState
        data object AddToGroupSuccess : MessageActionDialogState
        data class Failure(val message: UiText) : MessageActionDialogState
    }

    sealed interface MessageActionSheetState {
        data object Dismiss : MessageActionSheetState
        data object AddUserToGroup : MessageActionSheetState
    }

    // UI model: bọc Group kèm trạng thái đã tính sẵn, tránh UI phải tự suy luận domain rule
    data class GroupItemUi(
        val group: Group,
        val isAlreadyMember: Boolean
    )

    data class AddToGroupState(
        val querySearch: String = "",
        val localGroups: List<GroupItemUi>? = null,
        val searchResult: List<GroupItemUi>? = null,
        val isSearching: Boolean = false,
        val isAdding: Boolean = false
    )

    private val userId: String =
        checkNotNull(savedStateHandle[Screen.MessageAction.ARG_FRIEND_ID])

    private var updateMuteJob: Job? = null

    private val _messageActionState = MutableStateFlow(
        MessageActionState(
            userId = userId
        )
    )
    val messageActionState = _messageActionState.asStateFlow()

    private val _messageActionEvent = Channel<MessageActionEvent>()
    val messageActionEvent = _messageActionEvent.receiveAsFlow()

    private val _addToGroupState = MutableStateFlow(AddToGroupState())
    val addToGroupState = _addToGroupState.asStateFlow()

    init {
        getUserInfo()

        // Debounce search query
        viewModelScope.launch {
            _addToGroupState
                .map { it.querySearch }
                .distinctUntilChanged()
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        performGroupSearch(query)
                    } else {
                        _addToGroupState.update { it.copy(searchResult = null) }
                    }
                }
        }
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            _messageActionState.update { it.copy(isLoading = true) }

            getUserWithMutedStatusUseCase(userId).onSuccess { user ->
                _messageActionState.update {
                    it.copy(
                        avatar = user.avatar ?: "",
                        fullName = user.fullName,
                        email = user.email,
                        muteNotifications = user.isMuted,
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                Timber.e(exception, "Lỗi lấy thông tin người dùng")
                _messageActionState.update { it.copy(isLoading = false) }
                _messageActionEvent.send(MessageActionEvent.Failure(exception.getErrorMessage()))
            }
        }
    }

    fun onMuteNotificationChange(newValue: Boolean) {
        _messageActionState.update { it.copy(muteNotifications = newValue) }
        updateMuteJob?.cancel()
        updateMuteJob = viewModelScope.launch {
            updateMessageContactMuteStatusUseCase(userId, newValue)
        }
    }

    private fun List<Group>.toGroupItemUi(): List<GroupItemUi> =
        map { group ->
            GroupItemUi(
                group = group,
                isAlreadyMember = group.members.any { member -> member.userId?.id == userId }
            )
        }

    private fun prepareAddToGroupFlow() {
        viewModelScope.launch {
            _addToGroupState.update { it.copy(isSearching = true) }
            getLocalGroupsUseCase().onSuccess { groups ->
                _addToGroupState.update {
                    it.copy(
                        querySearch = "",
                        localGroups = groups.toGroupItemUi(),
                        searchResult = null,
                        isSearching = false,
                    )
                }
            }.onFailure { exception ->
                _messageActionEvent.send(MessageActionEvent.Failure(exception.getErrorMessage()))
                _addToGroupState.update {
                    it.copy(
                        querySearch = "",
                        localGroups = null,
                        searchResult = null,
                        isSearching = false,
                    )
                }
            }
        }
    }

    fun dismissSheet() {
        _messageActionState.update {
            it.copy(sheetState = MessageActionSheetState.Dismiss)
        }
    }

    fun showAddUserToGroupSheet() {
        prepareAddToGroupFlow()
        _messageActionState.update {
            it.copy(sheetState = MessageActionSheetState.AddUserToGroup)
        }
    }

    fun onGroupSearchQueryChange(query: String) {
        _addToGroupState.update { it.copy(querySearch = query) }
    }

    private fun performGroupSearch(query: String) {
        viewModelScope.launch {
            _addToGroupState.update { it.copy(isSearching = true) }

            performSearchUseCase(query).onSuccess { searchResult ->
                _addToGroupState.update {
                    it.copy(
                        searchResult = searchResult.groups?.toGroupItemUi(),
                        isSearching = false
                    )
                }
            }.onFailure { exception ->
                Timber.e(exception, "Lỗi tìm kiếm nhóm")
                _addToGroupState.update { it.copy(isSearching = false) }
                _messageActionEvent.send(MessageActionEvent.Failure(exception.getErrorMessage()))
            }
        }
    }

    fun showAddToGroupConfirmDialog(group: Group) {
        _messageActionState.update {
            it.copy(dialogState = MessageActionDialogState.AddToGroupConfirm(group))
        }
    }

    fun dismissDialog() {
        _messageActionState.update { it.copy(dialogState = MessageActionDialogState.Dismiss) }
    }

    fun addToSelectedGroup(selectedGroupId: String) {
        viewModelScope.launch {
            _addToGroupState.update { it.copy(isAdding = true) }

            addMembersUseCase(selectedGroupId, listOf(userId))
                .onSuccess {
                    _addToGroupState.update { it.copy(isAdding = false) }
                    _messageActionState.update {
                        it.copy(dialogState = MessageActionDialogState.AddToGroupSuccess)
                    }
                }
                .onFailure { exception ->
                    Timber.e(exception, "Lỗi thêm người dùng vào nhóm")
                    _addToGroupState.update { it.copy(isAdding = false) }
                    _messageActionState.update {
                        it.copy(dialogState = MessageActionDialogState.Failure(exception.getErrorMessage()))
                    }
                }
        }
    }
}