package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.realtimechatapp.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageContactDao {
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getMessageContactById(contactId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE is_group=0 ORDER BY last_time_stamp DESC")
    fun observeMessageContact(): Flow<List<ContactEntity>>

    @Upsert
    suspend fun insertContact(contactEntity: ContactEntity)

    @Upsert
    suspend fun insertAllContact(contactEntities: List<ContactEntity>)

    @Query("UPDATE contacts SET unread_count = unread_count + 1 WHERE id = :contactId")
    suspend fun updateUnreadCount(contactId: String)

    @Query("UPDATE contacts SET unread_count = 0 WHERE id = :contactId")
    suspend fun resetUnreadCount(contactId: String)

    @Transaction
    suspend fun upsertMessageContact(
        contactId: String,
        isMine: Boolean,
        lastMessage: String?,
        lastSenderName: String,
        lastTimeStamp: Long,
        contactName: String?,
        contactAvatar: String?
    ){
        val existingContact = getMessageContactById(contactId)

        if (existingContact == null){
            val newMessageContact = ContactEntity(
                id = contactId,
                isGroup = false,
                lastMessage = lastMessage,
                lastSenderName = lastSenderName,
                isMine = isMine,
                lastTimeStamp = lastTimeStamp,
                unreadCount = if (isMine) 0 else 1,
                contactName = contactName,
                contactAvatar = contactAvatar,
            )

            insertContact(newMessageContact)
        } else {
            val updatedContact = existingContact.copy(
                lastMessage = lastMessage,
                lastSenderName = lastSenderName,
                isMine = isMine,
                lastTimeStamp = lastTimeStamp,
                unreadCount = if (isMine) existingContact.unreadCount else existingContact.unreadCount + 1
            )
            insertContact(updatedContact)
        }
    }
}