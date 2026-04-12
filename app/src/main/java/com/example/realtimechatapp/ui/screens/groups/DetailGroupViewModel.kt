package com.example.realtimechatapp.ui.screens.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.domain.usecase.groups.GetGroupInfoUseCase
import com.example.realtimechatapp.domain.usecase.groups.GetGroupMessageUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetailGroupViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getGroupMessageUseCase: GetGroupMessageUseCase,
    private val getGroupInfoUseCase: GetGroupInfoUseCase
) : ViewModel() {
    data class DetailGroupState(
        val currentUserId: String = "",
        val groupId: String,
        val groupName: String? = null,
        val groupStatus: String? = null,
        val groupAvatar: String? = null,
        val groupMessages: List<Message> = emptyList(),
        val groupMembers: List<Member> = emptyList(),
        val messageInput: String? = null,
        val isLoading: Boolean = false
    )

    sealed class DetailGroupEvent {
        object Success : DetailGroupEvent()
        data class Failure(val message: String) : DetailGroupEvent()
    }

    private val _detailGroupState = MutableStateFlow(
        DetailGroupState(
            groupId = checkNotNull(
                savedStateHandle[Screen.DetailGroup.ARG_GROUP_ID]
            )
        )
    )
    val detailGroupState = _detailGroupState.asStateFlow()

    private val _detailGroupEvent = Channel<DetailGroupEvent>()
    val detailGroupEvent = _detailGroupEvent.receiveAsFlow()

    // init after state variables
    init {
        getCurrentUserId()
        getGroupInfo()
        getGroupMessage()
    }

    fun onGroupMessageInputChange(newValue: String){
        _detailGroupState.update { it.copy(messageInput = newValue) }
    }

    fun getCurrentUserId(){
        viewModelScope.launch {
            val currentUserId = getCurrentUserIdUseCase()
            _detailGroupState.update { it.copy(currentUserId = currentUserId) }
        }
    }

    fun getGroupMessage(){
        viewModelScope.launch {
            _detailGroupState.update { it.copy(isLoading = true) }

            val result = getGroupMessageUseCase(_detailGroupState.value.groupId)
            result.onSuccess { groupMessages ->
                _detailGroupState.update { it.copy(groupMessages = groupMessages, isLoading = false) }
                Timber.d(groupMessages.toString())
            }.onFailure { exception ->
                _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
                _detailGroupState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getGroupInfo(){
        viewModelScope.launch {
            val result = getGroupInfoUseCase(_detailGroupState.value.groupId)

            result.onSuccess {
                val group = result.getOrNull()

                _detailGroupState.update {
                    it.copy(
                        groupName = group?.name,
                        groupStatus = "${group?.members?.size} thành viên",
                        groupAvatar = group?.avatar,
                        groupMembers = group?.members ?: emptyList()
                    )
                }
            }.onFailure { exception ->
                _detailGroupEvent.send(DetailGroupEvent.Failure(exception.getErrorMessage()))
            }
        }
    }
}