package com.example.realtimechatapp.ui.screens.groups.crud

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.group.GetGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.group.UpdateGroupUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveKickedFromGroupUseCase
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
class EditGroupViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getGroupInfoUseCase: GetGroupInfoUseCase,
    private val observeGroupInfoUseCase: ObserveGroupInfoUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val observeKickedFromGroupUseCase: ObserveKickedFromGroupUseCase
) : ViewModel() {
    data class EditGroupState(
        val groupName: String = "",
        val groupDescription: String? = null,
        val groupAvatar: Any? = null,
        val isUpdateEnable: Boolean = false,
        val isLoading: Boolean = false,
        val isUpdating: Boolean = false,
        val dialogState: EditGroupDialogState = EditGroupDialogState.Dismiss
    )
    
    sealed interface EditGroupDialogState {
        object Dismiss : EditGroupDialogState
        object KickedFromGroup : EditGroupDialogState
    }

    sealed interface EditGroupEvent {
        object EditSuccess : EditGroupEvent
        data class Failure(val message: UiText) : EditGroupEvent
    }

    private data class OriginalGroupInfo(
        val groupName: String,
        val groupAvatar: String?,
        val groupDescription: String?
    )

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private var originalGroupInfo = OriginalGroupInfo("", null, null)
    private var isFirstEmit = true

    private val _editGroupState = MutableStateFlow(EditGroupState())
    val editGroupState = _editGroupState.asStateFlow()

    private val _editGroupEvent = Channel<EditGroupEvent>()
    val editGroupEvent = _editGroupEvent.receiveAsFlow()

    init {
        getGroupInfo()
        observeGroupInfo()
        observeKickEvents()
    }
    
    private fun observeKickEvents() {
        viewModelScope.launch {
            observeKickedFromGroupUseCase().collect { kickInfo ->
                if (kickInfo.groupId == groupId) {
                    _editGroupState.update { it.copy(dialogState = EditGroupDialogState.KickedFromGroup) }
                }
            }
        }
    }

    private fun getGroupInfo() {
        viewModelScope.launch {
            _editGroupState.update { it.copy(isLoading = true) }

            getGroupInfoUseCase(groupId).onSuccess {
                // do nothing, delegate to observe
            }.onFailure {
                _editGroupEvent.send(EditGroupEvent.Failure(it.getErrorMessage()))
            }

            _editGroupState.value = editGroupState.value.copy(isLoading = false)
        }
    }

    private fun observeGroupInfo() {
        viewModelScope.launch {
            observeGroupInfoUseCase(groupId).catch { exception ->
                Timber.e(exception, "Lỗi luồng theo dõi thông tin nhóm")
                _editGroupEvent.send(EditGroupEvent.Failure(exception.getErrorMessage()))
            }.collect { group ->
                originalGroupInfo = originalGroupInfo.copy(
                    groupName = group?.name ?: "",
                    groupAvatar = group?.avatar,
                    groupDescription = group?.description
                )

                _editGroupState.update { currentState ->
                    if (isFirstEmit) {
                        isFirstEmit = false
                        currentState.copy(
                            groupName = group?.name ?: "",
                            groupDescription = group?.description,
                            groupAvatar = group?.avatar,
                            isLoading = false
                        )
                    } else {
                        currentState.copy(isLoading = false)
                    }
                }

                checkUpdateEnable()
            }
        }
    }

    fun onGroupAvatarChange(newValue: Any?) {
        if (newValue is String || newValue is Uri || newValue == null) {
            _editGroupState.update { it.copy(groupAvatar = newValue) }
        }
        checkUpdateEnable()
    }

    fun onGroupNameChange(newValue: String) {
        _editGroupState.update { it.copy(groupName = newValue) }
        checkUpdateEnable()
    }

    fun onGroupDescriptionChange(newValue: String) {
        _editGroupState.update { it.copy(groupDescription = newValue) }
        checkUpdateEnable()
    }

    private fun checkUpdateEnable() {
        val original = originalGroupInfo
        val update = editGroupState.value

        val isChanged =
            (update.groupName != original.groupName) || (update.groupDescription != original.groupDescription) || (update.groupAvatar != original.groupAvatar)
        _editGroupState.update { it.copy(isUpdateEnable = isChanged) }
    }

    fun updateGroup() {
        viewModelScope.launch {
            val isGroupAvatarChanged =
                originalGroupInfo.groupAvatar != editGroupState.value.groupAvatar

            _editGroupState.update { it.copy(isUpdating = true) }

            val updateGroupResult = updateGroupUseCase(
                groupId,
                editGroupState.value.groupName,
                editGroupState.value.groupAvatar as? Uri,
                editGroupState.value.groupDescription,
                isGroupAvatarChanged
            )

            updateGroupResult.onSuccess {
                _editGroupState.update { it.copy(isUpdating = false) }
                _editGroupEvent.send(EditGroupEvent.EditSuccess)
            }.onFailure { exception ->
                _editGroupState.update { it.copy(isUpdating = false) }
                _editGroupEvent.send(EditGroupEvent.Failure(exception.getErrorMessage()))
            }
        }
    }
}