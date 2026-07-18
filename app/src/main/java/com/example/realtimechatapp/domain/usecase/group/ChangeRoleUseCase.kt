package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class ChangeRoleUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String, newRole: Role): Result<Unit> {
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            GroupValidator.validateMemberIdExist(memberId)
            groupRepository.changeRole(groupId, memberId, newRole)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}