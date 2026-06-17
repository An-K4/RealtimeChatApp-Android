package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.realtimechatapp.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupContactDao {
    @Query("SELECT * FROM contacts WHERE is_group=1 ORDER BY last_time_stamp DESC")
    suspend fun getGroupContact(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getGroupContactById(contactId: String): ContactEntity?

    @Query("SELECT id FROM contacts WHERE is_group=1")
    suspend fun getAllGroupContactIds(): List<String>

    @Query("SELECT * FROM contacts WHERE is_group=1 ORDER BY last_time_stamp DESC")
    fun observeGroupContact(): Flow<List<ContactEntity>>

    @Upsert
    suspend fun insertContact(contactEntity: ContactEntity)

    @Upsert
    suspend fun insertAllContact(contactEntities: List<ContactEntity>)

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

    @Transaction
    suspend fun upsertGroupContact(
        contactId: String,
        lastMessage: String?,
        lastSenderName: String,
        isMine: Boolean,
        lastTimeStamp: Long,
    ){
        val existingContact = getGroupContactById(contactId)

        if (existingContact != null){
            val updatedGroupContact = existingContact.copy(
                lastMessage = lastMessage,
                lastSenderName = lastSenderName,
                isMine = isMine,
                lastTimeStamp = lastTimeStamp,
                unreadCount = if (isMine) existingContact.unreadCount else existingContact.unreadCount + 1
            )

            insertContact(updatedGroupContact)
        } else {
            val newGroupContact = ContactEntity(
                id = contactId,
                isGroup = true,
                lastMessage = lastMessage,
                lastSenderName = lastSenderName,
                isMine = isMine,
                lastTimeStamp = lastTimeStamp,
                unreadCount = if (isMine) 0 else 1,
                contactName = "Nhóm $contactId",
                contactAvatar = null,
            )

            insertContact(newGroupContact)
        }
    }

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)
}