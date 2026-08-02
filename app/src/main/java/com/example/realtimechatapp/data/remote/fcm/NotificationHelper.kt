package com.example.realtimechatapp.data.remote.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.realtimechatapp.MainActivity
import com.example.realtimechatapp.R
import com.example.realtimechatapp.data.local.dao.NotificationPreferenceDao
import com.example.realtimechatapp.data.local.entity.NotificationPreferenceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.net.URL
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferenceDao: NotificationPreferenceDao
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_MESSAGE = "channel_message"
        const val CHANNEL_GROUP = "channel_group"
        const val CHANNEL_SYSTEM = "channel_system"
        
        private const val NOTIFICATION_ID_MESSAGE_BASE = 1000
        private const val NOTIFICATION_ID_GROUP_BASE = 2000
        private const val NOTIFICATION_ID_SYSTEM_BASE = 4000
        
        private const val REQUEST_CODE_MESSAGE = 100
        private const val REQUEST_CODE_GROUP = 200
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGE,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }
            
            val groupChannel = NotificationChannel(
                CHANNEL_GROUP,
                "Group Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for group messages"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                setShowBadge(true)
            }
            
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "System Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Important system notifications"
                enableVibration(false)
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(messageChannel, groupChannel, systemChannel)
            )
        }
    }
    
    suspend fun getNotificationPreferences(): NotificationPreferenceEntity {
        return notificationPreferenceDao.getPreferences() 
            ?: NotificationPreferenceEntity()
    }
    
    fun isQuietHours(prefs: NotificationPreferenceEntity): Boolean {
        if (!prefs.quietHoursEnabled) return false
        
        val start = prefs.quietHoursStart ?: return false
        val end = prefs.quietHoursEnd ?: return false
        
        val now = LocalTime.now()
        val startTime = LocalTime.parse(start)
        val endTime = LocalTime.parse(end)
        
        return if (endTime.isAfter(startTime)) {
            now.isAfter(startTime) && now.isBefore(endTime)
        } else {
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }
    
    fun showMessageNotification(
        messageId: String,
        senderId: String,
        senderName: String,
        content: String,
        avatarUrl: String?,
        isPreviewEnabled: Boolean = true
    ) {
        val notificationId = generateNotificationId(senderId, NOTIFICATION_ID_MESSAGE_BASE)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "detail_message")
            putExtra("friendId", senderId)
            putExtra("messageId", messageId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_MESSAGE + notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val avatarBitmap = avatarUrl?.let { loadBitmapFromUrl(it) }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(senderName)
            .setContentText(if (isPreviewEnabled) content else "New message")
            .setLargeIcon(avatarBitmap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(senderId)
            .setWhen(System.currentTimeMillis())
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    fun showGroupMessageNotification(
        messageId: String,
        groupId: String,
        groupName: String,
        senderId: String,
        senderName: String,
        content: String,
        avatarUrl: String?,
        isPreviewEnabled: Boolean = true
    ) {
        val notificationId = generateNotificationId(groupId, NOTIFICATION_ID_GROUP_BASE)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "detail_group")
            putExtra("groupId", groupId)
            putExtra("messageId", messageId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_GROUP + notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val avatarBitmap = avatarUrl?.let { loadBitmapFromUrl(it) }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GROUP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(groupName)
            .setContentText(
                if (isPreviewEnabled) "$senderName: $content" 
                else "$senderName sent a message"
            )
            .setLargeIcon(avatarBitmap)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(groupId)
            .setWhen(System.currentTimeMillis())
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    fun showSystemNotification(
        title: String,
        message: String,
        type: String,
        targetId: String
    ) {
        val notificationId = generateNotificationId(targetId, NOTIFICATION_ID_SYSTEM_BASE)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    private fun generateNotificationId(key: String, base: Int): Int {
        return base + (key.hashCode().absoluteValue % 100_000)
    }
    
    private fun loadBitmapFromUrl(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load avatar from $url")
            null
        }
    }
}
