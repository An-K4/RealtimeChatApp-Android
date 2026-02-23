package com.example.realtimechatapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_prefs")

class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    suspend fun saveToken(token: String){
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun deleteToken(){
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}