package com.example.realtimechatapp.domain.usecase.auth

import com.example.realtimechatapp.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SyncFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return try {
            // Update local DB
            userRepository.updateFcmToken(token)
            
            // Sync to server
            userRepository.syncFcmTokenToServer(token)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to sync FCM token")
            Result.failure(e)
        }
    }
}
