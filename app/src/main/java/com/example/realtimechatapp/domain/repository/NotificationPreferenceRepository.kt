package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.local.entity.NotificationPreferenceEntity
import kotlinx.coroutines.flow.Flow

interface NotificationPreferenceRepository {
    suspend fun getPreferences(): NotificationPreferenceEntity
    fun observePreferences(): Flow<NotificationPreferenceEntity?>
    suspend fun updatePreferences(preferences: NotificationPreferenceEntity)
}
