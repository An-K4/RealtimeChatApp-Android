package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.pojo.MessageWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateGroupMessage(message: MessageEntity)

    @Update
    suspend fun updateGroupMessages(messages: List<MessageEntity>)

    // REMEMBER TO ADD TRANSACTION ANNOTATION
    @Transaction
    @Query(
        """
            SELECT * FROM messages
            WHERE (group_id = :groupId)
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getGroupMessages(
        groupId: String,
        limit: Int,
        offset: Int
    ): List<MessageWithDetails>

    @Transaction
    @Query("""
        SELECT * FROM messages
        WHERE (group_id = :groupId)
        AND created_at > :since
        ORDER BY created_at ASC
    """)
    suspend fun getNewGroupMessages(
        groupId: String,
        since: Long
    ): List<MessageWithDetails>

    @Transaction
    @Query("""
        SELECT * FROM messages
        WHERE (group_id = :groupId)
        ORDER BY created_at DESC
    """)
    fun observeGroupMessages(
        groupId: String
    ): Flow<List<MessageWithDetails>>

    @Query("SELECT * FROM messages WHERE group_id = :groupId AND sender_id != :userId")
    suspend fun getMessagesToMarkSeen(groupId: String, userId: String): List<MessageEntity>

    @Query("""
        SELECT COUNT(*) FROM messages
        WHERE group_id = :groupId
        AND created_at > (
            SELECT last_read_timestamp FROM members
            WHERE group_id = :groupId AND user_id = :userId
        )
    """)
    fun observeUnreadCount(groupId: String, userId: String): Flow<Int>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}