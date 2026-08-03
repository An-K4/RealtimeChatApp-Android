package com.example.realtimechatapp.data.repository

import androidx.room.withTransaction
import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.toMessageContact
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.MessageApi
import com.example.realtimechatapp.data.remote.dto.message.MessageSeenDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.exception.AuthException
import com.example.realtimechatapp.domain.exception.LocalStorageException
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.MessageStatus
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
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi,
    private val messageContactDao: MessageContactDao,
    private val messageDao: MessageDao,
    private val localDatabase: LocalDatabase,
    private val socketRepository: SocketRepository,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : MessageRepository {
    // supervisor job protect coroutine when its child coroutine crash

    init {
        // Socket + FCM path: unified persistence logic
        applicationScope.launch {
            socketRepository.observeMessages().collect { messageDto ->
                persistIncomingMessage(messageDto)
            }
        }

        applicationScope.launch {
            socketRepository.observeMessageSeen().collect { messageSeenDto ->
                val viewerId = messageSeenDto.viewerId
                val senderId = currentUserManager.getCurrentUserId() ?: return@collect

                viewerId?.let { markMessageAsSeen(senderId, viewerId) }
            }
        }

        // Recovery: Đánh dấu các message bị kẹt ở SENDING quá lâu thành ERROR
        applicationScope.launch {
            recoverStaleSendingMessages()
        }
    }

    override suspend fun getMessageContacts(): Result<Unit> {
        return try {
            val response = safeApiCall(networkChecker) { messageApi.getUsers() }
            val users = response.users.map { it.toUserEntity() }
            val messageContacts = response.users.map { it.toMessageContactEntity() }

            Timber.d(users.toString())
            safeDbCall {
                userDao.upsertUsers(users)
                messageContacts.forEach { contact ->
                    messageContactDao.upsertMessageContact(
                        contactId = contact.id,
                        isMine = contact.isMine,
                        lastMessage = contact.lastMessage,
                        lastAttachments = contact.lastAttachments,
                        lastSenderName = contact.lastSenderName ?: "",
                        lastTimeStamp = contact.lastTimeStamp,
                        contactName = contact.contactName,
                        contactAvatar = contact.contactAvatar
                    )
                }
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
            val currentUserId = currentUserManager.getCurrentUserId() ?: return Result.failure(
                AuthException.InvalidCurrentUserIdException
            )
            val response = safeApiCall(networkChecker) { messageApi.getMessage(friendId) }
            val responseMessages = response.messages.map { it.toMessageEntity(currentUserId) }
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
                Result.failure(LocalStorageException.RecordNotFoundException)
            } else {
                Result.success(userInfo.toUser())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Result.failure(e)
        }
    }

    override fun observeMessages(friendId: String): Flow<List<Message>> = flow {
        val currentUserId = currentUserManager.getCurrentUserId() ?: return@flow
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
            val currentUserId = currentUserManager.getCurrentUserId() ?: return

            markMessageAsSeen(friendId, currentUserId)
            safeDbCall { messageContactDao.resetUnreadCount(friendId) }
            socketRepository.seenMessage(MessageSeenDto(friendId, currentUserId))
        } catch (e: Exception) {
            Timber.e(e, "Lỗi trong quá trình xem tin nhắn")
        }
    }

    override suspend fun markMessageAsSeen(senderId: String, receiverId: String) {
        val messages = safeDbCall { messageDao.getMessagesToMarkSeen(senderId, receiverId) }

        for (msg in messages) {
            // Update status to SEEN instead of updating seenBy list
            if (msg.status != MessageStatus.SEEN) {
                safeDbCall {
                    messageDao.updateMessageStatusWithTimestamp(
                        messageId = msg.id,
                        status = MessageStatus.SEEN
                    )
                }
            }
        }
    }

    // === NEW METHODS for Message Status Tracking ===

    override suspend fun insertOptimisticMessage(
        receiverId: String,
        content: String?,
        attachment: String?,
        replyTo: String?
    ): Result<String> {
        return try {
            // Generate temp ID
            val tempId = UUID.randomUUID().toString()

            // Get current user ID
            val currentUserId = currentUserManager.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            // Create optimistic message entity
            val messageEntity = MessageEntity(
                id = tempId,
                senderId = currentUserId,
                receiverId = receiverId,
                groupId = null,
                content = content,
                replyToId = replyTo,
                attachments = attachment,
                seenBy = null,
                status = MessageStatus.SENDING,
                createdAt = System.currentTimeMillis()
            )

            // Insert into DB
            safeDbCall { messageDao.insertMessage(messageEntity) }

            // Update message contact immediately for optimistic UI
            safeDbCall {
                messageContactDao.upsertMessageContact(
                    contactId = receiverId,
                    isMine = true,
                    lastMessage = content,
                    lastAttachments = attachment,
                    lastSenderName = "", // Sẽ được update khi server response
                    lastTimeStamp = messageEntity.createdAt,
                    contactName = null,
                    contactAvatar = null
                )
            }

            Result.success(tempId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to insert optimistic message")
            Result.failure(e)
        }
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        safeDbCall { messageDao.updateMessageStatusWithTimestamp(messageId, status) }
    }

    override suspend fun updateMessageAttachments(messageId: String, fileUrl: String) {
        safeDbCall { messageDao.updateMessageAttachments(messageId, fileUrl) }
    }

    override suspend fun replaceMessageId(oldId: String, newId: String) {
        safeDbCall { messageDao.replaceMessageId(oldId, newId) }
    }

    // Private method: Recovery cho messages bị kẹt ở SENDING
    private suspend fun recoverStaleSendingMessages() {
        try {
            val staleThreshold = System.currentTimeMillis() - 5 * 60 * 1000 // 5 phút
            val staleMessages = safeDbCall {
                messageDao.getStaleSendingMessages(staleThreshold)
            }

            staleMessages.forEach { msg ->
                safeDbCall {
                    messageDao.updateMessageStatusWithTimestamp(
                        msg.id,
                        MessageStatus.ERROR
                    )
                }
                Timber.w("Recovered stale SENDING message: ${msg.id}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to recover stale messages")
        }
    }

    // FCM Integration: Persist incoming message (used by Socket + FCM)
    override suspend fun persistIncomingMessage(dto: com.example.realtimechatapp.data.remote.dto.message.MessageDto) {
        val currentUserId = currentUserManager.getCurrentUserId() ?: return
        val messageEntity = if (dto.receiverId == null) {
            dto.toMessageEntity(currentUserId).copy(receiverId = currentUserId)
        } else {
            dto.toMessageEntity(currentUserId)
        }
        val contactId = dto.getMessageContactId(currentUserId)
        val isMine = dto.senderId.id == currentUserId

        safeDbCall {
            localDatabase.withTransaction {
                // Check if message already exists (dedupe for Socket.IO + FCM arriving simultaneously)
                val alreadyExists = messageDao.getMessageById(messageEntity.id) != null
                if (alreadyExists) return@withTransaction

                // Upsert UserEntity for sender (prevents RecordNotFoundException)
                userDao.upsertUser(dto.senderId.toUserEntity())

                messageDao.insertMessage(messageEntity)
                messageContactDao.upsertMessageContact(
                    contactId = contactId,
                    isMine = isMine,
                    lastMessage = dto.content,
                    lastAttachments = dto.attachments,
                    lastSenderName = dto.senderId.fullName,
                    lastTimeStamp = dto.createdAt.isoToLong(),
                    contactName = if (isMine) dto.receiverId?.fullName else dto.senderId.fullName,
                    contactAvatar = if (isMine) dto.receiverId?.avatar else dto.senderId.avatar
                )
            }
        }
    }
}