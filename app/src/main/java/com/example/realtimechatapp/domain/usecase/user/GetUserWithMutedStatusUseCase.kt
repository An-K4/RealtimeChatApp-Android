package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class GetUserWithMutedStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> =
        userRepository.getUserWithMutedStatus(userId)
}