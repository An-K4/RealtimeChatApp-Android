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
    suspend fun updateGroupMessages(messages: List<MessageEntity>)

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
}