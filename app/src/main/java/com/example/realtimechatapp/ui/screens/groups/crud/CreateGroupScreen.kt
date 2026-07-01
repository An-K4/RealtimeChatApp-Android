package com.example.realtimechatapp.ui.screens.groups.crud

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.ContactListItem
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    createGroupViewModel: CreateGroupViewModel = hiltViewModel()
) {
    val createGroupState by createGroupViewModel.createGroupState.collectAsStateWithLifecycle()
    val addMemberState by createGroupViewModel.addMemberState.collectAsStateWithLifecycle()
    val memberListState = rememberLazyListState()

    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    var showAddMemberSheet by remember { mutableStateOf(false) }
    val addMemberSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        lifeCycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            createGroupViewModel.createGroupEvent.collect { event ->
                when (event) {
                    is CreateGroupViewModel.CreateGroupEvent.CreateGroupSuccess -> {
                        Toast.makeText(
                            context,
                            UiText.StringResource(R.string.create_group_success).asString(context),
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate(Screen.DetailGroup.createRoute(event.groupId)) {
                            popUpTo(Screen.CreateGroup.route) { inclusive = true }
                        }
                    }

                    is CreateGroupViewModel.CreateGroupEvent.Failure -> {
                        Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        OutlinedTextField(
            value = createGroupState.groupName,
            onValueChange = { createGroupViewModel.onGroupNameChange(it) },
            label = { Text(UiText.StringResource(R.string.group_name).asString()) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = UiText.StringResource(R.string.member).asString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                createGroupViewModel.prepareAddMemberFlow()
                showAddMemberSheet = true
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

            Text(
                text = UiText.StringResource(R.string.add_member_to_group).asString(),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = memberListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = createGroupState.groupMembers.toList(),
                key = { member -> member.id }
            ) { groupMember ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ContactListItem(
                        avatar = groupMember.avatar ?: "",
                        name = groupMember.fullName,
                        additionalInfo = groupMember.email,
                        onItemClicked = { },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { createGroupViewModel.onGroupMemberRemove(groupMember) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "remove member",
                        )
                    }
                }
            }
        }

        Button(
            onClick = { createGroupViewModel.createGroup() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !createGroupState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (createGroupState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else Text(
                text = stringResource(R.string.create_group),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }

    if (showAddMemberSheet) {
        ModalBottomSheet(
            sheetState = addMemberSheetState,
            onDismissRequest = {
                showAddMemberSheet = false
                createGroupViewModel.clearAddMemberFlow()
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
                    text = UiText.StringResource(R.string.add_member_to_group).asString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = addMemberState.querySearch,
                    onValueChange = { createGroupViewModel.onQuerySearchChange(it) },
                    placeholder = {
                        Text(
                            text = UiText.StringResource(R.string.type_a_keyword).asString(),
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
                        items = addMemberState.searchResult ?: addMemberState.localUsers,
                        key = { member -> member.id }
                    ) { user ->
                        val isChecked = addMemberState.selectedUser.any { it.id == user.id }
                        val checkAction = {
                            if (!isChecked) {
                                createGroupViewModel.onSelectedMemberAdd(user)
                            } else {
                                createGroupViewModel.onSelectedMemberRemove(user)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checkAction() }
                            )

                            ContactListItem(
                                avatar = user.avatar ?: "",
                                name = user.fullName,
                                additionalInfo = user.email,
                                onItemClicked = { checkAction() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row {
                    OutlinedButton(
                        onClick = {
                            uiScope.launch { addMemberSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!addMemberSheetState.isVisible) showAddMemberSheet = false
                                }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = UiText.StringResource(R.string.cancel).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }

                    Button(
                        onClick = {
                            uiScope.launch { addMemberSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!addMemberSheetState.isVisible) showAddMemberSheet = false
                                }
                            with(createGroupViewModel) {
                                onNewMemberAdded()
                                clearAddMemberFlow()
                            }
                        },
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
                            text = UiText.StringResource(R.string.add).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CreateGroupScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text(UiText.StringResource(R.string.group_name).asString()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = UiText.StringResource(R.string.member).asString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.create_group),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddMemberSheet() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = UiText.StringResource(R.string.add_member_to_group).asString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
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

            Row {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                ) {
                    Text(
                        text = UiText.StringResource(R.string.cancel).asString(),
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
                        text = UiText.StringResource(R.string.add).asString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}
