package com.example.realtimechatapp.ui.screens.groups.crud

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.ui.components.ContactListItem
import com.example.realtimechatapp.ui.components.CustomClickableText
import com.example.realtimechatapp.ui.components.ErrorPlaceholder
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun MemberManagementScreen(
    navController: NavController,
    memberManagementViewModel: MemberManagementViewModel = hiltViewModel()
) {
    val memberManagementState by memberManagementViewModel.memberManagementState.collectAsStateWithLifecycle()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (memberManagementState.isLoading) {
            CircularProgressIndicator()
        } else if (memberManagementState.isEmptyMemberList) {
            ErrorPlaceholder {
                memberManagementViewModel.reloadMemberList()
            }
        } else {
            Column(
                modifier = Modifier.matchParentSize().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        // in development
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "add"
                    )

                    Text(UiText.StringResource(R.string.add_member_to_group).asString())
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (memberManagementState.isIncompleteList) {
                    CustomClickableText(
                        UiText.StringResource(R.string.note_incomplete_member_list).asString(),
                        UiText.StringResource(R.string.reload).asString(),
                        "reload",
                        "",
                        "",
                        textSize = 12.sp,
                        onTextClicked = {
                            memberManagementViewModel.reloadMemberList()
                        }
                    )
                }


                LazyColumn(
                    state = rememberLazyListState(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = memberManagementState.members,
                        key = { member -> member.userId?.id ?: "" }
                    ) { member ->
                        member.userId?.let {
                            ContactListItem(
                                avatar = it.avatar ?: "",
                                name = it.fullName,
                                additionalInfo = when (member.role) {
                                    Role.OWNER -> UiText.StringResource(R.string.owner)
                                        .asString()

                                    Role.ADMIN -> UiText.StringResource(R.string.admin)
                                        .asString()

                                    else -> UiText.StringResource(R.string.member_role)
                                        .asString()
                                },
                                onItemClicked = {
                                    // in development
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MemberManagementScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add"
                )

                Text(UiText.StringResource(R.string.add_member_to_group).asString())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: The member list may not be complete.",
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    ContactListItem(
                        avatar = "",
                        name = "Vũ Quốc An",
                        additionalInfo = "Owner",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "Peter Parker - Spider Man",
                        additionalInfo = "Admin",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "Tony Stark - Iron Man",
                        additionalInfo = "Admin",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC 1",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC 2",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC3",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}