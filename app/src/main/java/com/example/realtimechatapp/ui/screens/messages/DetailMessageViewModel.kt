package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.usecase.messages.GetMessageUseCase
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
class DetailMessageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMessageUseCase: GetMessageUseCase
): ViewModel(){
    data class DetailMessageState(
        val friendId: String = "",
        val messages: List<Message> = emptyList(),
        val messageInput: String? = null,
        val info: String? = null,
        val isLoading: Boolean = false
    )

    sealed class DetailMessageEvent{
        object GetMessageSuccess: DetailMessageEvent()
        data class Failure(val message: String): DetailMessageEvent()
    }

    private val _detailMessageState = MutableStateFlow(
        DetailMessageState(
            friendId = checkNotNull(
                savedStateHandle[Screen.DetailMessage.ARG_FRIEND_ID]
            )
        )
    )
    val detailMessageState = _detailMessageState.asStateFlow()

    fun onMessageInputChange(newValue: String){
        _detailMessageState.update { it.copy(messageInput = newValue) }
    }

    private val _detailMessageEvent = Channel<DetailMessageEvent>()
    val detailMessageEvent = _detailMessageEvent.receiveAsFlow()

    fun getMessages() {
        viewModelScope.launch {
            _detailMessageState.update { it.copy(isLoading = true) }

            getMessageUseCase(_detailMessageState.value.friendId).onSuccess { messages ->
                _detailMessageState.update { it.copy(messages = messages, isLoading = false) }
                Timber.log(1, detailMessageState.value.messages.toString())
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
                _detailMessageState.update { it.copy(isLoading = false) }
            }
        }
    }
}
