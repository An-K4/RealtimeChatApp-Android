package com.example.realtimechatapp.ui.screens.groups.crud

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText.*
import com.example.realtimechatapp.domain.model.Role
import com.example.realtimechatapp.ui.components.ActionItem
import com.example.realtimechatapp.ui.components.BadgedAvatar
import com.example.realtimechatapp.ui.components.ConfirmationDialog
import com.example.realtimechatapp.ui.components.ContactListItem
import com.example.realtimechatapp.ui.components.CustomClickableText
import com.example.realtimechatapp.ui.components.ErrorPlaceholder
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    navController: NavController,
    memberManagementViewModel: MemberManagementViewModel = hiltViewModel()
) {
    val memberManagementState by memberManagementViewModel.memberManagementState.collectAsStateWithLifecycle()
    val addMemberState by memberManagementViewModel.addMemberState.collectAsStateWithLifecycle()
    val memberActionState by memberManagementViewModel.memberActionState.collectAsStateWithLifecycle()

    val addMemberSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val memberActionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiScope = rememberCoroutineScope()

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
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        with(memberManagementViewModel) {
                            prepareAddMemberFlow()
                            showAddMemberSheet()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (memberManagementState.isMemberAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add"
                        )

                        Text(StringResource(R.string.add_member_to_group).asString())
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (memberManagementState.isIncompleteList) {
                    CustomClickableText(
                        StringResource(R.string.note_incomplete_member_list).asString(),
                        StringResource(R.string.reload).asString(),
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
                                    Role.OWNER -> StringResource(R.string.owner)
                                        .asString()

                                    Role.ADMIN -> StringResource(R.string.admin)
                                        .asString()

                                    else -> StringResource(R.string.member)
                                        .asString()
                                },
                                onItemClicked = {
                                    with(memberManagementViewModel) {
                                        prepareMemberActionFlow(member.userId.id)
                                        showMemberActionSheet()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    when (memberManagementState.sheetState) {
        MemberManagementViewModel.MemberManagementSheetState.Dismiss -> {}
        MemberManagementViewModel.MemberManagementSheetState.AddMember -> {
            ModalBottomSheet(
                sheetState = addMemberSheetState,
                onDismissRequest = {
                    uiScope.launch { addMemberSheetState.hide() }.invokeOnCompletion {
                        if (!addMemberSheetState.isVisible) memberManagementViewModel.dismissSheet()
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = StringResource(R.string.add_member_to_group).asString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = addMemberState.querySearch,
                        onValueChange = { memberManagementViewModel.onQuerySearchChange(it) },
                        placeholder = {
                            Text(
                                text = StringResource(R.string.type_a_keyword).asString(),
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

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = addMemberState.searchResult ?: addMemberState.localUsers
                            ?: emptyList(),
                            key = { member -> member.id }
                        ) { user ->
                            val isChecked = addMemberState.selectedUser.any { it.id == user.id }
                            val isInMemberList =
                                memberManagementState.members.any { it.userId?.id == user.id }
                            val checkAction = {
                                if (!isChecked) {
                                    memberManagementViewModel.onSelectedMemberAdd(user)
                                } else {
                                    memberManagementViewModel.onSelectedMemberRemove(user)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Checkbox(
                                    checked = isChecked || isInMemberList,
                                    enabled = !isInMemberList,
                                    onCheckedChange = { checkAction() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                        disabledCheckedColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                ContactListItem(
                                    avatar = user.avatar ?: "",
                                    name = user.fullName,
                                    additionalInfo = user.email,
                                    onItemClicked = { if (!isInMemberList) checkAction() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (addMemberState.selectedUser.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(
                                items = addMemberState.selectedUser.toList(),
                                key = { member -> member.id }
                            ) { user ->
                                BadgedAvatar(60.dp, user.avatar, Icons.Default.Close) {
                                    memberManagementViewModel.onSelectedMemberRemove(user)
                                }
                            }
                        }
                    }

                    Row {
                        OutlinedButton(
                            onClick = {
                                uiScope.launch { addMemberSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!addMemberSheetState.isVisible) memberManagementViewModel.dismissSheet()
                                    }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = StringResource(R.string.cancel).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }

                        Button(
                            onClick = { memberManagementViewModel.showAddMemberConfirmDialog() },
                            enabled = !addMemberState.selectedUser.isEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RealtimeGreen,
                                contentColor = Color.White,
                                disabledContainerColor = RealtimeGreen.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = StringResource(R.string.add).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        MemberManagementViewModel.MemberManagementSheetState.MemberAction -> {
            ModalBottomSheet(
                sheetState = memberActionSheetState,
                onDismissRequest = {
                    uiScope.launch { memberActionSheetState.hide() }.invokeOnCompletion {
                        if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                        memberManagementViewModel.clearMemberActionFlow()
                    }
                }
            ) {
                if (memberActionState.isFetchingInfo) {
                    CircularProgressIndicator()
                } else {
                    ContactListItem(
                        avatar = memberActionState.selectedMemberInfo?.avatar ?: "",
                        name = memberActionState.selectedMemberInfo?.fullName
                            ?: StringResource(R.string.clover_chatty_user).asString(),
                        additionalInfo = memberActionState.selectedMemberRole.toDisplayName()
                            .asString(),
                        onItemClicked = {
                            uiScope.launch { memberActionSheetState.hide() }.invokeOnCompletion {
                                if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                                memberActionState.selectedMemberInfo?.let {
                                    navController.navigate(
                                        Screen.DetailMessage.createRoute(it.id)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        if (memberActionState.isDirectMessageVisible) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionItem(
                                        icon = Icons.AutoMirrored.Filled.Chat,
                                        title = StringResource(R.string.direct_message).asString(),
                                        onClick = {
                                            uiScope.launch { memberActionSheetState.hide() }
                                                .invokeOnCompletion {
                                                    if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                                                    memberActionState.selectedMemberInfo?.let {
                                                        navController.navigate(
                                                            Screen.DetailMessage.createRoute(it.id)
                                                        )
                                                    }
                                                }
                                        },
                                        trailingContent = {}
                                    )
                                }
                            }
                        }

                        if (memberActionState.isTransferOwnerVisible) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionItem(
                                        icon = Icons.Default.Key,
                                        title = StringResource(R.string.transfer_group_ownership).asString(),
                                        onClick = {
                                            memberManagementViewModel.showTransferOwnerConfirmDialog()
                                        },
                                        trailingContent = {}
                                    )
                                }
                            }
                        }

                        if (memberActionState.isPromoteToAdminVisible) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionItem(
                                        icon = Icons.Default.ManageAccounts,
                                        title = StringResource(R.string.promote_as_admin).asString(),
                                        onClick = {
                                            memberManagementViewModel.showPromoteConfirmDialog()
                                        },
                                        trailingContent = {}
                                    )
                                }
                            }
                        }

                        if (memberActionState.isDemoteToMemberVisible) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionItem(
                                        icon = Icons.Default.Person2,
                                        title = StringResource(R.string.demote_to_member).asString(),
                                        onClick = {
                                            memberManagementViewModel.showDemoteConfirmDialog()
                                        },
                                        trailingContent = {}
                                    )
                                }
                            }
                        }

                        if (memberActionState.isDeleteMemberVisible) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionItem(
                                        icon = Icons.Default.PersonOff,
                                        title = StringResource(R.string.delete_member).asString(),
                                        isDangerAction = true,
                                        onClick = {
                                            memberManagementViewModel.showDeleteMemberConfirmDialog()
                                        },
                                        trailingContent = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (val dialogState = memberManagementState.dialogState) {
        MemberManagementViewModel.MemberManagementDialogState.Dismiss -> {}
        MemberManagementViewModel.MemberManagementDialogState.AddMemberConfirm -> {
            val newMembersSize = addMemberState.selectedUser.size

            ConfirmationDialog(
                title = StringResource(R.string.confirm).asString(),
                message = PluralsResource(
                    R.plurals.new_members_added_confirm,
                    newMembersSize,
                    newMembersSize
                ).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onConfirm = {
                    memberManagementViewModel.dismissDialog()
                    uiScope.launch { addMemberSheetState.hide() }
                        .invokeOnCompletion {
                            if (!addMemberSheetState.isVisible) memberManagementViewModel.dismissSheet()
                            memberManagementViewModel.addMembers()
                        }
                },
                onDismiss = {
                    memberManagementViewModel.dismissDialog()
                }
            )
        }

        MemberManagementViewModel.MemberManagementDialogState.AddMemberSuccess -> {
            val newMembersSize = addMemberState.selectedUser.size

            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = PluralsResource(
                    R.plurals.new_members_added,
                    newMembersSize,
                    newMembersSize
                ).asString(),
                isSuccess = true,
                onDismiss = {
                    with(memberManagementViewModel) {
                        reloadMemberList()
                        dismissDialog()
                    }
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.PromoteConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.confirm).asString(),
                message = StringResource(R.string.promote_admin_confirm).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onConfirm = {
                    memberManagementViewModel.dismissDialog()
                    uiScope.launch { memberActionSheetState.hide() }
                        .invokeOnCompletion {
                            if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                            memberManagementViewModel.promoteMemberToAdmin()
                        }
                },
                onDismiss = {
                    memberManagementViewModel.dismissDialog()
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.DemoteConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.confirm).asString(),
                message = StringResource(R.string.demote_member_confirm).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onConfirm = {
                    memberManagementViewModel.dismissDialog()
                    uiScope.launch { memberActionSheetState.hide() }
                        .invokeOnCompletion {
                            if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                            memberManagementViewModel.demoteMemberToMember()
                        }
                },
                onDismiss = {
                    memberManagementViewModel.dismissDialog()
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.DeleteMemberConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.confirm).asString(),
                message = StringResource(R.string.delete_member_confirm).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = true,
                onConfirm = {
                    memberManagementViewModel.dismissDialog()
                    uiScope.launch { memberActionSheetState.hide() }
                        .invokeOnCompletion {
                            if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                            memberManagementViewModel.deleteMember()
                        }
                },
                onDismiss = {
                    memberManagementViewModel.dismissDialog()
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.TransferOwnerConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.confirm).asString(),
                message = StringResource(R.string.transfer_owner_confirm).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = true,
                onConfirm = {
                    memberManagementViewModel.dismissDialog()
                    uiScope.launch { memberActionSheetState.hide() }
                        .invokeOnCompletion {
                            if (!memberActionSheetState.isVisible) memberManagementViewModel.dismissSheet()
                            memberManagementViewModel.transferOwner()
                        }
                },
                onDismiss = {
                    memberManagementViewModel.dismissDialog()
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.ChangeRoleSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.change_role_success).asString(),
                isSuccess = true,
                onDismiss = {
                    with(memberManagementViewModel) {
                        reloadMemberList()
                        clearMemberActionFlow()
                        dismissDialog()
                    }
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.DeleteMemberSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.delete_member_success).asString(),
                isSuccess = true,
                onDismiss = {
                    with(memberManagementViewModel) {
                        reloadMemberList()
                        clearMemberActionFlow()
                        dismissDialog()
                    }
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.TransferOwnerSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.transfer_owner_success).asString(),
                isSuccess = true,
                onDismiss = {
                    with(memberManagementViewModel) {
                        clearMemberActionFlow()
                        dismissDialog()
                    }
                }
            )
        }

        is MemberManagementViewModel.MemberManagementDialogState.Failure -> {
            NotificationDialog(
                title = StringResource(R.string.error).asString(),
                message = dialogState.message.asString(),
                isSuccess = false,
                onDismiss = { memberManagementViewModel.dismissDialog() }
            )
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

                Text(StringResource(R.string.add_member_to_group).asString())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AddMemberBottomSheet() {
    RealtimeChatAppTheme {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = StringResource(R.string.add_member_to_group).asString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = {
                        Text(
                            text = StringResource(R.string.type_a_keyword).asString(),
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

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = true,
                                enabled = false,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "Vũ Quốc An",
                                additionalInfo = "an@vu.demo",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = false,
                                enabled = true,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "Donald Trump - President",
                                additionalInfo = "donald@trump.president",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = true,
                                enabled = false,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "Tony Stark - Iron Man",
                                additionalInfo = "tony@millionaire.pro",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = false,
                                enabled = true,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "NPC 4",
                                additionalInfo = "npc4@clover.chatty",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = false,
                                enabled = true,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "NPC 5",
                                additionalInfo = "npc5@clover.chatty",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = true,
                                enabled = true,
                                onCheckedChange = { },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onBackground,
                                    disabledCheckedColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ContactListItem(
                                avatar = "",
                                name = "NPC 6",
                                additionalInfo = "npc6@clover.chatty",
                                onItemClicked = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        BadgedAvatar(60.dp, null, Icons.Default.Close) { }
                    }

                    item {
                        BadgedAvatar(60.dp, null, Icons.Default.Close) { }
                    }

                    item {
                        BadgedAvatar(60.dp, null, Icons.Default.Close) { }
                    }

                    item {
                        BadgedAvatar(60.dp, null, Icons.Default.Close) { }
                    }

                    item {
                        BadgedAvatar(60.dp, null, Icons.Default.Close) { }
                    }
                }

                Row {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = StringResource(R.string.cancel).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }

                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RealtimeGreen,
                            contentColor = Color.White,
                            disabledContainerColor = RealtimeGreen.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = StringResource(R.string.add).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MemberActionSheet() {
    RealtimeChatAppTheme {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {}
        ) {
            ContactListItem(
                avatar = "",
                name = "Vũ Quốc An",
                additionalInfo = "Owner",
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = StringResource(R.string.direct_message).asString(),
                            onClick = {},
                            trailingContent = {}
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionItem(
                            icon = Icons.Default.Key,
                            title = StringResource(R.string.transfer_group_ownership).asString(),
                            onClick = {},
                            trailingContent = {}
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionItem(
                            icon = Icons.Default.ManageAccounts,
                            title = StringResource(R.string.promote_as_admin).asString(),
                            onClick = {},
                            trailingContent = {}
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionItem(
                            icon = Icons.Default.Person2,
                            title = StringResource(R.string.demote_to_member).asString(),
                            onClick = {},
                            trailingContent = {}
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionItem(
                            icon = Icons.Default.PersonOff,
                            title = StringResource(R.string.delete_member).asString(),
                            isDangerAction = true,
                            onClick = {},
                            trailingContent = {}
                        )
                    }
                }
            }
        }
    }
}