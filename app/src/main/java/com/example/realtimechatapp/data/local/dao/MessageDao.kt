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
}