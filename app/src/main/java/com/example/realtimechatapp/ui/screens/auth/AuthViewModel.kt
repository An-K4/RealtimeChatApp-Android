package com.example.realtimechatapp.ui.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val loginUseCase: LoginUseCase): ViewModel() {

    sealed class UiEvent {
        object LoginSuccess: UiEvent()
        data class ShowToast(val message: String): UiEvent()
    }

    private val _username = mutableStateOf("")
    val username = _username

    private val _password = mutableStateOf("")
    val password = _password

    fun onUsernameChange(newValue: String){
        _username.value = newValue
    }

    fun onPasswordChange(newValue: String){
        _password.value = newValue
    }

    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun login(){
        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(username.value, password.value)
            _isLoading.value = false

            result.onSuccess { user ->
                _uiEvent.send(UiEvent.ShowToast("Đăng nhập thành công!"))
                _uiEvent.send(UiEvent.LoginSuccess)
            }.onFailure { exception ->
                _uiEvent.send(UiEvent.ShowToast("Lỗi: " + (exception.message ?: "không xác định")))
            }
        }
    }
}