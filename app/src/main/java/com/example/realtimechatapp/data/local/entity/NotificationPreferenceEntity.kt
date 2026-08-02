package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey
    val id: String = "default",

    @ColumnInfo(name = "enable_notifications")
    val enableNotifications: Boolean = true,

    @ColumnInfo(name = "enable_sound")
    val enableSound: Boolean = true,

    @ColumnInfo(name = "enable_vibration")
    val enableVibration: Boolean = true,

    @ColumnInfo(name = "enable_message_preview")
    val enableMessagePreview: Boolean = true,

    @ColumnInfo(name = "enable_group_notifications")
    val enableGroupNotifications: Boolean = true,

    @ColumnInfo(name = "quiet_hours_enabled")
    val quietHoursEnabled: Boolean = false,

    @ColumnInfo(name = "quiet_hours_start")
    val quietHoursStart: String? = null, // "22:00"

    @ColumnInfo(name = "quiet_hours_end")
    val quietHoursEnd: String? = null, // "08:00"

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
