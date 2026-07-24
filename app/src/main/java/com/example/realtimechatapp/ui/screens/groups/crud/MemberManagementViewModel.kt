package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.group.AddMembersUseCase
import com.example.realtimechatapp.domain.usecase.group.ChangeRoleUseCase
import com.example.realtimechatapp.domain.usecase.group.DeleteMemberUseCase
import com.example.realtimechatapp.domain.usecase.group.GetGroupMembersUseCase
import com.example.realtimechatapp.domain.usecase.group.TransferOwnerUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.domain.usecase.user.GetLocalUserUseCase
import com.example.realtimechatapp.domain.usecase.user.PerformSearchUsersUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MemberManagementViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val performSearchUsersUseCase: PerformSearchUsersUseCase,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val addMembersUseCase: AddMembersUseCase,
    private val changeRoleUseCase: ChangeRoleUseCase,
    private val deleteMemberUseCase: DeleteMemberUseCase,
    private val transferOwnerUseCase: TransferOwnerUseCase
) : ViewModel() {
    data class MemberManagementState(
        val members: List<Member> = emptyList(),
        val isIncompleteList: Boolean = false,
        val isEmptyMemberList: Boolean = false,
        val sheetState: MemberManagementSheetState = MemberManagementSheetState.Dismiss,
        val dialogState: MemberManagementDialogState = MemberManagementDialogState.Dismiss,
        val isMemberAdding: Boolean = false,
        val isLoading: Boolean = false
    )

    data class AddMemberState(
        val querySearch: String = "",
        val localUsers: List<User>? = null,
        val searchResult: List<User>? = null,
        val selectedUser: Set<User> = emptySet(),
        val isSearching: Boolean = false
    )

    data class MemberActionState(
        val currentUserId: String = "",
        val selectedMemberInfo: User? = null,
        val selectedMemberRole: Role = Role.MEMBER,
        val isDirectMessageVisible: Boolean = false,
        val isTransferOwnerVisible: Boolean = false,
        val isPromoteToAdminVisible: Boolean = false,
        val isDemoteToMemberVisible: Boolean = false,
        val isDeleteMemberVisible: Boolean = false,
        val isFetchingInfo: Boolean = false,
    )

    sealed interface MemberManagementSheetState {
        object Dismiss : MemberManagementSheetState
        object AddMember : MemberManagementSheetState
        object MemberAction : MemberManagementSheetState
    }

    sealed interface MemberManagementDialogState {
        object Dismiss : MemberManagementDialogState
        object AddMemberSuccess : MemberManagementDialogState
        object DeleteMemberSuccess : MemberManagementDialogState
        object ChangeRoleSuccess : MemberManagementDialogState
        object TransferOwnerSuccess : MemberManagementDialogState
        object AddMemberConfirm : MemberManagementDialogState
        object PromoteConfirm : MemberManagementDialogState
        object DemoteConfirm : MemberManagementDialogState
        object DeleteMemberConfirm : MemberManagementDialogState
        object TransferOwnerConfirm : MemberManagementDialogState
        data class Failure(val message: UiText) : MemberManagementDialogState
    }

    val groupId: String = checkNotNull(savedStateHandle[Screen.MemberManagement.ARG_GROUP_ID])

    private val _memberManagementState = MutableStateFlow(MemberManagementState())
    val memberManagementState = _memberManagementState.asStateFlow()

    private val _addMemberState = MutableStateFlow(AddMemberState())
    val addMemberState = _addMemberState.asStateFlow()

    private val _memberActionState = MutableStateFlow(MemberActionState())
    val memberActionState = _memberActionState.asStateFlow()

    init {
        getGroupMembers()
        viewModelScope.launch {
            _addMemberState.map { state -> state.querySearch }
                .distinctUntilChanged()
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        performUserSearch(query)
                    } else {
                        _addMemberState.update { it.copy(searchResult = null) }
                    }
                }
        }
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(isLoading = true) }

            getGroupMembersUseCase(groupId).onSuccess { members ->
                cleanAndUpdateMemberListState(members)
            }
        }
    }

    private fun cleanAndUpdateMemberListState(members: List<Member>) {
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

    /**
     * Exposes a public API for UI-initiated reload events (e.g., pull-to-refresh).
     * This encapsulates the internal loading logic and allows for future extensions
     * of reload-specific behaviors without affecting initial loading.
     */
    fun reloadMemberList() {
        getGroupMembers()
    }

    fun showAddMemberSheet() {
        prepareAddMemberFlow()
        viewModelScope.launch {
            _memberManagementState.update { it.copy(sheetState = MemberManagementSheetState.AddMember) }
        }
    }

    fun showMemberActionSheet() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(sheetState = MemberManagementSheetState.MemberAction) }
        }
    }

    private fun performUserSearch(query: String) {
        viewModelScope.launch {
            _addMemberState.update { it.copy(isSearching = true) }

            performSearchUsersUseCase(query).onSuccess { searchResult ->
                _addMemberState.update { it.copy(searchResult = searchResult.users) }
            }.onFailure { exception ->
                Timber.e(exception, "Lỗi tìm kiếm người dùng")
            }

            _addMemberState.update { it.copy(isSearching = false) }
        }
    }

    fun onQuerySearchChange(newQuery: String) {
        _addMemberState.update { it.copy(querySearch = newQuery) }
    }

    fun onSelectedMemberAdd(newMember: User) {
        _addMemberState.update { it.copy(selectedUser = it.selectedUser + newMember) }
    }

    fun onSelectedMemberRemove(newMember: User) {
        _addMemberState.update {
            it.copy(selectedUser = it.selectedUser.filter { user -> user.id != newMember.id }
                .toSet())
        }
    }

    fun prepareAddMemberFlow() {
        viewModelScope.launch {
            _addMemberState.update {
                it.copy(
                    querySearch = "",
                    searchResult = null,
                    selectedUser = emptySet(),
                    isSearching = true
                )
            }

            if (_addMemberState.value.localUsers == null) {
                getLocalUserUseCase().onSuccess { localUsers ->
                    _addMemberState.update {
                        it.copy(localUsers = localUsers, isSearching = false)
                    }
                }.onFailure { exception ->
                    _memberManagementState.update {
                        it.copy(
                            dialogState = MemberManagementDialogState.Failure(
                                exception.getErrorMessage()
                            )
                        )
                    }
                    _addMemberState.update { it.copy(isSearching = false) }
                }
            } else {
                _addMemberState.update { it.copy(isSearching = false) }
            }
        }
    }

    fun showAddMemberConfirmDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.AddMemberConfirm) }
        }
    }

    fun addMembers() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(isMemberAdding = true) }

            addMembersUseCase(
                groupId,
                _addMemberState.value.selectedUser.map { it.id }).onSuccess {
                _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.AddMemberSuccess) }
            }.onFailure { exception ->
                _memberManagementState.update {
                    it.copy(
                        dialogState = MemberManagementDialogState.Failure(
                            exception.getErrorMessage()
                        )
                    )
                }
            }

            _memberManagementState.update { it.copy(isMemberAdding = false) }
        }
    }

    fun prepareMemberActionFlow(memberId: String) {
        viewModelScope.launch {
            _memberActionState.update { it.copy(isFetchingInfo = true) }

            val currentUserId = _memberActionState.value.currentUserId.ifEmpty {
                getCurrentUserIdUseCase().getOrElse { exception ->
                    _memberManagementState.update {
                        it.copy(
                            dialogState = MemberManagementDialogState.Failure(
                                exception.getErrorMessage()
                            )
                        )
                    }
                    _memberActionState.update { state -> state.copy(isFetchingInfo = false) }
                    return@launch
                }
            }

            val selectedMember =
                memberManagementState.value.members.find { it.userId?.id == memberId }
            if (selectedMember == null) {
                _memberManagementState.update {
                    it.copy(
                        dialogState = MemberManagementDialogState.Failure(
                            UiText.StringResource(R.string.member_info_not_found)
                        )
                    )
                }
                _memberActionState.update { it.copy(isFetchingInfo = false) }
                return@launch
            }

            val currentUserRole = memberManagementState.value.members
                .find { it.userId?.id == currentUserId }?.role ?: Role.MEMBER

            val isCurrentUserOwner = currentUserRole == Role.OWNER
            val isCurrentUserAdmin = currentUserRole == Role.ADMIN

            val isSelectedMemberSelf = selectedMember.userId?.id == currentUserId
            val isSelectedMemberOwner = selectedMember.role == Role.OWNER
            val isSelectedMemberAdmin = selectedMember.role == Role.ADMIN
            val isSelectedMemberMember = selectedMember.role == Role.MEMBER

            _memberActionState.update {
                it.copy(
                    currentUserId = currentUserId,
                    selectedMemberInfo = selectedMember.userId,
                    selectedMemberRole = selectedMember.role,

                    /**
                     * Group member role hierarchy and permissions:
                     * - OWNER: Holds absolute control; can transfer ownership, promote/demote admins, and kick any member.
                     * - ADMIN: Holds moderate control; can kick standard MEMBERS, but cannot modify roles or kick other ADMINS/OWNERS.
                     * - MEMBER: Has standard view-only access with no administrative or moderation privileges.
                     */
                    isDirectMessageVisible = !isSelectedMemberSelf,
                    isTransferOwnerVisible = isCurrentUserOwner && !isSelectedMemberSelf,
                    isPromoteToAdminVisible = isCurrentUserOwner && isSelectedMemberMember && !isSelectedMemberSelf,
                    isDemoteToMemberVisible = isCurrentUserOwner && isSelectedMemberAdmin && !isSelectedMemberSelf,
                    isDeleteMemberVisible = !isSelectedMemberOwner && !isSelectedMemberSelf && (
                            isCurrentUserOwner || (isCurrentUserAdmin && isSelectedMemberMember)
                            ),
                    isFetchingInfo = false
                )
            }
        }
    }

    fun clearMemberActionFlow() {
        _memberActionState.update {
            it.copy(
                selectedMemberInfo = null,
                selectedMemberRole = Role.MEMBER,
                isDirectMessageVisible = false,
                isTransferOwnerVisible = false,
                isPromoteToAdminVisible = false,
                isDemoteToMemberVisible = false,
                isDeleteMemberVisible = false,
                isFetchingInfo = false
            )
        }
    }

    fun showPromoteConfirmDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.PromoteConfirm) }
        }
    }

    fun showDemoteConfirmDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.DemoteConfirm) }
        }
    }

    private suspend fun changeRole(newRole: Role) {
        _memberActionState.value.selectedMemberInfo?.let {
            changeRoleUseCase(groupId, it.id, newRole).onSuccess {
                _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.ChangeRoleSuccess) }
            }.onFailure { exception ->
                _memberManagementState.update {
                    it.copy(
                        dialogState = MemberManagementDialogState.Failure(
                            exception.getErrorMessage()
                        )
                    )
                }
            }
        }
    }

    fun promoteMemberToAdmin() {
        viewModelScope.launch {
            changeRole(Role.ADMIN)
        }
    }

    fun demoteMemberToMember() {
        viewModelScope.launch {
            changeRole(Role.MEMBER)
        }
    }

    fun showDeleteMemberConfirmDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.DeleteMemberConfirm) }
        }
    }

    fun deleteMember() {
        viewModelScope.launch {
            _memberActionState.value.selectedMemberInfo?.let {
                deleteMemberUseCase(groupId, it.id).onSuccess {
                    _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.DeleteMemberSuccess) }
                }.onFailure { exception ->
                    _memberManagementState.update {
                        it.copy(
                            dialogState = MemberManagementDialogState.Failure(
                                exception.getErrorMessage()
                            )
                        )
                    }
                }
            }
        }
    }

    fun showTransferOwnerConfirmDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.TransferOwnerConfirm) }
        }
    }

    fun transferOwner() {
        viewModelScope.launch {
            _memberActionState.value.selectedMemberInfo?.let {
                transferOwnerUseCase(groupId, it.id).onSuccess { members ->
                    cleanAndUpdateMemberListState(members)
                    _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.TransferOwnerSuccess) }
                }.onFailure { exception ->
                    _memberManagementState.update {
                        it.copy(
                            dialogState = MemberManagementDialogState.Failure(
                                exception.getErrorMessage()
                            )
                        )
                    }
                }
            }
        }
    }

    fun dismissSheet() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(sheetState = MemberManagementSheetState.Dismiss) }
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            _memberManagementState.update { it.copy(dialogState = MemberManagementDialogState.Dismiss) }
        }
    }
}