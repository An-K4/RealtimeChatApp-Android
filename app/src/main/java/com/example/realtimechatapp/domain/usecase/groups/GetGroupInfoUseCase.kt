package com.example.realtimechatapp.domain.usecase.groups

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupInfoUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<Group>{
        if (groupId.isBlank()) return Result.failure(Exception("ID nhóm không hợp lệ"))

        return groupRepository.getGroupInfo(groupId)
    }
}