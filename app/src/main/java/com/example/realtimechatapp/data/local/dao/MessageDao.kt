package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.pojo.MessageWithDetails
import com.example.realtimechatapp.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessage(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessages(messages: List<MessageEntity>)

    @Transaction
    @Query("""
        SELECT * FROM messages
        WHERE (receiver_id = :myId AND sender_id = :friendId)
        OR (receiver_id = :friendId AND sender_id = :myId)
        ORDER BY created_at DESC
    """)
    fun observeMessages(
        myId: String,
        friendId: String
    ): Flow<List<MessageWithDetails>>

    @Query("SELECT * FROM messages WHERE sender_id = :senderId AND receiver_id = :receiverId")
    suspend fun getMessagesToMarkSeen(senderId: String, receiverId: String): List<MessageEntity>

    // === NEW METHODS for Message Status Tracking ===

    // Update status của một message
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    // Update cả status và updatedAt timestamp
    @Query("UPDATE messages SET status = :status, updated_at = :updatedAt WHERE id = :messageId")
    suspend fun updateMessageStatusWithTimestamp(
        messageId: String,
        status: MessageStatus,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Replace temp ID với real ID từ server
    @Query("UPDATE messages SET id = :newId WHERE id = :oldId")
    suspend fun replaceMessageId(oldId: String, newId: String)

    // Update attachments sau khi upload xong
    @Query("UPDATE messages SET attachments = :fileUrl, updated_at = :updatedAt WHERE id = :messageId")
    suspend fun updateMessageAttachments(
        messageId: String,
        fileUrl: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Cần cho unit test + để repository kiểm tra lại message sau khi update
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    // Quét các message bị kẹt ở SENDING quá lâu (app bị kill giữa chừng, mất mạng...)
    @Query("SELECT * FROM messages WHERE status = 'SENDING' AND created_at < :staleBeforeTimestamp")
    suspend fun getStaleSendingMessages(staleBeforeTimestamp: Long): List<MessageEntity>

    // === NEW: Get messages with attachments for media grid view ===

    // Lấy tất cả tin nhắn có attachments giữa 2 người (cho màn hình media grid)
    @Transaction
    @Query("""
        SELECT * FROM messages
        WHERE attachments IS NOT NULL
        AND ((receiver_id = :myId AND sender_id = :friendId)
        OR (receiver_id = :friendId AND sender_id = :myId))
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getMessagesWithAttachments(
        myId: String,
        friendId: String,
        limit: Int,
        offset: Int
    ): List<MessageWithDetails>
}