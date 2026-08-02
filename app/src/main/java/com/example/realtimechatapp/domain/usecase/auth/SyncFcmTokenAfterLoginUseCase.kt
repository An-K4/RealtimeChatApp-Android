package com.example.realtimechatapp.domain.usecase.auth

import com.example.realtimechatapp.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to sync FCM token to server after successful login.
 * This ensures the user receives push notifications immediately after logging in.
 */
class SyncFcmTokenAfterLoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        try {
            // Get FCM token from Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            Timber.d("FCM Token after login: $token")
            
            // Sync to server
            val result = userRepository.syncFcmTokenToServer(token)
            
            result.onSuccess {
                Timber.d("FCM token synced successfully after login")
            }.onFailure { e ->
                Timber.e(e, "Failed to sync FCM token after login")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting FCM token after login")
        }
    }
}
