package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGroupMessageUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    operator fun invoke(groupId: String): Flow<List<Message>> = groupRepository.observeGroupMessages(groupId)
}