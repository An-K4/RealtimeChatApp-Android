package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class GetGroupMessagesWithAttachmentsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        groupId: String,
        limit: Int = 30,
        offset: Int = 0
    ): Result<List<Message>> {
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            groupRepository.getGroupMessagesWithAttachments(groupId, limit, offset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
