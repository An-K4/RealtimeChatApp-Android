package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.repository.CurrentUserManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserIdUseCase @Inject constructor(
    private val currentUserManager: CurrentUserManager
) {
    operator fun invoke(): Flow<String> = currentUserManager.observeCurrentUser()
}