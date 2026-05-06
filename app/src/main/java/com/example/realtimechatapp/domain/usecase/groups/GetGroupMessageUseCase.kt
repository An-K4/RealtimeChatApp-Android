package com.example.realtimechatapp.domain.usecase.groups

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class GetGroupMessageUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Message>>{
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            groupRepository.getGroupMessage(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}