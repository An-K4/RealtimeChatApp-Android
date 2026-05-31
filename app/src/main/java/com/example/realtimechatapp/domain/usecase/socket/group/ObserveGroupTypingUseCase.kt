package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.model.GroupTypingUser
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveGroupTypingUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    operator fun invoke(groupId: String): Flow<List<GroupTypingUser>> {
        return socketRepository.observeGroupTypingStatus().map { typingMap ->
            typingMap[groupId]?.toList() ?: emptyList()
        }
    }
}