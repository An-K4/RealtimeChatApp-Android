package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.model.ActiveConversation
import com.example.realtimechatapp.domain.model.ConversationType
import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveConversationManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ActiveConversationManager {
    
    companion object {
        private val ACTIVE_CONVERSATION_ID = stringPreferencesKey("active_conversation_id")
        private val ACTIVE_CONVERSATION_TYPE = stringPreferencesKey("active_conversation_type")
    }
    
    override fun getActiveConversation(): Flow<ActiveConversation?> {
        return dataStore.data.map { preferences ->
            val id = preferences[ACTIVE_CONVERSATION_ID]
            val type = preferences[ACTIVE_CONVERSATION_TYPE]
            
            if (id != null && type != null) {
                ActiveConversation(
                    conversationId = id,
                    type = ConversationType.valueOf(type)
                )
            } else {
                null
            }
        }.catch { exception ->
            Timber.e(exception, "Error reading active conversation")
            emit(null)
        }
    }
    
    override suspend fun setActiveConversation(conversationId: String, type: ConversationType) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_CONVERSATION_ID] = conversationId
            preferences[ACTIVE_CONVERSATION_TYPE] = type.name
        }
        Timber.d("Active conversation set: $conversationId (${type.name})")
    }
    
    override suspend fun clearActiveConversation() {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_CONVERSATION_ID)
            preferences.remove(ACTIVE_CONVERSATION_TYPE)
        }
        Timber.d("Active conversation cleared")
    }
}
