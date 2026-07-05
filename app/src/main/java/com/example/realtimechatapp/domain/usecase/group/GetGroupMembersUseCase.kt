package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class GetGroupMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Member>> {
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            groupRepository.getMembers(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}