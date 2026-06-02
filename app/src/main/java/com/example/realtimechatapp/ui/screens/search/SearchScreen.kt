package com.example.realtimechatapp.ui.screens.search

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.ui.components.SearchResultItem
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        searchViewModel.searchEvents.collect {
            when (it) {
                is SearchViewModel.SearchEvents.SaveNewUserSuccess -> {
                    navController.navigate(Screen.DetailMessage.createRoute(it.newUserId))
                }
                is SearchViewModel.SearchEvents.Failure -> {
                    Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            OutlinedTextField(
                value = searchState.query,
                onValueChange = { searchViewModel.onQueryChange(it) },
                placeholder = {
                    Text(
                        text = UiText.StringResource(R.string.type_a_keyword).asString(),
                        color = MaterialTheme.colorScheme.surface
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        TabRow(
            selectedTabIndex = searchState.currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = searchState.currentTab == SearchTabs.USER,
                onClick = { searchViewModel.onTabSelected(SearchTabs.USER) },
                text = { Text(UiText.StringResource(R.string.everyone).asString()) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground
            )

            Tab(
                selected = searchState.currentTab == SearchTabs.GROUP,
                onClick = { searchViewModel.onTabSelected(SearchTabs.GROUP) },
                text = { Text(UiText.StringResource(R.string.groups).asString()) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (searchState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (searchState.query.isEmpty()) {
                Text(
                    text = UiText.StringResource(R.string.type_something_to_search).asString(),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surface,
                )
            } else {
                when (searchState.currentTab) {
                    SearchTabs.USER -> {
                        val userSearchResult: List<User>? = searchState.userSearchResult

                        if (userSearchResult == null) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        } else if (userSearchResult.isEmpty()) {
                            Text(
                                text = UiText.StringResource(R.string.no_user_found).asString(),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            LazyColumn(
                                state = rememberLazyListState(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.matchParentSize()
                            ) {
                                items(
                                    items = userSearchResult,
                                    key = { user -> user.id }
                                ) { user ->
                                    SearchResultItem(
                                        avatar = user.avatar ?: "",
                                        name = user.fullName,
                                        additionalInfo = user.email,
                                        onItemClicked = {
                                            searchViewModel.saveNewUserInfo(user)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    SearchTabs.GROUP -> {
                        val groupSearchResult: List<Group>? = searchState.groupSearchResult

                        if (groupSearchResult == null) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        } else if (groupSearchResult.isEmpty()) {
                            Text(
                                text = UiText.StringResource(R.string.no_group_found).asString(),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            LazyColumn(
                                state = rememberLazyListState(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.matchParentSize()
                            ) {
                                items(
                                    items = groupSearchResult,
                                    key = { group -> group.id }
                                ) { group ->
                                    SearchResultItem(
                                        avatar = group.avatar ?: "",
                                        name = group.name,
                                        additionalInfo = UiText.StringResource(
                                            R.string.group_status,
                                            group.members.size
                                        ).asString(),
                                        onItemClicked = {
                                            navController.navigate(Screen.DetailGroup.createRoute(group.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = {
                        Text(
                            text = "Nhập từ khóa",
                            color = MaterialTheme.colorScheme.surface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            TabRow(
                selectedTabIndex = 0,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = true,
                    onClick = {},
                    text = { Text("Mọi người") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground
                )

                Tab(
                    selected = false,
                    onClick = {},
                    text = { Text("Nhóm") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}