package com.example.realtimechatapp.domain.usecase.config

import com.example.realtimechatapp.domain.repository.ThemeManager
import com.example.realtimechatapp.domain.repository.ThemeMode
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(private val themeManager: ThemeManager) {
    suspend operator fun invoke(themeMode: ThemeMode){
        themeManager.setThemeMode(themeMode)
    }
}