package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.remote.MessageApi
import com.example.realtimechatapp.domain.model.UserContact
import com.example.realtimechatapp.domain.repository.MessageRepository
import timber.log.Timber
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi
): MessageRepository {
    override suspend fun getUsers(): Result<List<UserContact>> {
        return try {
            val response = messageApi.getUsers()
            val users = response.users.map { it.toUserContact() }
            Result.success(users)
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(Exception(e.getErrorMessage()))
        }
    }
}