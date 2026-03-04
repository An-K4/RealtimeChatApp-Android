package com.example.realtimechatapp.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.usecase.auth.LogoutUseCase
import com.example.realtimechatapp.domain.usecase.user.GetMeUseCase
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
    private val logoutUseCase: LogoutUseCase
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
        val isUpdateEnable: Boolean = false
    )

    sealed class ProfileEvent {
        object UpdateProfileSuccess : ProfileEvent()
        object ChangePasswordSuccess : ProfileEvent()
        object LogoutSuccess : ProfileEvent()
        data class Failure(val message: String) : ProfileEvent()
    }

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow(UpdateProfileSate())
    val updateProfileState = _updateProfileState.asStateFlow()

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
        _updateProfileState.update { it.copy( email = newValue ) }
        checkUpdateEnable()
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

    }

    fun logout() {
        viewModelScope.launch {
            val result = logoutUseCase()

            result.onSuccess {
                _profileEvent.send(ProfileEvent.LogoutSuccess)
            }.onFailure { exception ->
                _profileEvent.send(ProfileEvent.Failure(exception.getErrorMessage()))
            }
        }
    }
}