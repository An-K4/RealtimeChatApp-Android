package com.example.realtimechatapp.data.repository

import androidx.room.withTransaction
import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MemberDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.ContactEntity
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.toGroupMessageContact
import com.example.realtimechatapp.data.local.pojo.toGroup
import com.example.realtimechatapp.data.local.pojo.toMember
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.GroupApi
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.data.remote.dto.group.AddMembersRequestDto
import com.example.realtimechatapp.data.remote.dto.group.ChangeRoleRequestDto
import com.example.realtimechatapp.data.remote.dto.group.CreateGroupRequestDto
import com.example.realtimechatapp.data.remote.dto.group.GroupMessageSeenDto
import com.example.realtimechatapp.data.remote.dto.group.MemberDto
import com.example.realtimechatapp.data.remote.dto.group.TransferOwnerRequestDto
import com.example.realtimechatapp.data.remote.dto.group.UpdateGroupRequestDto
import com.example.realtimechatapp.data.remote.dto.user.UserDto
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.exception.AuthException
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageStatus
import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.GroupCrudEvents
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.SocketConnectionState
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val groupContactDao: GroupContactDao,
    private val groupMessageDao: GroupMessageDao,
    private val groupDao: GroupDao,
    private val localDatabase: LocalDatabase,
    private val memberDao: MemberDao,
    private val userDao: UserDao,
    private val socketRepository: SocketRepository,
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

        // Socket + FCM path: unified persistence logic
        applicationScope.launch {
            socketRepository.observeGroupMessages().collect { messageDto ->
                persistIncomingGroupMessage(messageDto)
            }
        }

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

        applicationScope.launch {
            socketRepository.observeGroupCrudEvents().collect { event ->
                when (event) {
                    is GroupCrudEvents.Created -> {
                        val groupDto = event.group

                        saveGroupToLocalDatabase(
                            groupDto.toGroupEntity(),
                            groupDto.owner,
                            groupDto.members
                        )
                        socketRepository.joinGroup(groupDto.id)
                    }

                    is GroupCrudEvents.UpdatedInfo -> {
                        with(event.groupUpdatedInfo) {
                            groupDao.updateGroupInfo(id, name, avatar, description)
                        }
                    }

                    is GroupCrudEvents.Deleted -> {
                        val groupId = event.groupId

                        localDatabase.withTransaction {
                            groupContactDao.deleteGroupContact(groupId)
                            groupMessageDao.deleteGroupMessages(groupId)
                            memberDao.deleteGroupMembers(groupId)
                            groupDao.deleteGroup(groupId)
                        }

                        socketRepository.leaveGroup(groupId)
                    }

                    is GroupCrudEvents.MemberLeft -> {
                        val groupId = event.groupId
                        val userId = event.userId

                        safeDbCall {
                            memberDao.deleteMember(groupId, userId)
                        }
                    }

                    is GroupCrudEvents.ReloadGroups -> {
                        // Gọi lại getGroups() để lấy danh sách mới với unread count và lastMessage đúng
                        getGroups()
                        Timber.d("Đã được thêm vào nhóm mới, reload groups thành công")
                    }

                    is GroupCrudEvents.MemberRemoved -> {
                        localDatabase.withTransaction {
                            groupContactDao.deleteGroupContact(event.info.groupId)
                            groupMessageDao.deleteGroupMessages(event.info.groupId)
                            memberDao.deleteGroupMembers(event.info.groupId)
                            groupDao.deleteGroup(event.info.groupId)
                        }
                        socketRepository.leaveGroup(event.info.groupId)
                    }

                    is GroupCrudEvents.MemberRoleChanged -> {
                        memberDao.updateMemberRole(
                            groupId = event.info.groupId,
                            userId = event.info.member.userId.id,
                            newRole = event.info.member.role.toMemberRole()
                        )
                        Timber.d("Updated member role: groupId=${event.info.groupId}, userId=${event.info.member.userId.id}, role=${event.info.member.role}")
                    }

                    is GroupCrudEvents.OwnerTransferred -> {
                        localDatabase.withTransaction {
                            // Update new owner role to admin
                            memberDao.updateMemberRole(
                                groupId = event.info.groupId,
                                userId = event.info.newOwner.userId.id,
                                newRole = event.info.newOwner.role.toMemberRole()
                            )
                            // Update group owner field
                            groupDao.updateGroupOwner(
                                groupId = event.info.groupId,
                                newOwnerId = event.info.newOwner.userId.id
                            )
                        }
                        Timber.d("Owner transferred: groupId=${event.info.groupId}, newOwner=${event.info.newOwner.userId.id}")
                    }
                }
            }
        }

        applicationScope.launch {
            recoverStaleSendingMessages()
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
            val currentUserId = currentUserManager.getCurrentUserId() ?: return Result.failure(
                AuthException.InvalidCurrentUserIdException
            )
            val result = safeApiCall(networkChecker) { groupApi.getGroupMessage(groupId) }
            val responseGroupMessages = result.groupMessages
            val responseSender =
                result.groupMessages.map { it.senderId.toUserEntity() }.distinctBy { it.id }

            safeDbCall {
                userDao.upsertUsers(responseSender)
                groupMessageDao.insertAllMessages(responseGroupMessages.map {
                    it.toMessageEntity(
                        currentUserId
                    )
                })
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy tin nhắn nhóm")
            Result.failure(e)
        }
    }

    override suspend fun getGroupInfo(groupId: String): Result<Unit> {
        return try {
            // call api
            val result = safeApiCall(networkChecker) {
                groupApi.getGroupInfo(groupId)
            }
            val responseGroup = result.group.toGroupEntity()
            val responseOwner = result.group.owner.toUserEntity()
            val responseMembers = result.group.members.map { it.toMemberEntity(groupId) }

            // save to db
            safeDbCall {
                groupDao.insertGroup(responseGroup)
                userDao.insertUser(responseOwner)
                memberDao.syncGroupMembers(groupId, responseMembers)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy thông tin nhóm")
            Result.failure(e)
        }
    }

    override suspend fun getLocalGroups(): Result<List<Group>> {
        return try {
            val localGroupEntities = safeDbCall { groupDao.getLocalGroups() }

            Result.success(localGroupEntities.map { it.toGroup() })
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy danh sách nhóm cục bộ")
            Result.failure(e)
        }
    }

    override fun observeGroupInfo(groupId: String): Flow<Group?> {
        return groupDao.observeGroupById(groupId).map { it?.toGroup() }
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
        val currentUserId = currentUserManager.getCurrentUserId() ?: return

        markGroupMessageAsSeen(groupId, currentUserId)
        safeDbCall { groupContactDao.resetUnreadCount(groupId) }
        socketRepository.seenGroupMessage(GroupMessageSeenDto(groupId))
    }

    override suspend fun markGroupMessageAsSeen(groupId: String, userId: String) {
        val messages = safeDbCall { groupMessageDao.getMessagesToMarkSeen(groupId, userId) }
        val markedMessages = mutableListOf<MessageEntity>()

        for (msg in messages) {
            val currentSeenBy = msg.seenBy?.toMutableList()

            if (currentSeenBy?.contains(userId) != true) {
                currentSeenBy?.add(userId)
                markedMessages.add(msg.copy(seenBy = currentSeenBy))
            }
        }

        safeDbCall { groupMessageDao.updateGroupMessages(markedMessages) }
    }

    override suspend fun createGroup(
        name: String,
        members: List<String>
    ): Result<String> {
        return try {
            val response = safeApiCall(networkChecker) {
                groupApi.createGroup(
                    CreateGroupRequestDto(
                        name,
                        members
                    )
                )
            }

            val groupEntity = response.group.toGroupEntity()
            val owner = response.group.owner
            val members = response.group.members

            saveGroupToLocalDatabase(groupEntity, owner, members)
            socketRepository.joinGroup(groupEntity.id)

            Result.success(groupEntity.id)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi tạo nhóm")
            Result.failure(e)
        }
    }

    override suspend fun updateGroup(
        id: String,
        name: String,
        avatar: String?,
        description: String?
    ): Result<Unit> {
        return try {
            val response = safeApiCall(networkChecker) {
                groupApi.updateGroup(
                    id,
                    UpdateGroupRequestDto(name, avatar, description)
                )
            }

            val groupEntity = response.group.toGroupEntity()
            safeDbCall { groupDao.updateGroup(groupEntity) }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi cập nhật thông tin nhóm")
            Result.failure(e)
        }
    }

    override suspend fun getMembers(groupId: String): Result<List<Member>> {
        return try {
            val response = safeApiCall(networkChecker) { groupApi.getMembers(groupId) }
            val responseMembers = response.members
            safeDbCall {
                memberDao.syncGroupMembers(
                    groupId,
                    responseMembers.map { it.toMemberEntity(groupId) }
                )
                userDao.upsertUsers(responseMembers.map { it.userId.toUserEntity() })
            }

            val ownerId = getOwnerIdOfGroup(groupId)

            val memberWithDetails = safeDbCall { memberDao.getGroupMembers(groupId) }
            val members = memberWithDetails.map { it.toMember(ownerId) }
            Result.success(members)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi lấy danh sách thành viên nhóm")

            val cachedMemberWithDetails = safeDbCall { memberDao.getGroupMembers(groupId) }
            val cachedOwnerId = getOwnerIdOfGroup(groupId)
            val cachedMembers = cachedMemberWithDetails.map { it.toMember(cachedOwnerId) }
            Result.success(cachedMembers)
        }
    }

    private suspend fun getOwnerIdOfGroup(groupId: String): String {
        return safeDbCall { groupDao.getOwnerIdOfGroup(groupId) }.orEmpty()
    }

    override suspend fun addMembers(
        groupId: String,
        newMembers: List<String>
    ): Result<Unit> {
        return try {
            safeApiCall(networkChecker) {
                groupApi.addMembers(
                    groupId,
                    AddMembersRequestDto(newMembers)
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi thêm thành viên")
            Result.failure(e)
        }
    }

    override suspend fun changeRole(
        groupId: String,
        memberId: String,
        newRole: Role
    ): Result<Unit> {
        return try {
            safeApiCall(networkChecker) {
                groupApi.changeRole(
                    groupId,
                    memberId,
                    ChangeRoleRequestDto(newRole.rawValue)
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi thay đổi quyền thành viên")
            Result.failure(e)
        }
    }

    override suspend fun deleteMember(
        groupId: String,
        memberId: String
    ): Result<Unit> {
        return try {
            safeApiCall(networkChecker) {
                groupApi.deleteMember(
                    groupId,
                    memberId
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi xóa thành viên")
            Result.failure(e)
        }
    }

    override suspend fun transferOwner(
        groupId: String,
        newOwnerId: String
    ): Result<List<Member>> {
        return try {
            val response = safeApiCall(networkChecker) {
                groupApi.transferOwner(
                    groupId,
                    TransferOwnerRequestDto(newOwnerId)
                )
            }

            val updatedGroup = response.updatedGroup
            safeDbCall {
                localDatabase.withTransaction {
                    groupDao.updateGroup(updatedGroup.toGroupEntity())
                    memberDao.syncGroupMembers(
                        groupId,
                        updatedGroup.members.map { it.toMemberEntity(groupId) })
                }
            }

            val ownerId = getOwnerIdOfGroup(groupId)

            val memberWithDetails = safeDbCall { memberDao.getGroupMembers(groupId) }
            val members = memberWithDetails.map { it.toMember(ownerId) }
            Result.success(members)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi chuyển quyền chủ nhóm")
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(groupId: String): Result<Unit> {
        return try {
            // Call API to leave/delete group
            safeApiCall(networkChecker) { groupApi.deleteGroup(groupId) }

            // Cleanup local database
            safeDbCall {
                localDatabase.withTransaction {
                    groupContactDao.deleteGroupContact(groupId)
                    groupMessageDao.deleteGroupMessages(groupId)
                    memberDao.deleteGroupMembers(groupId)
                    groupDao.deleteGroup(groupId)
                }
            }

            // Leave socket room
            socketRepository.leaveGroup(groupId)

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi khi rời nhóm")
            Result.failure(e)
        }
    }

    private suspend fun saveGroupToLocalDatabase(
        groupEntity: GroupEntity,
        owner: UserDto,
        members: List<MemberDto>
    ) {
        safeDbCall {
            localDatabase.withTransaction {
                groupDao.insertGroup(groupEntity)
                groupContactDao.insertContact(
                    ContactEntity(
                        id = groupEntity.id,
                        isGroup = true,
                        lastMessage = null,
                        lastSenderName = null,
                        lastAttachments = null,
                        isMine = false,
                        lastTimeStamp = groupEntity.createdAt,
                        unreadCount = 0,
                        contactName = groupEntity.name,
                        contactAvatar = groupEntity.avatar
                    )
                )
                userDao.upsertUser(owner.toUserEntity())
                memberDao.insertAllMember(members.map { it.toMemberEntity(groupEntity.id) })
            }
        }
    }

    override suspend fun insertOptimisticGroupMessage(
        groupId: String,
        content: String,
        attachment: String?,
        replyTo: String?
    ): Result<String> {
        return try {
            val tempId = UUID.randomUUID().toString()
            val currentUserId = currentUserManager.getCurrentUserId()
                ?: return Result.failure(AuthException.InvalidCurrentUserIdException)

            val messageEntity = MessageEntity(
                id = tempId,
                senderId = currentUserId,
                receiverId = null,
                groupId = groupId,
                content = content,
                replyToId = replyTo,
                attachments = attachment,
                seenBy = null,
                status = MessageStatus.SENDING,
                createdAt = System.currentTimeMillis()
            )

            safeDbCall { groupMessageDao.insertMessage(messageEntity) }

            safeDbCall {
                groupContactDao.upsertGroupContact(
                    contactId = groupId,
                    isMine = true,
                    lastMessage = content,
                    lastAttachments = attachment,
                    lastSenderName = "",
                    lastTimeStamp = messageEntity.createdAt
                )
            }

            Result.success(tempId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to insert optimistic group message")
            Result.failure(e)
        }
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        safeDbCall { groupMessageDao.updateMessageStatusWithTimestamp(messageId, status) }
    }

    override suspend fun updateMessageAttachments(messageId: String, fileUrl: String) {
        safeDbCall { groupMessageDao.updateMessageAttachments(messageId, fileUrl) }
    }

    override suspend fun replaceMessageId(oldId: String, newId: String) {
        safeDbCall { groupMessageDao.replaceMessageId(oldId, newId) }
    }

    private suspend fun recoverStaleSendingMessages() {
        try {
            val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
            val staleMessages = safeDbCall {
                groupMessageDao.getStaleSendingMessages(fiveMinutesAgo)
            }

            staleMessages.forEach { message ->
                safeDbCall {
                    groupMessageDao.updateMessageStatusWithTimestamp(
                        messageId = message.id,
                        status = MessageStatus.ERROR
                    )
                }
                Timber.w("Recovered stale group message: ${message.id}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to recover stale group messages")
        }
    }

    // FCM Integration: Persist incoming group message (used by Socket + FCM)
    override suspend fun persistIncomingGroupMessage(dto: com.example.realtimechatapp.data.remote.dto.message.MessageDto) {
        val currentUserId = currentUserManager.getCurrentUserId() ?: return
        val messageEntity = if (dto.receiverId == null) {
            dto.toMessageEntity(currentUserId).copy(receiverId = currentUserId)
        } else {
            dto.toMessageEntity(currentUserId)
        }
        val groupId = dto.groupId ?: return

        safeDbCall {
            localDatabase.withTransaction {
                // Check if message already exists (dedupe for Socket.IO + FCM arriving simultaneously)
                val alreadyExists = groupMessageDao.getMessageById(messageEntity.id) != null
                if (alreadyExists) return@withTransaction

                // Upsert UserEntity for sender (prevents RecordNotFoundException)
                userDao.upsertUser(dto.senderId.toUserEntity())

                groupMessageDao.insertMessage(messageEntity)
                groupContactDao.upsertGroupContact(
                    contactId = groupId,
                    isMine = dto.senderId.id == currentUserId,
                    lastMessage = dto.content,
                    lastAttachments = dto.attachments,
                    lastSenderName = dto.senderId.fullName,
                    lastTimeStamp = dto.createdAt.isoToLong()
                )
            }
        }
    }
}
