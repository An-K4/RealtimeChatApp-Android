package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import javax.inject.Inject

class DeleteMemberUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String): Result<Unit> {
        return try {
            GroupValidator.validateGroupIdExist(groupId)
            GroupValidator.validateMemberIdExist(memberId)
            groupRepository.deleteMember(groupId, memberId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}