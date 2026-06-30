package com.example.realtimechatapp.domain.usecase.group

import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Member>> {
        return groupRepository.getMembers(groupId)
    }
}