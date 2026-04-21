package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.manager.TokenManager
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.usecase.messages.GetMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ConnectSocketUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveTypingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getMessageContactUseCase: GetMessageContactUseCase,
    private val observeMessageContactUseCase: ObserveMessageContactUseCase,
    private val observeOnlineUserUseCase: ObserveOnlineUserUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val connectSocketUseCase: ConnectSocketUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    data class MessageState(
        val isLoading: Boolean = false,
        val users: List<MessageContact> = emptyList()
    )

    sealed class MessageEvent{
        object Authenticated: MessageEvent()
        object Unauthenticated: MessageEvent()
        data class Failure(val message: String): MessageEvent()
    }

    private val _isLoading = MutableStateFlow(false)
    val messageState = combine(
        observeMessageContactUseCase(),
        observeOnlineUserUseCase(),
        observeTypingUseCase(),
        _isLoading
    ){ messageContacts, onlineUserIds, typingUserIds, isLoading ->
        MessageState(
            isLoading = isLoading && messageContacts.isEmpty(),
            users = messageContacts.map { contact ->
                contact.copy(
                    isOnline = onlineUserIds.contains(contact.id),
                    isTyping = typingUserIds.contains(contact.id)
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MessageState(isLoading = true)
    )

    private val _messageEvent = Channel<MessageEvent>()
    val messageEvent = _messageEvent.receiveAsFlow()

    init {
        checkToken()
        connectSocket()
        getUsers() // auto load
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

    fun connectSocket(){
        viewModelScope.launch {
            connectSocketUseCase()
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            _isLoading.value = true

            getMessageContactUseCase().onSuccess {
                // do nothing, delegate to observe fun
            }.onFailure { exception ->
                _messageEvent.send(MessageEvent.Failure(exception.getErrorMessage()))
            }

            _isLoading.value = false
        }
    }
}
