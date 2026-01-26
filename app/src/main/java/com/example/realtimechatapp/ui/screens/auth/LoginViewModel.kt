package com.example.realtimechatapp.ui.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository): ViewModel() {

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

    private val _loginState = mutableStateOf(LoginState())
    val loginState = _loginState

    fun login(){
        if(username.value.isBlank() || password.value.isBlank()){
            _loginState.value = LoginState(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            val result = authRepository.login(username.value, password.value)

            result.onSuccess { user ->
                _loginState.value = LoginState(user = user)
            }.onFailure { exception ->
                _loginState.value = LoginState(error = exception.message ?: "Lỗi không xác định")
            }
        }
    }
}