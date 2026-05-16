package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.repository.ThemeManager
import com.example.realtimechatapp.domain.repository.ThemeMode
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeManagerImpl @Inject constructor(private val dataStore: DataStore<Preferences>
): ThemeManager {
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    override val themeMode = dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE] ?: ThemeMode.LIGHT.name
        ThemeMode.valueOf(themeModeString)
    }

    companion object{
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}