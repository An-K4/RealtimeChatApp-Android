package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.repository.GroupCrudEvents
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class KickedFromGroupInfo(
    val groupId: String,
    val groupName: String,
    val removedBy: String
)

class ObserveKickedFromGroupUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    operator fun invoke(): Flow<KickedFromGroupInfo> {
        return socketRepository.observeGroupCrudEvents()
            .filterIsInstance<GroupCrudEvents.MemberRemoved>()
            .map { event ->
                KickedFromGroupInfo(
                    groupId = event.info.groupId,
                    groupName = event.info.groupName,
                    removedBy = event.info.removedBy
                )
            }
    }
}
