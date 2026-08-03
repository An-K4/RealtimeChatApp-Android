package com.example.realtimechatapp.domain.usecase.notification

import com.example.realtimechatapp.data.remote.fcm.NotificationHelper
import javax.inject.Inject

class CancelGroupNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(groupId: String) {
        notificationHelper.cancelGroupNotification(groupId)
    }
}
