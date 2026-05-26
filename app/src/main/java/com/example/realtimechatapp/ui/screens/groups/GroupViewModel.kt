package com.example.realtimechatapp.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.usecase.groups.GetGroupsUseCase
import com.example.realtimechatapp.domain.usecase.socket.group.ObserveGroupMessageContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val observeGroupMessageContactUseCase: ObserveGroupMessageContactUseCase
): ViewModel() {
    data class GroupState(
        val groups: List<GroupMessageContact> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class GroupEvent {
        object Success : GroupEvent()
        data class Failure(val message: UiText) : GroupEvent()
    }

    private val _groupEvent = Channel<GroupEvent>()

    private val _isLoading = MutableStateFlow(false)
    val groupState = combine(
        observeGroupMessageContactUseCase().catch { exception ->
            Timber.e("Lỗi luồng lấy danh sách nhóm: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        _isLoading
    ){ groups, isLoading ->
        GroupState(
            groups = groups,
            isLoading = _isLoading.value && groups.isEmpty()
        )
    }.catch { exception ->
        Timber.e("Lỗi luồng màn hình tin nhắn: ${exception.getErrorMessage()}")
        _groupEvent.send(GroupEvent.Failure(exception.getErrorMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupState(isLoading = true)
    )

    // init after state variables
    init {
        getGroups()
    }

    fun getGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getGroupsUseCase()

            result.onSuccess {
                // do nothing, saved db before, wait observe
            }.onFailure { exception ->
                _groupEvent.send(GroupEvent.Failure(exception.getErrorMessage()))
            }

            _isLoading.value = false
        }
    }
}