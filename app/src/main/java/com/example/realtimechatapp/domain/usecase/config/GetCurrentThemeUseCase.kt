package com.example.realtimechatapp.domain.usecase.config

import com.example.realtimechatapp.domain.repository.ThemeManager
import com.example.realtimechatapp.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(private val themeManager: ThemeManager) {
    operator fun invoke(): Flow<ThemeMode> = themeManager.themeMode
}