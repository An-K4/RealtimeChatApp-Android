package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class AddMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(groupId: String, newMembers: List<String>): Result<Unit> {
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            groupRepository.addMembers(groupId, newMembers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}