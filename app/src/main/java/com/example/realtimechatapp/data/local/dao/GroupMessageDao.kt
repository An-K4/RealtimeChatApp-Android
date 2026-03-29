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
    suspend fun updateMessage(message: MessageEntity)

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

    @Query(
        "UPDATE messages " +
                "SET seen_by = json_insert(seen_by, '\$', :userId)" +
                "WHERE (receiver_id = :userId OR group_id = :groupId)" +
                "AND json_extract(seen_by, '\$') NOT LIKE '%' || :userId || '%'"
    )
    suspend fun markMessageAsSeen(userId: String, groupId: String? = null)

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