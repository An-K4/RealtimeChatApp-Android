package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String, members: List<String>): Result<String> {
        return try {
            GroupValidator.validateGroupMemberSize(members)
            groupRepository.createGroup(name, members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}