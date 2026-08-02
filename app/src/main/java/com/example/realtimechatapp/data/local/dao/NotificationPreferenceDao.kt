package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.realtimechatapp.data.local.entity.NotificationPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferenceDao {
    
    @Query("SELECT * FROM notification_preferences WHERE id = 'default' LIMIT 1")
    suspend fun getPreferences(): NotificationPreferenceEntity?
    
    @Query("SELECT * FROM notification_preferences WHERE id = 'default' LIMIT 1")
    fun observePreferences(): Flow<NotificationPreferenceEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: NotificationPreferenceEntity)
}
