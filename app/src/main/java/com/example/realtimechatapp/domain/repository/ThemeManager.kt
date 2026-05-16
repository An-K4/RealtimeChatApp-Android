package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeManager {

    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(themeMode: ThemeMode)
}

enum class ThemeMode{
    LIGHT, DARK
}