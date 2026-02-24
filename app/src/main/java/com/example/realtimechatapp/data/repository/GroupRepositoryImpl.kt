package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.remote.GroupApi
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.repository.GroupRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi
): GroupRepository{
    override suspend fun getGroups(): Result<List<GroupContact>> {
        return try {
            val response = groupApi.getGroups()
            val groups = response.groups.map { it.toGroup() }
            Result.success(groups)
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(Exception(e.getErrorMessage()))
        }
    }
}
