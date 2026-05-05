package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.repository.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): TokenManager {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    override suspend fun saveToken(token: String){
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    override val token: Flow<String?> = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    override suspend fun deleteToken(){
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}