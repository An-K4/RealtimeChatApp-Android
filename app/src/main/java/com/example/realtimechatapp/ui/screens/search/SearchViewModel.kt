package com.example.realtimechatapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.user.PerformSearchUseCase
import com.example.realtimechatapp.domain.usecase.user.SaveNewUserInfoUseCase
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
class SearchViewModel @Inject constructor(
    private val performSearchUseCase: PerformSearchUseCase,
    private val saveNewUserInfoUseCase: SaveNewUserInfoUseCase
) : ViewModel() {
    data class SearchState(
        val query: String = "",
        val currentTab: SearchTabs = SearchTabs.USER,
        val userSearchResult: List<User>? = null,
        val groupSearchResult: List<Group>? = null,
        val isLoading: Boolean = false
    )

    sealed class SearchEvents {
        data class SaveNewUserSuccess(val newUserId: String) : SearchEvents()
        data class Failure(val message: UiText) : SearchEvents()
    }

    private var _searchState = MutableStateFlow(SearchState())
    val searchState = _searchState.asStateFlow()

    private val _searchEvents = Channel<SearchEvents>()
    val searchEvents = _searchEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            _searchState
                .map { state -> state.query }
                .distinctUntilChanged()
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        _searchState.update {
                            it.copy(
                                userSearchResult = null,
                                groupSearchResult = null
                            )
                        }
                    }
                }
        }
    }

    fun onTabSelected(tab: SearchTabs) {
        _searchState.update { it.copy(currentTab = tab) }
    }

    fun onQueryChange(query: String) {
        _searchState.update { it.copy(query = query) }
    }

    private suspend fun performSearch(query: String) {
        _searchState.update { it.copy(isLoading = true) }

        performSearchUseCase(query).onSuccess { searchResult ->
            _searchState.update {
                it.copy(
                    userSearchResult = searchResult.users,
                    groupSearchResult = searchResult.groups
                )
            }
        }.onFailure { exception ->
            _searchEvents.send(SearchEvents.Failure(exception.getErrorMessage()))
        }

        _searchState.update { it.copy(isLoading = false) }
    }

    fun saveNewUserInfo(newUser: User) {
        viewModelScope.launch {
            saveNewUserInfoUseCase(newUser).onSuccess {
                _searchEvents.send(SearchEvents.SaveNewUserSuccess(newUser.id))
            }.onFailure { exception ->
                _searchEvents.send(SearchEvents.Failure(exception.getErrorMessage()))
            }
        }
    }
}