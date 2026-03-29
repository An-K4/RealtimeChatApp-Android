package com.example.realtimechatapp.domain.usecase

import com.example.realtimechatapp.domain.repository.CurrentUserManager
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val currentUserManager: CurrentUserManager
) {
    suspend operator fun invoke(): String = currentUserManager.getCurrentUserId()
}