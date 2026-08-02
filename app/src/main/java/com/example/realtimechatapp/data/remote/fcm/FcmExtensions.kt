package com.example.realtimechatapp.data.remote.fcm

import com.example.realtimechatapp.data.remote.dto.message.MessageDto
import com.example.realtimechatapp.data.remote.dto.user.UserDto

/**
 * Extension function to convert FCM payload (Map<String, String>) to MessageDto.
 * 
 * FCM payload has flat structure with string values. This function reconstructs
 * the nested MessageDto structure required by the repository layer.
 * 
 * Note: UserDto objects are created with minimal fields available from FCM payload.
 * Full user data will be fetched from repository when needed.
 */
fun Map<String, String>.toMessageDto(): MessageDto {
    // Extract message fields
    val messageId = this["messageId"] ?: throw IllegalArgumentException("messageId is required")
    val content = this["content"] ?: ""
    val groupId = this["groupId"]
    val createdAt = this["createdAt"] ?: System.currentTimeMillis().toString()
    
    // Extract sender fields
    val senderId = this["senderId"] ?: throw IllegalArgumentException("senderId is required")
    val senderName = this["senderName"] ?: "Unknown"
    val senderAvatar = this["senderAvatar"]
    
    // Extract receiver fields (only for direct messages)
    val receiverId = this["receiverId"]
    val receiverName = this["receiverName"]
    val receiverAvatar = this["receiverAvatar"]
    
    // Create sender UserDto with available fields
    val senderDto = UserDto(
        id = senderId,
        username = senderName, // Use display name as username fallback
        fullName = senderName,
        email = "", // Not available in FCM payload
        avatar = senderAvatar,
        createdAt = createdAt
    )
    
    // Create receiver UserDto if this is a direct message
    val receiverDto = if (receiverId != null) {
        UserDto(
            id = receiverId,
            username = receiverName ?: "Unknown",
            fullName = receiverName ?: "Unknown",
            email = "", // Not available in FCM payload
            avatar = receiverAvatar,
            createdAt = createdAt
        )
    } else {
        null
    }
    
    // Return MessageDto
    return MessageDto(
        id = messageId,
        senderId = senderDto,
        receiverId = receiverDto,
        groupId = groupId,
        content = content,
        replyTo = null, // FCM notifications don't include reply chain
        attachments = this["attachments"],
        seenBy = null, // Will be updated when message is seen
        createdAt = createdAt
    )
}
