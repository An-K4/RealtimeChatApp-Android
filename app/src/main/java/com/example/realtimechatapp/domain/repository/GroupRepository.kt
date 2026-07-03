package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun getGroups(): Result<Unit>
    suspend fun getGroupMessage(groupId: String): Result<Unit>
    suspend fun getGroupInfo(groupId: String): Result<Group>
    fun observeGroupMessages(groupId: String): Flow<List<Message>>
    fun observeGroupMessageContacts(): Flow<List<GroupMessageContact>>
    suspend fun seenGroupMessage(groupId: String)
    suspend fun markGroupMessageAsSeen(groupId: String, userId: String)

    suspend fun createGroup(name: String, members: List<String>): Result<String>

    suspend fun getMembers(groupId: String): Result<List<Member>>
    suspend fun addMembers(groupId: String, newMembers: List<String>): Result<Unit>
}