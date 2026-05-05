package com.example.realtimechatapp.domain.usecase.config

import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    operator fun invoke(language: AppLanguage){
        languageManager.setCurrentLanguage(language)
    }
}