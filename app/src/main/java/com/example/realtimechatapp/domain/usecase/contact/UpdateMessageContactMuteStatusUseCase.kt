package com.example.realtimechatapp.domain.usecase.contact

import com.example.realtimechatapp.data.local.dao.MessageContactDao
import javax.inject.Inject

class UpdateMessageContactMuteStatusUseCase @Inject constructor(
    private val messageContactDao: MessageContactDao
) {
    suspend operator fun invoke(contactId: String, isMuted: Boolean) {
        messageContactDao.updateIsMuted(contactId, isMuted)
    }
}
