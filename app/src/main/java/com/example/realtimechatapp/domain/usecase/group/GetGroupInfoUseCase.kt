package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class GetGroupInfoUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<Group>{
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            groupRepository.getGroupInfo(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}