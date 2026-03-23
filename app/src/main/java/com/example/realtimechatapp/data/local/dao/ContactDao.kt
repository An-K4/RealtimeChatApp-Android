package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.realtimechatapp.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Upsert
    suspend fun insertContact(contactEntity: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY last_time_stamp DESC")
    fun getAllContact(): Flow<List<ContactEntity>>

    @Query("UPDATE contacts SET unread_count = unread_count + 1 WHERE id = :contactId")
    suspend fun updateUnreadCount(contactId: String)

    @Query("UPDATE contacts SET unread_count = 0 WHERE id = :contactId")
    suspend fun resetUnreadCount(contactId: String)

    @Query("""
        UPDATE contacts
        SET last_message = :lastMessage,
            last_sender_name = :lastSenderName,
            last_time_stamp = :lastTimeStamp
        WHERE id = :contactId
    """)
    suspend fun updateLastMessage(
        contactId: String,
        lastMessage: String?,
        lastSenderName: String?,
        lastTimeStamp: Long
    )

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)
}