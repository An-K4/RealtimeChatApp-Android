package com.example.realtimechatapp.ui.screens.messages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.usecase.messages.GetMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ConnectSocketUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveTypingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getMessageContactUseCase: GetMessageContactUseCase,
    private val observeMessageContactUseCase: ObserveMessageContactUseCase,
    private val observeOnlineUserUseCase: ObserveOnlineUserUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val connectSocketUseCase: ConnectSocketUseCase,
    private val tokenManager: TokenManagerImpl,
    @ApplicationContext private val context: Context
) : ViewModel() {
    data class MessageState(
        val isLoading: Boolean = false,
        val users: List<MessageContact> = emptyList()
    )

    sealed class MessageEvent {
        object Authenticated : MessageEvent()
        object Unauthenticated : MessageEvent()
        data class Failure(val message: String) : MessageEvent()
    }

    private val _isLoading = MutableStateFlow(false)
    val messageState = combine(
        observeMessageContactUseCase().catch { exception ->
            Timber.e("Lỗi luồng lấy danh sách người dùng: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        observeOnlineUserUseCase().catch { exception ->
            Timber.e("Lỗi luồng user online: ${exception.getErrorMessage()}")
            emit(emptySet())
        },
        observeTypingUseCase().catch { exception ->
            Timber.e("Lỗi luồng user typing: ${exception.getErrorMessage()}")
            emit(emptySet())
        },
        _isLoading
    ) { messageContacts, onlineUserIds, typingUserIds, isLoading ->
        MessageState(
            isLoading = isLoading && messageContacts.isEmpty(),
            users = messageContacts.map { contact ->
                contact.copy(
                    isOnline = onlineUserIds.contains(contact.id),
                    isTyping = typingUserIds.contains(contact.id)
                )
            }
        )
    }.catch { exception ->
        Timber.e("Lỗi luồng màn hình tin nhắn: ${exception.getErrorMessage().asString(context)}")
        _messageEvent.send(MessageEvent.Failure(exception.getErrorMessage().asString(context)))
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

    fun checkToken() {
        viewModelScope.launch {
            tokenManager.token.collect { token ->
                if (token.isNullOrEmpty()) {
                    _messageEvent.send(MessageEvent.Unauthenticated)
                }
            }
        }
    }

    fun connectSocket() {
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
                _messageEvent.send(
                    MessageEvent.Failure(
                        exception.getErrorMessage().asString(context)
                    )
                )
            }

            _isLoading.value = false
        }
    }
}
