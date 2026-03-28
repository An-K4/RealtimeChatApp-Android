package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CurrentUserManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): CurrentUserManager{
    companion object{
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
    }

    override suspend fun getCurrentUserId(): String {
        return dataStore.data.map { preferences ->
                preferences[CURRENT_USER_ID] ?: ""
            }.first()
    }

    override suspend fun switchUser(userId: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    override fun observeCurrentUser(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[CURRENT_USER_ID] ?: ""
        }
    }
}