package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class AddMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(groupId: String, newMembers: List<String>): Result<Unit> {
        return groupRepository.addMembers(groupId, newMembers)
    }
}