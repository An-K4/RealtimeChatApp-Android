package com.example.realtimechatapp.domain.usecase.socket.message

import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOnlineUserUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    operator fun invoke(): Flow<Set<String>> = socketRepository.observeOnlineUserIds()
}