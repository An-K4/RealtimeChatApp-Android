package com.example.realtimechatapp.domain.usecase.contact

import com.example.realtimechatapp.data.local.dao.GroupContactDao
import javax.inject.Inject

class UpdateGroupContactMuteStatusUseCase @Inject constructor(
    private val groupContactDao: GroupContactDao
) {
    suspend operator fun invoke(contactId: String, isMuted: Boolean) {
        groupContactDao.updateIsMuted(contactId, isMuted)
    }
}
