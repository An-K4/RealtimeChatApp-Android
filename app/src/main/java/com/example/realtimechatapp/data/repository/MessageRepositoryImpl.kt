package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.toMessageContact
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.MessageApi
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi,
    private val messageContactDao: MessageContactDao,
    private val messageDao: MessageDao,
    private val socketRepository: SocketRepository,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager
) : MessageRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            socketRepository.observeMessages().collect { messageDto ->
                val messageEntity = if (messageDto.receiverId == null) {
                    messageDto.toMessageEntity().copy(
                        receiverId = currentUserManager.getCurrentUserId()
                    )
                } else {
                    messageDto.toMessageEntity()
                }
                messageDao.insertMessage(messageEntity)
                Timber.d("Đã chèn tin nhắn vào db: ${messageDto.toMessageEntity()}")
            }
        }
    }

    override suspend fun getMessageContacts(): Result<List<MessageContact>> =
        withContext(Dispatchers.IO) {
            val cachedContacts = messageContactDao.getMessageContact().map { it.toMessageContact() }

            return@withContext try {
                if (networkChecker.isNetworkAvailable()) {
                    val response = messageApi.getUsers()
                    val users = response.users.map { it.toUserEntity() }
                    val messageContacts = response.users.map { it.toMessageContactEntity() }

                    Timber.d(users.toString())
                    userDao.insertAllUsers(users)
                    messageContactDao.insertAllContact(messageContacts)

                    val contacts = messageContactDao.getMessageContact().map {
                        it.toMessageContact()
                    }
                    Result.success(contacts)
                } else {
                    Timber.d("Mất kết nối, lấy trong cache")
                    Result.success(cachedContacts)
                }
            } catch (e: Exception) {
                if (cachedContacts.isNotEmpty()) {
                    Timber.d(e.getErrorMessage())
                    Result.success(cachedContacts)
                } else {
                    Result.failure(e)
                }
            }
        }

    override suspend fun getMessage(friendId: String): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        val currentUserId = currentUserManager.getCurrentUserId()

        return@withContext try {
            if (networkChecker.isNetworkAvailable()) {
                val response = messageApi.getMessage(friendId)
                val responseMessages = response.messages.map { it.toMessageEntity() }

                messageDao.insertAllMessage(responseMessages)

                Timber.d(responseMessages.toString())
                Result.success(Unit)
            } else {
                Timber.d("Mất kết nối")
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getHeaderInfo(friendId: String): Result<User> {
        return try {
            val userInfo = userDao.getUserById(friendId)
            if (userInfo == null) {
                Result.failure(Exception("Không tìm thấy thông tin người dùng"))
            } else {
                Result.success(userInfo.toUser())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMessage(friendId: String): Flow<List<Message>> = flow {
        val currentUserId = currentUserManager.getCurrentUserId()
        emitAll(
            messageDao.observeMessages(currentUserId, friendId).map { messageWithDetails ->
                messageWithDetails.map { it.toMessage() }
            }
        )
    }

    override suspend fun sendMessage(message: SendMessageParam) {
        Timber.d("Impl gọi socket nghe rõ trả lời")
        socketRepository.sendMessage(message)
    }
}