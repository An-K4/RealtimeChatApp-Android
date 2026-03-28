package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.toMessageContact
import com.example.realtimechatapp.data.remote.MessageApi
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi,
    private val messageContactDao: MessageContactDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager
) : MessageRepository {
    override suspend fun getUsers(): Result<List<MessageContact>> = withContext(Dispatchers.IO) {
        val cachedContacts = messageContactDao.getMessageContact().map { it.toMessageContact() }

        return@withContext try {
            if (networkChecker.isNetworkAvailable()) {
                val response = messageApi.getUsers()
                val users = response.users.map { it.toUserEntity() }
                val messageContacts = response.users.map { it.toMessageContactEntity() }

                Timber.d(users.toString())
                userDao.insertAllUser(users)
                messageContactDao.insertAllContact(messageContacts)

                val contacts = messageContactDao.getMessageContact().map {
                    it.toMessageContact()
                }
                Result.success(contacts)
            } else {
                Result.success(cachedContacts)
            }
        } catch (e: Exception) {
            if (cachedContacts.isNotEmpty()) {
                Result.success(cachedContacts)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getMessage(friendId: String): Result<List<Message>> = withContext(
        Dispatchers.IO) {
        val currentUserId = currentUserManager.getCurrentUserId()
        val cachedMessages = messageDao.getMessages(
            currentUserId,
            friendId,
            10,
            0
        ).map { it.toMessage() }

        return@withContext try {
            if (networkChecker.isNetworkAvailable()){
                val response = messageApi.getMessage(friendId)
                val responseMessages = response.messages.map { it.toMessageEntity() }

                messageDao.insertAllMessage(responseMessages)

                val messages = messageDao.getMessages(
                    currentUserId,
                    friendId,
                    10,
                    0
                ).map {
                    it.toMessage()
                }
                Timber.d(messages.toString())
                Result.success(messages)
            } else {
                Result.success(cachedMessages)
            }
        } catch (e: Exception) {
            if (cachedMessages.isNotEmpty()) {
                Result.success(cachedMessages)
            } else {
                Result.failure(e)
            }
        }
    }
}