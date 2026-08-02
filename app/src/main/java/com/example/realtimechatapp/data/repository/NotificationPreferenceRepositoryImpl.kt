package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.local.dao.NotificationPreferenceDao
import com.example.realtimechatapp.data.local.entity.NotificationPreferenceEntity
import com.example.realtimechatapp.domain.repository.NotificationPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferenceRepositoryImpl @Inject constructor(
    private val notificationPreferenceDao: NotificationPreferenceDao
) : NotificationPreferenceRepository {
    
    override suspend fun getPreferences(): NotificationPreferenceEntity {
        return notificationPreferenceDao.getPreferences()
            ?: NotificationPreferenceEntity() // Return default if none exists
    }
    
    override fun observePreferences(): Flow<NotificationPreferenceEntity?> {
        return notificationPreferenceDao.observePreferences()
    }
    
    override suspend fun updatePreferences(preferences: NotificationPreferenceEntity) {
        notificationPreferenceDao.insertOrUpdate(preferences)
    }
}
