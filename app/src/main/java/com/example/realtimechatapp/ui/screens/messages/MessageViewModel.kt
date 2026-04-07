package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.data.local.manager.TokenManager
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.usecase.messages.GetMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveMessageContactUseCase
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
class MessageViewModel @Inject constructor(
    private val getMessageContactUseCase: GetMessageContactUseCase,
    private val observeMessageContactUseCase: ObserveMessageContactUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    data class MessageState(
        val isLoading: Boolean = false,
        val users: List<MessageContact> = emptyList()
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
        getUsers() // auto load
        observeMessageContacts()
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
            _messageState.update { it.copy(isLoading = true) }

            val result = getMessageContactUseCase()

            result.onSuccess { users ->
                _messageState.update {
                    it.copy(
                        isLoading = false,
                        users = users
                    )
                }
            }.onFailure { exception ->
                _messageState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun observeMessageContacts(){
        viewModelScope.launch {
            observeMessageContactUseCase().collect { updatedContactList ->
                val content = updatedContactList.map { it.lastMessage }

                _messageState.update {
                    Timber.d("Observe được gọi, cập nhật lại danh sách: $content")
                    it.copy(users = updatedContactList)
                }
            }
        }
    }
}
