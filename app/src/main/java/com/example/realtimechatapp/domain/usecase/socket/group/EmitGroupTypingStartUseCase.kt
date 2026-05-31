package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.repository.SocketRepository
import javax.inject.Inject

class EmitGroupTypingStartUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    suspend operator fun invoke(groupId: String) {
        socketRepository.emitGroupTypingStart(groupId)
    }
}