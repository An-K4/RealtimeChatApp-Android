package com.example.realtimechatapp.ui.screens.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.TokenManager
import com.example.realtimechatapp.domain.usecase.auth.LoginUseCase
import com.example.realtimechatapp.domain.usecase.auth.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class LoginState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false
    )

    data class SignupState(
        val username: String = "",
        val password: String = "",
        val passwordRetype: String = "",
        val fullName: String = "",
        val email: String = "",
        val avatar: Uri? = null,
        val isLoading: Boolean = false
    )

    sealed class AuthEvent {
        object AuthSuccess : AuthEvent()
        data class Failure(val message: String) : AuthEvent()
    }

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

    private var _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow(SignupState())
    val signupState = _signupState.asStateFlow()

    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    fun loginWithToken() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val token = tokenManager.token.first()

                if (token.isNullOrEmpty()){
                    _isLoading.value = false
                } else {
                    _authEvent.send(AuthEvent.AuthSuccess)
                }
            } catch (e: Exception){
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
                _authEvent.send(AuthEvent.Failure(exception.getErrorMessage()))
            }
        }
    }

    fun signup() {
        viewModelScope.launch {
            var compressAvatar: File? = null

            if (_signupState.value.password != _signupState.value.passwordRetype) {
                _authEvent.send(AuthEvent.Failure("Mật khẩu xác nhận không khớp"))
            } else {
                _signupState.update { it.copy(isLoading = true) }

                _signupState.value.avatar?.let { uri ->
                    Timber.d("Avatar tải lên: %s", uri)
                    val file = FileUtils.getFileFromUri(context, uri)
                    file?.let {
                        Timber.d("Lấy file từ uri thành công, bắt đầu nén: %s", file.absolutePath)
                        compressAvatar = ImageUtils.compressImageFile(it)
                    }
                }

                val result = with(_signupState.value) {
                    signupUseCase(compressAvatar, username, password, fullName, email)
                }

                result.onSuccess { message ->
                    _authEvent.send(AuthEvent.AuthSuccess)
                }.onFailure { exception ->
                    _authEvent.send(AuthEvent.Failure(exception.getErrorMessage()))
                }

                _signupState.update { it.copy(isLoading = false) }
            }
        }
    }
}