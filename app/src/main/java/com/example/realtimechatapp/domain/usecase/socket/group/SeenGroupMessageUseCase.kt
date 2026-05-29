package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class SeenGroupMessageUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String) {
        groupRepository.seenGroupMessage(groupId)
    }
}