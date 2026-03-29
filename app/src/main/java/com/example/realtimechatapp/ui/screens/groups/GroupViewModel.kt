package com.example.realtimechatapp.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.usecase.groups.GetGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase
): ViewModel() {
    data class GroupListUiState(
        val isLoading: Boolean = false,
        val groups: List<GroupContact> = emptyList()
    )

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState = _uiState.asStateFlow()

    // init after state variables
    init {
        getGroup()
    }

    fun getGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getGroupsUseCase()

            result.onSuccess { groups ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groups = groups,
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        }
    }
}