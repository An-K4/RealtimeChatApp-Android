package com.example.realtimechatapp.ui.screens.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.auth.GetTokenUseCase
import com.example.realtimechatapp.domain.usecase.auth.LoginUseCase
import com.example.realtimechatapp.domain.usecase.auth.SignupUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val getTokenUseCase: GetTokenUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {
    sealed interface AuthDialogState {
        object Dismiss : AuthDialogState
        object AuthSuccess : AuthDialogState
        data class Failure(val message: UiText) : AuthDialogState
    }

    data class LoginState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val authDialogState: AuthDialogState = AuthDialogState.Dismiss
    )

    data class SignupState(
        val username: String = "",
        val password: String = "",
        val passwordRetype: String = "",
        val fullName: String = "",
        val email: String = "",
        val avatar: Uri? = null,
        val isLoading: Boolean = false,
        val authDialogState: AuthDialogState = AuthDialogState.Dismiss
    )

    sealed interface AuthEvent {
        object AuthSuccess : AuthEvent
        data class Failure(val message: UiText) : AuthEvent
    }

    private var _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow(SignupState())
    val signupState = _signupState.asStateFlow()

    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    fun onLoginUsernameChange(newValue: String) {
        _loginState.update { it.copy(username = newValue) }
    }

    fun onLoginPasswordChange(newValue: String) {
        _loginState.update { it.copy(password = newValue) }
    }

    fun onSignupUsernameChange(newValue: String) {
        _signupState.update { it.copy(username = newValue) }
    }

    fun onSignupPasswordChange(newValue: String) {
        _signupState.update { it.copy(password = newValue) }
    }

    fun onSignupPasswordRetypeChange(newValue: String) {
        _signupState.update { it.copy(passwordRetype = newValue) }
    }

    fun onSignupFullNameChange(newValue: String) {
        _signupState.update { it.copy(fullName = newValue) }
    }

    fun onSignupEmailChange(newValue: String) {
        _signupState.update { it.copy(email = newValue) }
    }

    fun onSignupAvatarChange(newValue: Uri?) {
        _signupState.update { it.copy(avatar = newValue) }
    }

    fun dismissLoginDialog() {
        _loginState.update { it.copy(authDialogState = AuthDialogState.Dismiss) }
    }

    fun dismissSignupDialog() {
        _signupState.update { it.copy(authDialogState = AuthDialogState.Dismiss) }
    }

    fun loginWithToken() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val token = getTokenUseCase()
                val currentUserId = getCurrentUserIdUseCase()

                if (token.isSuccess && currentUserId.isSuccess) {
                    _authEvent.send(AuthEvent.AuthSuccess)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _authEvent.send(AuthEvent.Failure(e.getErrorMessage()))
                Timber.d(e, "Lỗi đăng nhập bằng token")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true) }
            val result = with(_loginState.value) { loginUseCase(username, password) }
            _loginState.update { it.copy(isLoading = false) }

            result.onSuccess { user ->
                _authEvent.send(AuthEvent.AuthSuccess)
            }.onFailure { exception ->
                _loginState.update { it.copy(authDialogState = AuthDialogState.Failure(exception.getErrorMessage())) }
            }
        }
    }

    fun signup() {
        viewModelScope.launch {
            _signupState.update { it.copy(isLoading = true) }

            with(_signupState.value) {
                signupUseCase(avatar, username, password, passwordRetype, fullName, email)
            }.onSuccess {
                _signupState.update { it.copy(authDialogState = AuthDialogState.AuthSuccess, isLoading = false) }
            }.onFailure { exception ->
                _signupState.update { it.copy(authDialogState = AuthDialogState.Failure(exception.getErrorMessage()), isLoading = false) }
            }
        }
    }
}