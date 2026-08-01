package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetLocalGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(): Result<List<Group>> {
        return groupRepository.getLocalGroups()
    }
}
