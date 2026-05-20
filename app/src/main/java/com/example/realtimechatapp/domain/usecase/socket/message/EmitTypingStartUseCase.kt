package com.example.realtimechatapp.domain.usecase.socket.message

import com.example.realtimechatapp.domain.repository.SocketRepository
import javax.inject.Inject

class EmitTypingStartUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    suspend operator fun invoke(receiverId: String){
        socketRepository.emitTypingStart(receiverId)
    }
}