package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.usecase.group.GetGroupMembersUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberManagementViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getGroupMembersUseCase: GetGroupMembersUseCase
) : ViewModel() {
    data class MemberManagementState(
        val members: List<Member> = emptyList(),
        val isIncompleteList: Boolean = false,
        val isEmptyMemberList: Boolean = false,
        val isLoading: Boolean = false
    )

    val groupId: String = checkNotNull(savedStateHandle[Screen.MemberManagement.ARG_GROUP_ID])

    private val _memberManagementState = MutableStateFlow(MemberManagementState())
    val memberManagementState = _memberManagementState.asStateFlow()

    init {
        getGroupMembers()
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(isLoading = true) }

            getGroupMembersUseCase(groupId).onSuccess { members ->
                val memberSize = members.size
                val cleanMemberList = members.filter { it.userId != null }.sortedBy { it.role }
                val cleanMemberSize = cleanMemberList.size

                _memberManagementState.update {
                    it.copy(
                        members = cleanMemberList,
                        isIncompleteList = memberSize > cleanMemberSize && cleanMemberSize > 0,
                        isEmptyMemberList = cleanMemberSize == 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Exposes a public API for UI-initiated reload events (e.g., pull-to-refresh).
     * This encapsulates the internal loading logic and allows for future extensions
     * of reload-specific behaviors without affecting initial loading.
     */
    fun reloadMemberList() {
        getGroupMembers()
    }
}