package com.example.realtimechatapp.ui.screens.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.LoginUseCase
import com.example.realtimechatapp.domain.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    @ApplicationContext private val context: Context
): ViewModel() {

    sealed class UiEvent {
        object Success: UiEvent()
        data class ShowToast(val message: String): UiEvent()
    }

    private val _username = MutableStateFlow("")
    val username : StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password : StateFlow<String> = _password.asStateFlow()

    private val _passwordRetype = MutableStateFlow("")
    val passwordRetype : StateFlow<String> = _passwordRetype.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName : StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email : StateFlow<String> = _email.asStateFlow()

    private val _avatar = MutableStateFlow(null as Uri?)
    val avatar : StateFlow<Uri?> = _avatar.asStateFlow()

    fun onUsernameChange(newValue: String){
        _username.value = newValue
    }

    fun onPasswordChange(newValue: String){
        _password.value = newValue
    }

    fun onPasswordRetypeChange(newValue: String){
        _passwordRetype.value = newValue
    }

    fun onFullNameChange(newValue: String){
        _fullName.value = newValue
    }

    fun onEmailChange(newValue: String){
        _email.value = newValue
    }

    fun onAvatarChange(newValue: Uri?){
        _avatar.value = newValue
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
                _uiEvent.send(UiEvent.Success)
            }.onFailure { exception ->
                _uiEvent.send(UiEvent.ShowToast("Lỗi: " + (exception.message ?: "không xác định")))
            }
        }
    }

    fun signup(){
        viewModelScope.launch {
            var compressAvatar: File? = null

            if (password.value != passwordRetype.value){
                _uiEvent.send(UiEvent.ShowToast("Mật khẩu xác nhận không khớp"))
            } else {
                _isLoading.value = true

                avatar.value?.let { uri ->
                    Timber.d("Avatar tải lên: %s", uri)
                    val file = FileUtils.getFileFromUri(context, uri)
                    file?.let {
                        Timber.d("Lấy file từ uri thành công, bắt đầu nén: %s", file.absolutePath)
                        compressAvatar = ImageUtils.compressImageFile(it)
                    }
                }

                val result = signupUseCase(
                    compressAvatar,
                    username.value,
                    password.value,
                    fullName.value,
                    email.value
                )

                result.onSuccess{ message ->
                    _uiEvent.send(UiEvent.ShowToast(message))
                    _uiEvent.send(UiEvent.Success)
                }.onFailure { exception ->
                    _uiEvent.send(UiEvent.ShowToast(exception.getErrorMessage()))
                }

                _isLoading.value = false
            }
        }
    }
}