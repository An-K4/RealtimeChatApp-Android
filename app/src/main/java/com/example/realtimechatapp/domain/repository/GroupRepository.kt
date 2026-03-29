package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.model.Message

interface GroupRepository {
    suspend fun getGroups(): Result<List<GroupContact>>
    suspend fun getGroupMessage(groupId: String): Result<List<Message>>
    suspend fun getGroupInfo(groupId: String): Result<Group>
}