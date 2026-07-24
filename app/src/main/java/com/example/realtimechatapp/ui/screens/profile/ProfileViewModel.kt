package com.example.realtimechatapp.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.auth.LogoutUseCase
import com.example.realtimechatapp.domain.usecase.socket.DisconnectSocketUseCase
import com.example.realtimechatapp.domain.usecase.user.ChangePasswordUseCase
import com.example.realtimechatapp.domain.usecase.user.GetMeUseCase
import com.example.realtimechatapp.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val disconnectSocketUseCase: DisconnectSocketUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    data class ProfileState(
        val avatar: Any? = null,
        val fullName: String = "",
        val username: String = "",
        val email: String = "",
        val createdAt: String = "",
        val sheetState: ProfileSheetState = ProfileSheetState.Dismiss,
        val dialogState: ProfileDialogState = ProfileDialogState.Dismiss,
        val isUpdating: Boolean = false,
        val isChanging: Boolean = false,
        val isLoading: Boolean = false
    )

    data class UpdateProfileSate(
        val avatar: Any? = null,
        val fullName: String = "",
        val email: String = "",
        val isUpdateEnable: Boolean = false
    )

    data class ChangePasswordState(
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmNewPassword: String = "",
        val isChangePasswordEnable: Boolean = false
    )

    sealed interface ProfileSheetState {
        object Dismiss : ProfileSheetState
        object UpdateProfile : ProfileSheetState
        object ChangePassword : ProfileSheetState
    }

    sealed interface ProfileDialogState {
        object Dismiss : ProfileDialogState
        object UpdateProfileConfirm : ProfileDialogState
        object UpdateProfileSuccess : ProfileDialogState
        object ChangePasswordConfirm : ProfileDialogState
        object ChangePasswordSuccess : ProfileDialogState
        object LogoutConfirm : ProfileDialogState
        object LogoutSuccess : ProfileDialogState
        data class Failure(val message: UiText) : ProfileDialogState
    }

    sealed interface ProfileEvent {
        object NavigateToLogin : ProfileEvent
        data class Failure(val message: UiText) : ProfileEvent
    }

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow(UpdateProfileSate())
    val updateProfileState = _updateProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState = _changePasswordState.asStateFlow()

    private val _profileEvent = Channel<ProfileEvent>()
    val profileEvent = _profileEvent.receiveAsFlow()

    // init after state variables
    init {
        getMe()
    }

    fun onUpdateAvatarChange(newValue: Any?) {
        if (newValue is String || newValue is Uri || newValue == null) {
            _updateProfileState.update { it.copy(avatar = newValue) }
        }
        checkUpdateEnable()
    }

    fun onUpdateFullNameChange(newValue: String) {
        _updateProfileState.update { it.copy(fullName = newValue) }
        checkUpdateEnable()
    }

    fun onUpdateEmailChange(newValue: String) {
        _updateProfileState.update { it.copy(email = newValue) }
        checkUpdateEnable()
    }

    fun onOldPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy(oldPassword = newValue) }
        checkChangePasswordEnable()
    }

    fun onNewPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy(newPassword = newValue) }
        checkChangePasswordEnable()
    }

    fun onConfirmNewPasswordChange(newValue: String) {
        _changePasswordState.update { it.copy(confirmNewPassword = newValue) }
        checkChangePasswordEnable()
    }

    private fun initUpdateProfileSheet() {
        _updateProfileState.update {
            it.copy(
                avatar = profileState.value.avatar,
                fullName = profileState.value.fullName,
                email = profileState.value.email,
                isUpdateEnable = false
            )
        }
    }

    private fun initChangePasswordSheet() {
        _changePasswordState.update {
            it.copy(
                oldPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                isChangePasswordEnable = false
            )
        }
    }

    fun checkUpdateEnable() {
        val original = _profileState.value
        val update = _updateProfileState.value

        val isNotEmpty = update.fullName.isNotBlank() && update.email.isNotBlank()
        val isChanged =
            (update.fullName != original.fullName) || (update.email != original.email) || (update.avatar != original.avatar)

        _updateProfileState.update { it.copy(isUpdateEnable = isNotEmpty && isChanged) }
    }

    fun checkChangePasswordEnable() {
        val currentValue = _changePasswordState.value
        val isNotEmpty =
            currentValue.oldPassword.isNotBlank() && currentValue.newPassword.isNotBlank() && currentValue.confirmNewPassword.isNotBlank()

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
                _profileEvent.send(
                    ProfileEvent.Failure(exception.getErrorMessage())
                )
                _profileState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showUpdateProfileSheet() {
        initUpdateProfileSheet()
        viewModelScope.launch {
            _profileState.update { it.copy(sheetState = ProfileSheetState.UpdateProfile) }
        }
    }

    fun showUpdateProfileConfirmDialog() {
        viewModelScope.launch {
            _profileState.update { it.copy(dialogState = ProfileDialogState.UpdateProfileConfirm) }
        }
    }

    fun updateProfile() {
        val original = _profileState.value
        val current = _updateProfileState.value
        val isAvatarChanged = original.avatar != current.avatar

        viewModelScope.launch {
            _profileState.update { it.copy(isUpdating = true) }
            val updateProfileResult = updateProfileUseCase(
                current.fullName,
                current.email,
                current.avatar as? Uri,
                isAvatarChanged
            )

            updateProfileResult.onSuccess { updatedUser ->
                _profileState.update {
                    it.copy(
                        avatar = updatedUser.avatar,
                        fullName = updatedUser.fullName,
                        email = updatedUser.email
                    )
                }

                _profileState.update {
                    it.copy(
                        dialogState = ProfileDialogState.UpdateProfileSuccess,
                        isUpdating = false
                    )
                }
            }.onFailure { exception ->
                _profileState.update {
                    it.copy(
                        dialogState = ProfileDialogState.Failure(
                            exception.getErrorMessage()
                        ),
                        isUpdating = false
                    )
                }
            }
        }
    }

    fun showChangePasswordSheet() {
        initChangePasswordSheet()
        viewModelScope.launch {
            _profileState.update { it.copy(sheetState = ProfileSheetState.ChangePassword) }
        }
    }

    fun showChangePasswordConfirmDialog() {
        viewModelScope.launch {
            _profileState.update { it.copy(dialogState = ProfileDialogState.ChangePasswordConfirm) }
        }
    }

    fun changePassword() {
        val oldPassword = _changePasswordState.value.oldPassword
        val newPassword = _changePasswordState.value.newPassword
        val confirmNewPassword = _changePasswordState.value.confirmNewPassword

        viewModelScope.launch {
            _profileState.update { it.copy(isChanging = true) }

            changePasswordUseCase(oldPassword, newPassword, confirmNewPassword).onSuccess {
                _profileState.update {
                    it.copy(
                        dialogState = ProfileDialogState.ChangePasswordSuccess,
                        isChanging = false
                    )
                }
            }.onFailure { exception ->
                _profileState.update {
                    it.copy(
                        dialogState = ProfileDialogState.Failure(
                            exception.getErrorMessage()
                        ),
                        isChanging = false
                    )
                }
            }
        }
    }

    fun showLogoutConfirmDialog() {
        viewModelScope.launch {
            _profileState.update {
                it.copy(dialogState = ProfileDialogState.LogoutConfirm)
            }
        }
    }

    fun logout(showLogoutSuccessDialog: Boolean) {
        viewModelScope.launch {
            disconnectSocketUseCase()
            logoutUseCase().onSuccess {
                if (showLogoutSuccessDialog) {
                    _profileState.update { it.copy(dialogState = ProfileDialogState.LogoutSuccess) }
                } else {
                    _profileEvent.send(ProfileEvent.NavigateToLogin)
                }
            }.onFailure { exception ->
                _profileState.update {
                    it.copy(
                        dialogState = ProfileDialogState.Failure(
                            exception.getErrorMessage()
                        )
                    )
                }
            }
        }
    }

    fun dismissSheet() {
        _profileState.update { it.copy(sheetState = ProfileSheetState.Dismiss) }
    }

    fun dismissDialog() {
        _profileState.update { it.copy(dialogState = ProfileDialogState.Dismiss) }
    }
}