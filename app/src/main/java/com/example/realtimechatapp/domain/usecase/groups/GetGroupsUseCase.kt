package com.example.realtimechatapp.domain.usecase.groups

import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
){
    suspend operator fun invoke(): Result<Unit> {
        return groupRepository.getGroups()
    }
}