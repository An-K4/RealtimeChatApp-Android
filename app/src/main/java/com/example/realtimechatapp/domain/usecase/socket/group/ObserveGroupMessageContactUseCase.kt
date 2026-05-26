package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class ObserveGroupMessageContactUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    operator fun invoke() = groupRepository.observeGroupMessageContacts()
}