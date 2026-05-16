package com.example.realtimechatapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.ThemeMode
import com.example.realtimechatapp.domain.usecase.config.GetCurrentLanguageUseCase
import com.example.realtimechatapp.domain.usecase.config.GetCurrentThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase
): ViewModel() {
    data class MainViewModelState(
        val isLoading: Boolean = true,
        val currentLanguage: AppLanguage,
        val currentTheme: ThemeMode
    )

    val mainViewModelState = combine(
        getCurrentLanguageUseCase().catch { exception ->
            Timber.d("Lỗi lấy ngôn ngữ hiện tại: ${exception.message}")
            emit(AppLanguage.VIETNAMESE)
        },
        getCurrentThemeUseCase().catch { exception ->
            Timber.d("Lỗi lấy giao diện hiện tại: ${exception.message}")
            emit(ThemeMode.LIGHT)
        }
    ) { currentLanguage, currentTheme ->
        MainViewModelState(false, currentLanguage, currentTheme)
    }.catch { exception ->
        Timber.d("Lỗi cấu hình ứng dụng: ${exception.message}")
        emit(MainViewModelState(false,AppLanguage.VIETNAMESE, ThemeMode.LIGHT))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainViewModelState(true, AppLanguage.VIETNAMESE, ThemeMode.LIGHT)
    )
}