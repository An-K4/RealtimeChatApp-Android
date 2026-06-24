package com.example.realtimechatapp.ui.screens.groups.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.group.CreateGroupUseCase
import com.example.realtimechatapp.domain.usecase.user.GetLocalUserUseCase
import com.example.realtimechatapp.domain.usecase.user.PerformSearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val performSearchUsersUseCase: PerformSearchUsersUseCase
) : ViewModel() {
    data class CreateGroupState(
        val groupName: String = "",
        val groupMembers: Set<User> = emptySet(),
        val isLoading: Boolean = false
    )

    data class AddMemberState(
        val querySearch: String = "",
        val localUsers: List<User> = emptyList(),
        val searchResult: List<User>? = null,
        val selectedUser: Set<User> = emptySet(),
        val isSearching: Boolean = false
    )

    sealed class CreateGroupEvent {
        data class CreateGroupSuccess(val groupId: String) : CreateGroupEvent()
        data class Failure(val message: UiText) : CreateGroupEvent()
    }

    private var _createGroupState = MutableStateFlow(CreateGroupState())
    val createGroupState = _createGroupState.asStateFlow()

    private var _addMemberState = MutableStateFlow(AddMemberState())
    val addMemberState = _addMemberState.asStateFlow()

    private val _createGroupEvent = Channel<CreateGroupEvent>()
    val createGroupEvent = _createGroupEvent.receiveAsFlow()

    init {
        getLocalUsers()
        viewModelScope.launch {
            _addMemberState
                .map { state -> state.querySearch }
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

    private fun getLocalUsers() {
        viewModelScope.launch {
            _addMemberState.update { it.copy(isSearching = true) }

            getLocalUserUseCase().onSuccess { localUsers ->
                _addMemberState.update { it.copy(localUsers = localUsers) }
            }.onFailure { message ->
                _createGroupEvent.send(CreateGroupEvent.Failure(message.getErrorMessage()))
            }

            _addMemberState.update { it.copy(isSearching = false) }
        }
    }

    private fun performUserSearch(query: String) {
        viewModelScope.launch {
            _addMemberState.update { it.copy(isSearching = true) }

            performSearchUsersUseCase(query).onSuccess { searchResult ->
                _addMemberState.update { it.copy(searchResult = searchResult.users) }
            }.onFailure { message ->
                _createGroupEvent.send(CreateGroupEvent.Failure(message.getErrorMessage()))
            }

            _addMemberState.update { it.copy(isSearching = false) }
        }
    }

    fun onGroupNameChange(newGroupName: String) {
        _createGroupState.update { it.copy(groupName = newGroupName) }
    }

    fun onGroupMemberRemove(removeMember: User) {
        _createGroupState.update {
            it.copy(groupMembers = it.groupMembers.filter { member -> member.id != removeMember.id }
                .toSet())
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

    fun onNewMemberAdded() {
        _createGroupState.update { it.copy(groupMembers = _addMemberState.value.selectedUser) }
    }

    fun prepareAddMemberFlow() {
        _addMemberState.update {
            it.copy(
                querySearch = "",
                searchResult = null,
                selectedUser = _createGroupState.value.groupMembers
            )
        }
    }

    fun clearAddMemberFlow() {
        _addMemberState.update {
            it.copy(
                querySearch = "",
                searchResult = null,
                selectedUser = emptySet()
            )
        }
    }

    fun createGroup() {
        viewModelScope.launch {
            _createGroupState.update { it.copy(isLoading = true) }

            with(_createGroupState.value) {
                createGroupUseCase(groupName, groupMembers.map { it.id }).onSuccess { groupId ->
                    _createGroupEvent.send(CreateGroupEvent.CreateGroupSuccess(groupId))
                }.onFailure { message ->
                    _createGroupEvent.send(CreateGroupEvent.Failure(message.getErrorMessage()))
                }
            }

            _createGroupState.update { it.copy(isLoading = false) }
        }
    }
}