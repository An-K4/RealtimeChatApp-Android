package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class ObserveGroupInfoUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    operator fun invoke(groupId: String) = groupRepository.observeGroupInfo(groupId)
}