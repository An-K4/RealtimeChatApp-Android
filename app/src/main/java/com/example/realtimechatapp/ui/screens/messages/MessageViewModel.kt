package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.model.UserContact
import com.example.realtimechatapp.domain.usecase.messages.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {
    data class UserListUiState(
        val isLoading: Boolean = false,
        val users: List<UserContact> = emptyList(),
        val info: String? = null
    )

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState = _uiState.asStateFlow()

    fun getUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, info = null) }

            getUsersUseCase().onSuccess { users ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = users,
                        info = if (users.isEmpty()) "Hãy tìm 1 người bạn và bắt đầu trò chuyện nào!" else null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        info = "${exception.message}"
                    )
                }
            }
        }
    }
}