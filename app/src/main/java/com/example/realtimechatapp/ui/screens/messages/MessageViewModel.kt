package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.data.local.TokenManager
import com.example.realtimechatapp.domain.model.UserContact
import com.example.realtimechatapp.domain.usecase.messages.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    data class MessageState(
        val isLoading: Boolean = false,
        val users: List<UserContact> = emptyList(),
        val info: String? = null
    )

    sealed class MessageEvent{
        object Authenticated: MessageEvent()
        object Unauthenticated: MessageEvent()
    }

    private val _messageState = MutableStateFlow(MessageState())
    val messageState = _messageState.asStateFlow()

    private val _messageEvent = Channel<MessageEvent>()
    val messageEvent = _messageEvent.receiveAsFlow()

    init {
        checkToken()
    }

    fun checkToken(){
        viewModelScope.launch {
            tokenManager.token.collect { token ->
                if (token.isNullOrEmpty()){
                    _messageEvent.send(MessageEvent.Unauthenticated)
                }
            }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            _messageState.update { it.copy(isLoading = true, info = null) }

            getUsersUseCase().onSuccess { users ->
                _messageState.update {
                    it.copy(
                        isLoading = false,
                        users = users,
                        info = if (users.isEmpty()) "Hãy tìm 1 người bạn và bắt đầu trò chuyện nào!" else null
                    )
                }
            }.onFailure { exception ->
                _messageState.update {
                    it.copy(
                        isLoading = false,
                        info = "${exception.message}"
                    )
                }
            }
        }
    }
}