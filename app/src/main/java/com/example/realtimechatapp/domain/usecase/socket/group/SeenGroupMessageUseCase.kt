package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.data.remote.dto.GroupMessageSeenDto
import com.example.realtimechatapp.domain.repository.SocketRepository
import javax.inject.Inject

class SeenGroupMessageUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    suspend operator fun invoke(messageSeen: GroupMessageSeenDto) {
        socketRepository.seenGroupMessage(messageSeen)
    }
}