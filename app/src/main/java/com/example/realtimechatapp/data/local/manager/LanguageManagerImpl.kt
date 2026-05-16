package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class LanguageManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LanguageManager {

    companion object {
        private val APP_LANGUAGE = stringPreferencesKey("selected_language")
    }

    override val currentLanguage: Flow<AppLanguage> = dataStore.data.map { preferences ->
        val languageCode = preferences[APP_LANGUAGE] ?: getDeviceLanguage().code
        AppLanguage.fromCode(languageCode)
    }

    private fun getDeviceLanguage(): AppLanguage {
        return AppLanguage.fromCode(Locale.getDefault().language)
    }

    override suspend fun setCurrentLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[APP_LANGUAGE] = language.code
        }
    }
}