package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.MessageStatus
import com.example.realtimechatapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessageContacts(): Result<Unit>
    suspend fun getMessage(friendId: String): Result<Unit>
    suspend fun getHeaderInfo(friendId: String): Result<User>
    fun observeMessages(friendId: String): Flow<List<Message>>
    fun observeMessageContacts(): Flow<List<MessageContact>>
    suspend fun seenMessage(friendId: String)
    suspend fun markMessageAsSeen(senderId: String, receiverId: String)

    // === NEW METHODS for Message Status Tracking ===

    // Insert optimistic message (repository handles ID generation and current user)
    suspend fun insertOptimisticMessage(
        receiverId: String,
        content: String?,
        attachment: String?,
        replyTo: String? = null
    ): Result<String> // Returns temp message ID

    // Update message status
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    // Update message attachments after upload
    suspend fun updateMessageAttachments(messageId: String, fileUrl: String)

    // Replace temp ID with real ID from server
    suspend fun replaceMessageId(oldId: String, newId: String)
}