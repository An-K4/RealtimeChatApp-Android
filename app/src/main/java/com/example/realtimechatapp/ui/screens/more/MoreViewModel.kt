package com.example.realtimechatapp.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.ThemeMode
import com.example.realtimechatapp.domain.usecase.config.GetCurrentLanguageUseCase
import com.example.realtimechatapp.domain.usecase.config.GetCurrentThemeUseCase
import com.example.realtimechatapp.domain.usecase.config.SetLanguageUseCase
import com.example.realtimechatapp.domain.usecase.config.SetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase
): ViewModel() {
    data class MoreScreenState(
        val supportedLanguages: List<AppLanguage> = AppLanguage.entries,
        val selectedLanguage: AppLanguage = AppLanguage.VIETNAMESE,
        val isDarkTheme: Boolean = false,
        val isLoading: Boolean = true,
    )

    val moreScreenState = combine(
        getCurrentLanguageUseCase().catch { exception ->
            Timber.d("Lỗi lấy ngôn ngữ hiện tại: ${exception.message}")
            emit(AppLanguage.VIETNAMESE)
        },
        getCurrentThemeUseCase().catch { exception ->
            Timber.d("Lỗi lấy giao diện hiện tại: ${exception.message}")
            emit(ThemeMode.LIGHT)
        }
    ) { currentLanguage, currentTheme ->
        MoreScreenState(AppLanguage.entries, selectedLanguage = currentLanguage, currentTheme == ThemeMode.DARK, false)
    }.catch { exception ->
        Timber.d("Lỗi cấu hình ứng dụng: ${exception.message}")
        emit(MoreScreenState(isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MoreScreenState(isLoading = false)
    )

    fun changeLanguage(language: AppLanguage){
        viewModelScope.launch{
            Timber.d("ViewModel nhận lệnh đổi sang: ${language.displayName} (Code: ${language.code})")
            setLanguageUseCase(language)
        }
    }

    fun changeTheme(theme: ThemeMode){
        viewModelScope.launch {
            Timber.d("Đổi sang theme: ${theme.name}")
            setThemeUseCase(theme)
        }
    }
}