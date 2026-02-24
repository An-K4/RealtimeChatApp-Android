package com.example.realtimechatapp.domain.usecase.groups

import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
){
    suspend operator fun invoke(): Result<List<GroupContact>> {
        return groupRepository.getGroups()
    }
}