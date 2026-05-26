package com.example.realtimechatapp.domain.usecase.socket.group

import com.example.realtimechatapp.domain.model.SendGroupMessageParam
import com.example.realtimechatapp.domain.repository.SocketRepository
import timber.log.Timber
import javax.inject.Inject

class SendGroupMessageUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    suspend operator fun invoke(
        groupId: String,
        content: String,
        replyTo: String? = null,
        fileUrl: String? = null
    ) {
        val sendGroupMessageParam = SendGroupMessageParam(
            groupId = groupId,
            content = content,
            replyTo = replyTo,
            fileUrl = fileUrl
        )
        Timber.d("Đã gọi hàm gửi tin nhắn nhóm")
        socketRepository.sendGroupMessage(sendGroupMessageParam)
    }
}