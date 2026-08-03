package com.example.realtimechatapp.data.remote.fcm

import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.domain.model.ConversationType
import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var messageContactDao: MessageContactDao

    @Inject
    lateinit var groupContactDao: GroupContactDao

    @Inject
    lateinit var activeConversationManager: ActiveConversationManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")

        // Save token to local DB + sync to server
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("FCM received: ${message.data}")

        val data = message.data
        val type = data["type"] ?: return

        when (type) {
            "new_message", "group_message" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (type == "new_message") handleNewMessage(data) else handleGroupMessage(data)
                    } catch (e: Exception) {
                        Timber.e(e, "Error handling FCM message (type=$type)")
                    }
                }
            }

            "member_kicked" -> handleMemberKicked(data)
            "group_deleted" -> handleGroupDeleted(data)
            else -> Timber.w("Unknown notification type: $type")
        }
    }

    private suspend fun handleNewMessage(data: Map<String, String>) {
        val messageId = data["messageId"] ?: return
        val senderId = data["senderId"] ?: return
        val senderName = data["senderName"] ?: "Someone"
        val content = data["content"] ?: ""
        val attachments = data["attachments"] ?: ""
        val avatar = data["senderAvatar"]

        try {
            messageRepository.persistIncomingMessage(data.toMessageDto())
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist FCM message to local DB")
        }

        val isMuted = messageContactDao.getMessageContactById(senderId)?.isMuted ?: false

        if (isMuted) {
            Timber.d("Contact $senderId is muted, skipping notification")
            return
        }

        val activeConv = activeConversationManager.getActiveConversation().firstOrNull()

        if (activeConv?.conversationId == senderId && activeConv.type == ConversationType.DIRECT) {
            Timber.d("User is in conversation with $senderId, skipping notification")
            return
        }

        val prefs = notificationHelper.getNotificationPreferences()

        if (!prefs.enableNotifications) {
            Timber.d("Notifications disabled globally")
            return
        }

        if (notificationHelper.isQuietHours(prefs)) {
            Timber.d("In quiet hours, skipping notification")
            return
        }

        notificationHelper.showMessageNotification(
            messageId = messageId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            attachments = attachments,
            avatarUrl = avatar,
            isPreviewEnabled = prefs.enableMessagePreview
        )
    }

    private suspend fun handleGroupMessage(data: Map<String, String>) {
        val messageId = data["messageId"] ?: return
        val groupId = data["groupId"] ?: return
        val senderId = data["senderId"] ?: return
        val senderName = data["senderName"] ?: "Someone"
        val groupName = data["groupName"] ?: "Group"
        val content = data["content"] ?: ""
        val attachments = data["attachments"] ?: ""
        val avatar = data["senderAvatar"]

        try {
            groupRepository.persistIncomingGroupMessage(data.toMessageDto())
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist FCM group message to local DB")
        }

        val isMuted = groupContactDao.getGroupContactById(groupId)?.isMuted ?: false

        if (isMuted) {
            Timber.d("Group $groupId is muted, skipping notification")
            return
        }

        val activeConv = activeConversationManager.getActiveConversation().firstOrNull()

        if (activeConv?.conversationId == groupId && activeConv.type == ConversationType.GROUP) {
            Timber.d("User is in group conversation $groupId, skipping notification")
            return
        }

        val prefs = notificationHelper.getNotificationPreferences()

        if (!prefs.enableGroupNotifications) {
            Timber.d("Group notifications disabled")
            return
        }

        notificationHelper.showGroupMessageNotification(
            messageId = messageId,
            groupId = groupId,
            groupName = groupName,
            senderId = senderId,
            senderName = senderName,
            content = content,
            attachments = attachments,
            avatarUrl = avatar,
            isPreviewEnabled = prefs.enableMessagePreview
        )
    }

    private fun handleMemberKicked(data: Map<String, String>) {
        val groupId = data["groupId"] ?: return
        val groupName = data["groupName"] ?: "Group"
        val kickedBy = data["kickedBy"] ?: "Admin"

        notificationHelper.showSystemNotification(
            title = "Removed from group",
            message = "$kickedBy removed you from $groupName",
            type = "member_kicked",
            targetId = groupId
        )
    }

    private fun handleGroupDeleted(data: Map<String, String>) {
        val groupId = data["groupId"] ?: return
        val groupName = data["groupName"] ?: "Group"

        notificationHelper.showSystemNotification(
            title = "Group deleted",
            message = "$groupName has been deleted",
            type = "group_deleted",
            targetId = groupId
        )
    }
}
