package com.example.realtimechatapp.domain.usecase.notification

import com.example.realtimechatapp.data.remote.fcm.NotificationHelper
import javax.inject.Inject

class CancelMessageNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    operator fun invoke(senderId: String) {
        notificationHelper.cancelMessageNotification(senderId)
    }
}
