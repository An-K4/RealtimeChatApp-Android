package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageStatus
import com.example.realtimechatapp.domain.model.Role
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun getGroups(): Result<Unit>
    suspend fun getGroupMessage(groupId: String): Result<Unit>
    suspend fun getGroupInfo(groupId: String): Result<Unit>
    fun observeGroupInfo(groupId: String): Flow<Group?>
    fun observeGroupMessages(groupId: String): Flow<List<Message>>
    fun observeGroupMessageContacts(): Flow<List<GroupMessageContact>>
    suspend fun getLocalGroups(): Result<List<Group>>
    suspend fun seenGroupMessage(groupId: String)
    suspend fun markGroupMessageAsSeen(groupId: String, userId: String)

    // Message status tracking methods
    suspend fun insertOptimisticGroupMessage(
        groupId: String,
        content: String,
        attachment: String? = null,
        replyTo: String? = null
    ): Result<String>

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    suspend fun updateMessageAttachments(messageId: String, fileUrl: String)
    suspend fun replaceMessageId(oldId: String, newId: String)

    suspend fun createGroup(name: String, members: List<String>): Result<String>
    suspend fun updateGroup(
        id: String,
        name: String,
        avatar: String?,
        description: String?
    ): Result<Unit>

    suspend fun getMembers(groupId: String): Result<List<Member>>
    suspend fun addMembers(groupId: String, newMembers: List<String>): Result<Unit>
    suspend fun changeRole(groupId: String, memberId: String, newRole: Role): Result<Unit>
    suspend fun deleteMember(groupId: String, memberId: String): Result<Unit>
    suspend fun transferOwner(groupId: String, newOwnerId: String): Result<List<Member>>
    suspend fun leaveGroup(groupId: String): Result<Unit>
}