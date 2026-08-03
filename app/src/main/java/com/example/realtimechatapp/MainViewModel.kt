package com.example.realtimechatapp

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.ThemeMode
import com.example.realtimechatapp.domain.usecase.RequestNotificationPermissionUseCase
import com.example.realtimechatapp.domain.usecase.auth.SyncFcmTokenUseCase
import com.example.realtimechatapp.domain.usecase.config.GetCurrentLanguageUseCase
import com.example.realtimechatapp.domain.usecase.config.GetCurrentThemeUseCase
import com.example.realtimechatapp.domain.usecase.socket.ConnectSocketUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val connectSocketUseCase: ConnectSocketUseCase
): ViewModel() {

    init {
        // Get FCM token on app start
        getFcmToken()
    }
    data class MainViewModelState(
        val isLoading: Boolean = true,
        val currentLanguage: AppLanguage,
        val currentTheme: ThemeMode
    )

    val mainViewModelState = combine(
        getCurrentLanguageUseCase().catch { exception ->
            Timber.d("Lỗi lấy ngôn ngữ hiện tại: ${exception.message}")
            emit(AppLanguage.VIETNAMESE)
        },
        getCurrentThemeUseCase().catch { exception ->
            Timber.d("Lỗi lấy giao diện hiện tại: ${exception.message}")
            emit(ThemeMode.LIGHT)
        }
    ) { currentLanguage, currentTheme ->
        MainViewModelState(false, currentLanguage, currentTheme)
    }.catch { exception ->
        Timber.d("Lỗi cấu hình ứng dụng: ${exception.message}")
        emit(MainViewModelState(false,AppLanguage.VIETNAMESE, ThemeMode.LIGHT))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainViewModelState(true, AppLanguage.VIETNAMESE, ThemeMode.LIGHT)
    )

    private fun getFcmToken() {
        viewModelScope.launch {
            try {
                // Check if user is authenticated before syncing
                val currentUserId = getCurrentUserIdUseCase()
                if (currentUserId.isFailure) {
                    Timber.d("User not authenticated, skip FCM token sync")
                    return@launch
                }
                
                // User is authenticated, proceed with socket connection and FCM sync
                // Connect socket at app-level (not dependent on MessageScreen)
                viewModelScope.launch {
                    connectSocketUseCase()
                }
                
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    Timber.d("FCM Token: $token")

                    // Sync to server
                    viewModelScope.launch {
                        syncFcmTokenUseCase(token)
                    }
                }.addOnFailureListener { e ->
                    Timber.e(e, "Failed to get FCM token")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error getting FCM token")
            }
        }
    }

    fun requestNotificationPermission(activity: ComponentActivity) {
        requestNotificationPermissionUseCase(activity)
    }
}