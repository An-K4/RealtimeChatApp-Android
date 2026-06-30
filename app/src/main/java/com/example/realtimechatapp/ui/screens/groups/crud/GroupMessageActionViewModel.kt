package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.usecase.group.GetGroupInfoUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupMessageActionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getGroupInfoUseCase: GetGroupInfoUseCase
) : ViewModel() {
    data class GroupMessageActionState(
        val groupId: String = "",
        val groupName: String? = null,
        val groupDescription: String? = null,
        val groupAvatar: String? = null,
        val groupMemberSize: Int? = null,
        val muteNotifications: Boolean = false,
        val isLoading: Boolean = false
    )

    private val groupId: String =
        checkNotNull(savedStateHandle[Screen.GroupMessageAction.ARG_GROUP_ID])

    private val _groupMessageActionState = MutableStateFlow(
        GroupMessageActionState(
            groupId = groupId
        )
    )
    val groupMessageActionState = _groupMessageActionState.asStateFlow()

    init {
        getGroupInfo()
    }

    private fun getGroupInfo() {
        viewModelScope.launch {
            _groupMessageActionState.update { it.copy(isLoading = true) }

            getGroupInfoUseCase(groupId).onSuccess { group ->
                _groupMessageActionState.update {
                    it.copy(
                        groupId = group.id,
                        groupName = group.name,
                        groupDescription = group.description,
                        groupAvatar = group.avatar,
                        groupMemberSize = group.members.size,
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _groupMessageActionState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onMuteNotificationChange(newValue: Boolean) {
        _groupMessageActionState.update { it.copy(muteNotifications = newValue) }
    }
}