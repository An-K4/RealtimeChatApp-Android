package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.toMessageContact
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.MessageApi
import com.example.realtimechatapp.data.remote.dto.message.MessageSeenDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.exception.DatabaseException
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi,
    private val messageContactDao: MessageContactDao,
    private val messageDao: MessageDao,
    private val socketRepository: SocketRepository,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : MessageRepository {
    // supervisor job protect coroutine when its child coroutine crash

    init {
        applicationScope.launch {
            socketRepository.observeMessages().collect { messageDto ->
                val messageEntity = if (messageDto.receiverId == null) {
                    messageDto.toMessageEntity().copy(
                        receiverId = currentUserManager.getCurrentUserId()
                    )
                } else {
                    messageDto.toMessageEntity()
                }
                safeDbCall { messageDao.insertMessage(messageEntity) }
            }
        }

        applicationScope.launch {
            socketRepository.observeMessageContacts().collect { messageDto ->
                val currentUserId = currentUserManager.getCurrentUserId()
                val contactId = messageDto.getMessageContactId(currentUserId)
                val isMine = messageDto.senderId.id == currentUserId

                safeDbCall {
                    messageContactDao.upsertMessageContact(
                        contactId = contactId,
                        isMine = isMine,
                        lastMessage = messageDto.content,
                        lastSenderName = messageDto.senderId.fullName,
                        lastTimeStamp = messageDto.createdAt.isoToLong(),
                        contactName = if (isMine) messageDto.receiverId?.fullName else messageDto.senderId.fullName,
                        contactAvatar = if (isMine) messageDto.receiverId?.avatar else messageDto.senderId.avatar
                    )
                }
            }
        }

        applicationScope.launch {
            socketRepository.observeMessageSeen().collect { messageSeenDto ->
                val viewerId = messageSeenDto.viewerId
                val senderId = currentUserManager.getCurrentUserId()

                viewerId?.let { markMessageAsSeen(senderId, viewerId) }
            }
        }
    }

    override suspend fun getMessageContacts(): Result<Unit> {
        return try {
            val response = safeApiCall(networkChecker) { messageApi.getUsers() }
            val users = response.users.map { it.toUserEntity() }
            val messageContacts = response.users.map { it.toMessageContactEntity() }

            Timber.d(users.toString())
            safeDbCall {
                userDao.insertAllUsers(users)
                messageContactDao.insertAllContact(messageContacts)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy danh sách tin nhắn")
            Result.failure(e)
        }
    }

    override suspend fun getMessage(friendId: String): Result<Unit> {
        return try {
            val response = safeApiCall(networkChecker) { messageApi.getMessage(friendId) }
            val responseMessages = response.messages.map { it.toMessageEntity() }
            safeDbCall { messageDao.insertAllMessage(responseMessages) }
            Timber.d(responseMessages.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy tin nhắn của %s", friendId)
            Result.failure(e)
        }
    }

    override suspend fun getHeaderInfo(friendId: String): Result<User> {
        return try {
            val userInfo = safeDbCall { userDao.getUserById(friendId) }
            if (userInfo == null) {
                Result.failure(DatabaseException.RecordNotFoundException)
            } else {
                Result.success(userInfo.toUser())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Result.failure(e)
        }
    }

    override fun observeMessages(friendId: String): Flow<List<Message>> = flow {
        val currentUserId = currentUserManager.getCurrentUserId()
        emitAll(
            messageDao.observeMessages(currentUserId, friendId).map { messageWithDetails ->
                messageWithDetails.map { it.toMessage() }
            }
        )
    }

    // direct return is preferred since Room already yields a reactive Flow.
    // wrapping with 'flow { emitAll(...) }' is only needed when calling suspending functions before emitting
    override fun observeMessageContacts(): Flow<List<MessageContact>> {
        return messageContactDao.observeMessageContact().map { contactEntities ->
            contactEntities.map { it.toMessageContact() }
        }
    }

    override suspend fun seenMessage(friendId: String) {
        try {
            val currentUserId = currentUserManager.getCurrentUserId()

            markMessageAsSeen(friendId, currentUserId)
            safeDbCall { messageContactDao.resetUnreadCount(friendId) }
            socketRepository.seenMessage(MessageSeenDto(friendId, currentUserId))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi trong quá trình xem tin nhắn")
        }
    }

    override suspend fun markMessageAsSeen(senderId: String, receiverId: String) {
        val messages = safeDbCall { messageDao.getMessagesToMarkSeen(senderId, receiverId) }
        val markedMessages = mutableListOf<MessageEntity>()

        for (msg in messages) {
            // without mutableListOf(), if block is always ignored, because currentSeenBy can be null
            val currentSeenBy = msg.seenBy?.toMutableList() // ?: mutableListOf()

            // or use currentSeenBy?.contains(receiverId) != true to fix this bug
            // instead of currentSeenBy?.contains(receiverId) == false
            if (currentSeenBy?.contains(receiverId) != true) {
                currentSeenBy?.add(receiverId)
                markedMessages.add(msg.copy(seenBy = currentSeenBy))
            }
        }

        if (messages.isNotEmpty()) safeDbCall { messageDao.updateMessages(markedMessages) }
    }
}