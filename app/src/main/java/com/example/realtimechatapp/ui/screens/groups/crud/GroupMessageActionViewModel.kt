package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.domain.usecase.group.GetGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.group.LeaveGroupUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GroupMessageActionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getGroupInfoUseCase: GetGroupInfoUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeGroupInfoUseCase: ObserveGroupInfoUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase
) : ViewModel() {
    data class GroupMessageActionState(
        val groupId: String = "",
        val groupName: String? = null,
        val groupDescription: String? = null,
        val groupAvatar: String? = null,
        val groupMemberSize: Int? = null,
        val isEditGroupInfoVisible: Boolean = false,
        val isDeleteGroupVisible: Boolean = false,
        val muteNotifications: Boolean = false,
        val isLoading: Boolean = false,
        val dialogState: GroupMessageActionDialogState = GroupMessageActionDialogState.Dismiss,
        val isLeavingGroup: Boolean = false
    )

    sealed interface GroupMessageActionDialogState {
        object Dismiss : GroupMessageActionDialogState
        object LeaveGroupConfirm : GroupMessageActionDialogState
        object LeaveGroupSuccess : GroupMessageActionDialogState
        object DeleteGroupConfirm : GroupMessageActionDialogState
        object DeleteGroupSuccess : GroupMessageActionDialogState
        data class Failure(val message: UiText) : GroupMessageActionDialogState
    }

    sealed interface GroupMessageActionEvent {
        object NavigateBack : GroupMessageActionEvent
        data class Failure(val message: UiText) : GroupMessageActionEvent
    }

    private val groupId: String =
        checkNotNull(savedStateHandle[Screen.GroupMessageAction.ARG_GROUP_ID])
    private lateinit var currentUserId: String

    private val _groupMessageActionState = MutableStateFlow(
        GroupMessageActionState(
            groupId = groupId
        )
    )
    val groupMessageActionState = _groupMessageActionState.asStateFlow()

    private val _groupMessageActionEvent = Channel<GroupMessageActionEvent>()
    val groupMessageActionEvent = _groupMessageActionEvent.receiveAsFlow()

    private var isCurrentUserOwner: Boolean = false

    init {
        getGroupInfo()
        getCurrentUserId()
        observeGroupInfo()
    }

    private fun observeGroupInfo() {
        viewModelScope.launch {
            observeGroupInfoUseCase(groupId).catch { exception ->
                Timber.e(exception, "Lỗi luồng theo dõi thông tin nhóm")
                _groupMessageActionEvent.send(GroupMessageActionEvent.Failure(exception.getErrorMessage()))
            }.collect { group ->
                if (currentUserId.isEmpty()) {
                    return@collect
                } else {
                    val currentUserRole = group?.members?.find { it.userId?.id == currentUserId }?.role ?: Role.MEMBER
                    isCurrentUserOwner = group?.owner?.id == currentUserId
                    val isCurrentUserAdmin = currentUserRole == Role.ADMIN

                    _groupMessageActionState.update {
                        it.copy(
                            groupName = group?.name ?: "",
                            groupDescription = group?.description,
                            groupAvatar = group?.avatar,
                            groupMemberSize = group?.members?.size,
                            isEditGroupInfoVisible = isCurrentUserOwner || isCurrentUserAdmin,
                            isDeleteGroupVisible = isCurrentUserOwner
                        )
                    }
                }
            }
        }
    }

    private fun getCurrentUserId() {
        viewModelScope.launch {
            currentUserId = getCurrentUserIdUseCase().getOrElse { exception ->
                _groupMessageActionEvent.send(GroupMessageActionEvent.Failure(exception.getErrorMessage()))
                return@launch
            }
        }
    }

    private fun getGroupInfo() {
        viewModelScope.launch {
            _groupMessageActionState.update { it.copy(isLoading = true) }

            getGroupInfoUseCase(groupId).onSuccess {
                // do nothing, delegate to observe
            }.onFailure { exception ->
                _groupMessageActionEvent.send(GroupMessageActionEvent.Failure(exception.getErrorMessage()))
                return@launch
            }

            _groupMessageActionState.update { it.copy(isLoading = false) }
        }
    }

    fun onMuteNotificationChange(newValue: Boolean) {
        _groupMessageActionState.update { it.copy(muteNotifications = newValue) }
    }

    fun showLeaveGroupConfirmDialog() {
        viewModelScope.launch {
            if (isCurrentUserOwner) {
                _groupMessageActionState.update {
                    it.copy(
                        dialogState = GroupMessageActionDialogState.Failure(
                            UiText.StringResource(R.string.owner_cannot_leave_group)
                        )
                    )
                }
            } else {
                _groupMessageActionState.update { it.copy(dialogState = GroupMessageActionDialogState.LeaveGroupConfirm) }
            }
        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            _groupMessageActionState.update { it.copy(isLeavingGroup = true) }

            leaveGroupUseCase(groupId).onSuccess {
                _groupMessageActionState.update {
                    it.copy(
                        dialogState = GroupMessageActionDialogState.LeaveGroupSuccess,
                        isLeavingGroup = false
                    )
                }
            }.onFailure { exception ->
                _groupMessageActionState.update {
                    it.copy(
                        dialogState = GroupMessageActionDialogState.Failure(
                            exception.getErrorMessage()
                        ),
                        isLeavingGroup = false
                    )
                }
            }
        }
    }

    fun dismissDialog() {
        _groupMessageActionState.update { it.copy(dialogState = GroupMessageActionDialogState.Dismiss) }
    }

    fun showDeleteGroupConfirmDialog() {
        _groupMessageActionState.update { it.copy(dialogState = GroupMessageActionDialogState.DeleteGroupConfirm) }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            _groupMessageActionState.update { it.copy(isLeavingGroup = true) }

            leaveGroupUseCase(groupId).onSuccess {
                _groupMessageActionState.update {
                    it.copy(
                        dialogState = GroupMessageActionDialogState.DeleteGroupSuccess,
                        isLeavingGroup = false
                    )
                }
            }.onFailure { exception ->
                _groupMessageActionState.update {
                    it.copy(
                        dialogState = GroupMessageActionDialogState.Failure(
                            exception.getErrorMessage()
                        ),
                        isLeavingGroup = false
                    )
                }
            }
        }
    }
}