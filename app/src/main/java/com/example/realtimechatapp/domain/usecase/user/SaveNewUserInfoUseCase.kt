package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class SaveNewUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(newUser: User): Result<Unit> {
        return userRepository.saveNewUserInfo(newUser)
    }
}