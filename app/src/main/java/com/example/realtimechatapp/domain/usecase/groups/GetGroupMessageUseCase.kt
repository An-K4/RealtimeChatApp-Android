package com.example.realtimechatapp.domain.usecase.groups

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupMessageUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Message>>{
        if (groupId.isBlank()) return Result.failure(Exception("ID nhóm không hợp lệ"))

        return groupRepository.getGroupMessage(groupId)
    }
}