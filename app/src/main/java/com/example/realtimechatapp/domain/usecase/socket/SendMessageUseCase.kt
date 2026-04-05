package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.repository.MessageRepository
import timber.log.Timber
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        content: String,
        receiverId: String,
        replyTo: String? = null,
        fileUrl: String? = null
    ){
        val sendMessageParam = SendMessageParam(
            content = content,
            receiverId = receiverId,
            replyTo = replyTo,
            fileUrl = fileUrl
        )
        Timber.d("Đã gọi hàm gửi tin nhắn")
        messageRepository.sendMessage(sendMessageParam)
    }
}