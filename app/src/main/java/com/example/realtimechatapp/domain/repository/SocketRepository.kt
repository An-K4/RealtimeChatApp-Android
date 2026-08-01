package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.group.GroupDto
import com.example.realtimechatapp.data.remote.dto.group.GroupInfoUpdatedDto
import com.example.realtimechatapp.data.remote.dto.group.GroupMessageSeenDto
import com.example.realtimechatapp.data.remote.dto.message.MessageDto
import com.example.realtimechatapp.data.remote.dto.message.MessageSeenDto
import com.example.realtimechatapp.data.remote.dto.socket.MemberRemovedDto
import com.example.realtimechatapp.data.remote.dto.socket.MemberRoleChangedDto
import com.example.realtimechatapp.data.remote.dto.socket.OwnerTransferredDto
import com.example.realtimechatapp.domain.model.GroupTypingUser
import com.example.realtimechatapp.domain.model.SendGroupMessageParam
import com.example.realtimechatapp.domain.model.SendMessageParam
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    fun observeConnectionState(): StateFlow<SocketConnectionState>

    fun observeMessages(): SharedFlow<MessageDto>
    fun observeMessageContacts(): SharedFlow<MessageDto>
    fun observeMessageSeen(): SharedFlow<MessageSeenDto>
    fun observeOnlineUserIds(): StateFlow<Set<String>>
    fun observeTypingStatus(): StateFlow<Set<String>>

    suspend fun sendMessage(message: SendMessageParam)
    suspend fun sendMessageWithAck(message: SendMessageParam, onAck: (success: Boolean, realMessageId: String?) -> Unit)
    suspend fun seenMessage(messageSeen: MessageSeenDto)
    suspend fun emitTypingStart(receiverId: String)
    suspend fun emitTypingStop(receiverId: String)

    fun joinGroup(groupId: String)
    fun leaveGroup(groupId: String)
    fun observeGroupCrudEvents(): SharedFlow<GroupCrudEvents>
    fun observeGroupMessages(): SharedFlow<MessageDto>
    fun observeGroupMessageContacts(): SharedFlow<MessageDto>
    fun observeGroupMessageSeen(): SharedFlow<GroupMessageSeenDto>
    fun observeGroupTypingStatus(): StateFlow<Map<String, Set<GroupTypingUser>>>

    suspend fun sendGroupMessage(groupMessage: SendGroupMessageParam)
    suspend fun sendGroupMessageWithAck(message: SendGroupMessageParam, onAck: (success: Boolean, realMessageId: String?) -> Unit)
    suspend fun seenGroupMessage(messageSeen: GroupMessageSeenDto)
    suspend fun emitGroupTypingStart(groupId: String)
    suspend fun emitGroupTypingStop(groupId: String)
}

sealed class SocketConnectionState {
    object Connected: SocketConnectionState()
    object Disconnected: SocketConnectionState()
    data class Error(val message: String): SocketConnectionState()
}

sealed class GroupCrudEvents {
    data class Created(val group: GroupDto) : GroupCrudEvents()
    data class UpdatedInfo(val groupUpdatedInfo: GroupInfoUpdatedDto) : GroupCrudEvents()
    data class Deleted(val groupId: String) : GroupCrudEvents()
    data class MemberLeft(val groupId: String, val userId: String) : GroupCrudEvents()
    data class MemberRemoved(val info: MemberRemovedDto) : GroupCrudEvents()
    data class MemberRoleChanged(val info: MemberRoleChangedDto) : GroupCrudEvents()
    data class OwnerTransferred(val info: OwnerTransferredDto) : GroupCrudEvents()
    object ReloadGroups : GroupCrudEvents()
}

object SocketEvents {
    // message
    const val RECEIVE_MESSAGE = "receive-message"
    const val SEND_MESSAGE = "send-message"
    const val SEEN_MESSAGE = "seen-message"
    const val TYPING_START = "typing-start"
    const val TYPING_STOP = "typing-stop"
    const val NOTIFY_USER_ONLINE = "noti-online"
    const val NOTIFY_USER_OFFLINE = "noti-offline"
    const val NOTIFY_ONLINE_LIST = "noti-onlineList-toMe"

    // group
    const val JOIN_GROUP = "join-group"
    const val LEAVE_GROUP = "leave-group"
    const val RECEIVE_GROUP_MESSAGE = "receive-group-message"
    const val SEEN_GROUP_MESSAGE = "seen-group-message"
    const val USER_SEEN_MESSAGE = "user-seen-message"
    const val SEND_GROUP_MESSAGE = "send-group-message"
    const val GROUP_TYPING_START = "group-typing-start"
    const val GROUP_TYPING_STOP = "group-typing-stop"
    const val NEW_GROUP_RECEIVED = "new-group-received"
    const val NEW_GROUP_UPDATED = "new-group-updated"
    const val GROUP_DELETED = "group-deleted"
    const val MEMBER_LEFT = "member-left"
    const val MEMBER_REMOVED = "member-removed"
    const val MEMBER_ROLE_CHANGED = "member-role-changed"
    const val OWNER_TRANSFERRED = "owner-transferred"
    const val RELOAD_GROUPS = "reload-groups"
}