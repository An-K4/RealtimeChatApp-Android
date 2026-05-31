package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MemberDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.toGroupMessageContact
import com.example.realtimechatapp.data.local.pojo.toGroup
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.GroupApi
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.data.remote.dto.GroupMessageSeenDto
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.exception.DatabaseException
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.SocketConnectionState
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val groupContactDao: GroupContactDao,
    private val groupMessageDao: GroupMessageDao,
    private val groupDao: GroupDao,
    private val socketRepository: SocketRepository,
    private val memberDao: MemberDao,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : GroupRepository {

    init {
        applicationScope.launch {
            socketRepository.observeConnectionState().collect {
                if (it is SocketConnectionState.Connected) {
                    val groupIds = groupContactDao.getAllGroupContactIds()

                    groupIds.forEach { groupId ->
                        socketRepository.joinGroup(groupId)
                    }
                }
            }
        }

        applicationScope.launch {
            socketRepository.observeGroupMessages().collect {
                val messageEntity = it.toMessageEntity()
                safeDbCall { groupMessageDao.insertMessage(messageEntity) }
            }
        }

        applicationScope.launch {
            socketRepository.observeGroupMessages().collect { messageDto ->
                val currentUserId = currentUserManager.getCurrentUserId()
                val contactId = messageDto.getMessageContactId(currentUserId)
                val isMine = messageDto.senderId.id == currentUserId

                safeDbCall {
                    groupContactDao.upsertGroupContact(
                        contactId = contactId,
                        lastMessage = messageDto.content,
                        lastSenderName = messageDto.senderId.fullName,
                        isMine = isMine,
                        lastTimeStamp = messageDto.createdAt.isoToLong()
                    )
                }
            }
        }

        applicationScope.launch {
            applicationScope.launch {
                socketRepository.observeGroupMessageSeen().collect { groupMessageSeenDto ->
                    val groupId = groupMessageSeenDto.groupId
                    val userId = groupMessageSeenDto.userId

                    if (groupId != null && userId != null) {
                        markGroupMessageAsSeen(groupId, userId)
                    } else {
                        Timber.e("Thiếu dữ liệu để đánh dấu đã xem tin nhắn nhóm")
                    }
                }
            }

            // use explicit property access instead of destructuring declarations (e.g., '(groupId, userId)').
            // destructuring depends on property order, which can cause silent logical bugs if DTO fields are reordered or modified later.
            // socketRepository.observeGroupMessageSeen().collect { (groupId, userId) ->
            //     if (groupId != null && userId != null) {
            //         markGroupMessageAsSeen(groupId, userId)
            //     } else {
            //         Timber.e("Thiếu dữ liệu để đánh dấu đã xem tin nhắn nhóm")
            //     }
            // }
        }
    }

    override suspend fun getGroups(): Result<Unit> {
        return try {
            val response = safeApiCall(networkChecker) { groupApi.getGroups() }
            val responseGroups = response.groups.map { it.toContactEntity() }
            safeDbCall { groupContactDao.insertAllContact(responseGroups) }
            Timber.d(responseGroups.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy danh sách nhóm")
            Result.failure(e)
        }
    }

    override suspend fun getGroupMessage(groupId: String): Result<Unit> {
        return try {
            val result = safeApiCall(networkChecker) { groupApi.getGroupMessage(groupId) }
            val responseGroupMessages = result.groupMessages
            val responseSender =
                result.groupMessages.map { it.senderId.toUserEntity() }.distinctBy { it.id }

            safeDbCall {
                // debug - before
                Timber.d("${userDao.getUserCount()}")
                Timber.d(responseSender.toString())
                Timber.d("${responseSender.size}")

                userDao.insertAllUsers(responseSender)

                // debug - after
                Timber.d("${userDao.getUserCount()}")
                Timber.d(responseSender.toString())
                Timber.d("${responseSender.size}")

                groupMessageDao.insertAllMessages(responseGroupMessages.map { it.toMessageEntity() })
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy tin nhắn nhóm")
            Result.failure(e)
        }
    }

    override suspend fun getGroupInfo(groupId: String): Result<Group> {
        val cachedGroupInfo = safeDbCall { groupDao.getGroupById(groupId)?.toGroup() }

        return try {
            // call api
            val result = safeApiCall(networkChecker) {
                groupApi.getGroupInfo(groupId)
            }
            val responseGroup = result.group.toGroupEntity()
            val responseMembers = result.group.members.map { it.toMemberEntity(groupId) }

            // save to db
            safeDbCall {
                groupDao.insertGroup(responseGroup)
                memberDao.insertAllMember(responseMembers)
            }

            val groupInfo = safeDbCall { groupDao.getGroupById(groupId)?.toGroup() }

            if (groupInfo != null) {
                Timber.d(groupInfo.toString())
                Result.success(groupInfo)
            } else {
                Timber.d("Lỗi khi lưu db")
                Result.failure(DatabaseException.RecordNotFoundException)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy thông tin nhóm")

            if (cachedGroupInfo != null) {
                Timber.d("Có lỗi khi kết nối máy chủ, lấy dữ liệu trong cache")
                Result.success(cachedGroupInfo)
            } else {
                Result.failure(e)
            }
        }
    }

    override fun observeGroupMessages(groupId: String): Flow<List<Message>> {
        return groupMessageDao.observeGroupMessages(groupId).map { messageWithDetails ->
            messageWithDetails.map { it.toMessage() }
        }
    }

    override fun observeGroupMessageContacts(): Flow<List<GroupMessageContact>> {
        return groupContactDao.observeGroupContact().map { contactEntities ->
            contactEntities.map { it.toGroupMessageContact() }
        }
    }

    override suspend fun seenGroupMessage(groupId: String) {
        val currentUserId = currentUserManager.getCurrentUserId()

        markGroupMessageAsSeen(groupId, currentUserId)
        safeDbCall { groupContactDao.resetUnreadCount(groupId) }
        socketRepository.seenGroupMessage(GroupMessageSeenDto(groupId))
    }

    override suspend fun markGroupMessageAsSeen(groupId: String, userId: String) {
        val messages = safeDbCall { groupMessageDao.getMessagesToMarkSeen(groupId, userId) }
        val markedMessages = mutableListOf<MessageEntity>()

        for (msg in messages){
            val currentSeenBy = msg.seenBy?.toMutableList()

            if (currentSeenBy?.contains(userId) != true){
                currentSeenBy?.add(userId)
                markedMessages.add(msg.copy(seenBy = currentSeenBy))
            }
        }

        safeDbCall { groupMessageDao.updateGroupMessages(markedMessages) }
    }
}
