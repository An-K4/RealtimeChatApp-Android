package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.GroupContact

interface GroupRepository {
    suspend fun getGroups(): Result<List<GroupContact>>
}