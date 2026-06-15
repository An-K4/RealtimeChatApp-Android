package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.model.SearchResult
import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class PerformSearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<SearchResult> {
        return userRepository.performSearchUsers(query)
    }
}