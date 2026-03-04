package com.example.realtimechatapp.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.auth.LogoutUseCase
import com.example.realtimechatapp.domain.usecase.user.ChangePasswordUseCase
import com.example.realtimechatapp.domain.usecase.user.GetMeUseCase
import com.example.realtimechatapp.domain.usecase.user.UpdateAvatarUseCase
import com.example.realtimechatapp.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMeUseCase: GetMeUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    data class ProfileState(
        val avatar: Any? = null,
        val fullName: String = "",
        val username: String = "",
        val email: String = "",
        val createdAt: String = "",
        val isLoading: Boolean = false
    )

    data class UpdateProfileSate(
        val avatar: Any? = null,
        val fullName: String = "",
        val email: String = "",
        val isUpdateEnable: Boolean = false,
        val isUpdating: Boolean = false
    )

    data class ChangePasswordState(
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmNewPassword: String = "",
        val isChangePasswordEnable: Boolean = false,
        val isChanging: Boolean = false
    )

    sealed class ProfileEvent {
        object UpdateProfileSuccess : ProfileEvent()
        object ChangePasswordSuccess : ProfileEvent()
        object LogoutSuccess : ProfileEvent()
        object NavigateToLogin : ProfileEvent()
        data class Failure(val message: String) : ProfileEvent()
    }

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow(UpdateProfileSate())
    val updateProfileState = _updateProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState = _changePasswordState.asStateFlow()

    private val _profileEvent = Channel<ProfileEvent>()
    val profileEvent = _profileEvent.receiveAsFlow()

    fun onUpdateAvatarChange(newValue: Any?) {
        if (newValue is String || newValue is Uri || newValue == null) {
            _updateProfileState.update { it.copy(avatar = newValue) }
        }
        checkUpdateEnable()
    }

    fun onUpdateFullNameChange(newValue: String) {
        _updateProfileState.update { it.copy( fullName = newValue) }
        checkUpdateEnable()
    }

    fun onUpdateEmailChange(newValue: String) {
        _updateProfileState.update { it.copy( email = newValue) }
        checkUpdateEnable()
    }

    fun onOldPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy( oldPassword = newValue ) }
        checkChangePasswordEnable()
    }

    fun onNewPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy( newPassword = newValue ) }
        checkChangePasswordEnable()
    }

    fun onConfirmNewPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy( confirmNewPassword = newValue ) }
        checkChangePasswordEnable()
    }

    fun initUpdateSheet() {
        _updateProfileState.update {
            it.copy(
                avatar = profileState.value.avatar,
                fullName = profileState.value.fullName,
                email = profileState.value.email,
                isUpdateEnable = false
            )
        }
    }

    fun checkUpdateEnable(){
        val original = _profileState.value
        val update = _updateProfileState.value

        val isNotEmpty = update.fullName.isNotBlank() && update.email.isNotBlank()
        val isChanged = (update.fullName != original.fullName)
                || (update.email != original.email)
                || (update.avatar != original.avatar)

        _updateProfileState.update { it.copy(isUpdateEnable = isNotEmpty && isChanged) }
    }

    fun checkChangePasswordEnable(){
        val currentValue = _changePasswordState.value
        val isNotEmpty = currentValue.oldPassword.isNotBlank()
                && currentValue.newPassword.isNotBlank()
                && currentValue.confirmNewPassword.isNotBlank()

        _changePasswordState.update { it.copy(isChangePasswordEnable = isNotEmpty) }
    }

    fun getMe() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }
            val result = getMeUseCase()

            result.onSuccess {
                val currentUser = result.getOrNull()
                _profileState.update {
                    it.copy(
                        avatar = currentUser?.avatar,
                        fullName = currentUser?.fullName ?: "no one knows",
                        username = currentUser?.username ?: "no one knows",
                        email = currentUser?.email ?: "no one knows",
                        createdAt = currentUser?.createdAt ?: "no one knows",
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _profileEvent.send(ProfileEvent.Failure(exception.getErrorMessage()))
                _profileState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateProfile() {
        val original = _profileState.value
        val current = _updateProfileState.value
        val isAvatarChanged = original.avatar != current.avatar
        val isInfoChanged = original.fullName != current.fullName
                || original.email != current.email

        viewModelScope.launch {
            try {
                _updateProfileState.update { it.copy(isUpdating = true) }

                when{
                    isAvatarChanged && isInfoChanged -> {
                        val avatar = _updateProfileState.value.avatar as? Uri ?: run{
                            _profileEvent.send(ProfileEvent.Failure("Avatar không hợp lệ."))
                            return@launch
                        }
                        val file = FileUtils.getFileFromUri(context, avatar)
                        val compressedAvatar = file?.let { ImageUtils.compressImageFile(file) }

                        if (compressedAvatar == null){
                            _profileEvent.send(ProfileEvent.Failure("Xử lý ảnh không thành công."))
                            return@launch
                        }

                        val avatarDeferred = async { updateAvatarUseCase(compressedAvatar) }
                        val infoDeferred = async { updateProfileUseCase(current.fullName, current.email) }

                        val updateAvatarResult = avatarDeferred.await()
                        val updateInfoResult = infoDeferred.await()

                        if (updateInfoResult.isSuccess && updateAvatarResult.isSuccess){
                            val updatedInfo = updateInfoResult.getOrNull()

                            _profileState.update {
                                it.copy(
                                    avatar = updateAvatarResult.getOrNull(),
                                    fullName = updatedInfo?.fullName?:original.fullName,
                                    email = updatedInfo?.email?:original.email
                                )
                            }

                            _profileEvent.send(ProfileEvent.UpdateProfileSuccess)
                        } else {
                            val errors = mutableListOf<String>()
                            updateInfoResult.exceptionOrNull()?.let { errors.add("Profile: ${it.getErrorMessage()}") }
                            updateAvatarResult.exceptionOrNull()?.let { errors.add("Avatar: ${it.getErrorMessage()}") }
                            _profileEvent.send(ProfileEvent.Failure(errors.joinToString("\n")))
                        }
                    }
                    isAvatarChanged -> {
                        val avatar = _updateProfileState.value.avatar as? Uri ?: run {
                            _profileEvent.send(ProfileEvent.Failure("Avatar không hợp lệ."))
                            return@launch
                        }
                        val file = FileUtils.getFileFromUri(context, avatar)
                        val compressedAvatar = file?.let { ImageUtils.compressImageFile(file) }

                        if (compressedAvatar == null){
                            _profileEvent.send(ProfileEvent.Failure("Xử lý ảnh không thành công."))
                            return@launch
                        }

                        val updateAvatarResult = updateAvatarUseCase(compressedAvatar)
                        if (updateAvatarResult.isSuccess){
                            _profileState.update { it.copy(avatar = updateAvatarResult.getOrNull()) }
                            _profileEvent.send(ProfileEvent.UpdateProfileSuccess)
                        } else {
                            _profileEvent.send(
                                ProfileEvent.Failure(
                                    updateAvatarResult.exceptionOrNull()?.getErrorMessage()
                                        ?: "Có lỗi gì đó đã xảy ra."
                                )
                            )
                        }
                    }
                    isInfoChanged -> {
                        val updateInfoResult = updateProfileUseCase(current.fullName, current.email)
                        if (updateInfoResult.isSuccess){
                            val updatedUser = updateInfoResult.getOrNull()

                            _profileState.update {
                                it.copy(
                                    fullName = updatedUser?.fullName ?: original.fullName,
                                    email = updatedUser?.email ?: original.email
                                )
                            }
                            _profileEvent.send(ProfileEvent.UpdateProfileSuccess)
                        } else {
                            _profileEvent.send(
                                ProfileEvent.Failure(
                                    updateInfoResult.exceptionOrNull()?.getErrorMessage()
                                        ?: "Có lỗi gì đó đã xảy ra."
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _profileEvent.send(ProfileEvent.Failure("Lỗi: ${e.getErrorMessage()}"))
            } finally {
                _updateProfileState.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun changePassword(){
        val oldPassword = _changePasswordState.value.oldPassword
        val newPassword = _changePasswordState.value.newPassword
        val confirmNewPassword = _changePasswordState.value.confirmNewPassword

        viewModelScope.launch {
            _changePasswordState.update { it.copy(isChanging = true) }

            if (newPassword != confirmNewPassword){
                _profileEvent.send(ProfileEvent.Failure("Mật khẩu xác nhận không trùng khớp với mật khẩu mới."))
                return@launch
            }

            val result = changePasswordUseCase(oldPassword, newPassword)
            result.onSuccess {
                _profileEvent.send(ProfileEvent.ChangePasswordSuccess)
            }.onFailure {
                _profileEvent.send(ProfileEvent.Failure(it.getErrorMessage()))
            }
        }
    }

    fun logout(showLogoutSuccessDialog: Boolean) {
        viewModelScope.launch {
            val result = logoutUseCase()

            result.onSuccess {
                if (showLogoutSuccessDialog){
                    _profileEvent.send(ProfileEvent.LogoutSuccess)
                } else {
                    _profileEvent.send(ProfileEvent.NavigateToLogin)
                }
            }.onFailure { exception ->
                _profileEvent.send(ProfileEvent.Failure(exception.getErrorMessage()))
            }
        }
    }
}