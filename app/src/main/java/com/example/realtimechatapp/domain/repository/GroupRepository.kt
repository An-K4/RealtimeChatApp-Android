package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun getGroups(): Result<Unit>
    suspend fun getGroupMessage(groupId: String): Result<Unit>
    suspend fun getGroupInfo(groupId: String): Result<Group>
    fun observeGroupMessages(groupId: String): Flow<List<Message>>
    fun observeGroupMessageContacts(): Flow<List<GroupMessageContact>>
}