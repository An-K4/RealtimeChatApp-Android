package com.example.realtimechatapp.data.remote.dto.message

import com.example.realtimechatapp.domain.model.LastMessage

data class LastMessageDto(
    val content: String,
    val attachments: String?,
    val createdAt: String,
    val senderName: String?,
    val isMine: Boolean
){
    fun toLastMessage(): LastMessage{
        return LastMessage(
            content = this.content,
            attachments = this.attachments,
            createdAt = this.createdAt,
            senderName = this.senderName,
            isMine = this.isMine
        )
    }
}