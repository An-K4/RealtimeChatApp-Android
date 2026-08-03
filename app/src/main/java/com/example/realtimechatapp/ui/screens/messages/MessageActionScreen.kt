package com.example.realtimechatapp.ui.screens.messages

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.ActionItem
import com.example.realtimechatapp.ui.components.ConfirmationDialog
import com.example.realtimechatapp.ui.components.ContactListItem
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.components.ToggleSettingItem
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MessageActionScreen(
    navController: NavController,
    messageActionViewModel: MessageActionViewModel = hiltViewModel()
) {
    val messageActionState by messageActionViewModel.messageActionState.collectAsStateWithLifecycle()
    val addToGroupState by messageActionViewModel.addToGroupState.collectAsStateWithLifecycle()
    val lifeCycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val addToGroupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiScope = rememberCoroutineScope()

    // Remember callbacks
    val onMuteNotificationChange = remember {
        { isChecked: Boolean ->
            messageActionViewModel.onMuteNotificationChange(isChecked)
        }
    }

    val onMediaFilesClick = remember {
        {
            Toast.makeText(
                context,
                UiText.StringResource(R.string.in_development).asString(context),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onAddToGroupClick = remember {
        {
            messageActionViewModel.showAddUserToGroupSheet()
        }
    }

    // Collect events to show toast
    LaunchedEffect(lifeCycleOwner) {
        lifeCycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            messageActionViewModel.messageActionEvent.collect { event ->
                when (event) {
                    is MessageActionViewModel.MessageActionEvent.Failure -> Toast.makeText(
                        context,
                        event.message.asString(context),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    when (messageActionState.sheetState) {
        is MessageActionViewModel.MessageActionSheetState.Dismiss -> {

        }

        is MessageActionViewModel.MessageActionSheetState.AddUserToGroup -> {
            val onGroupSearchQueryChange = remember {
                { query: String ->
                    messageActionViewModel.onGroupSearchQueryChange(query)
                }
            }

            ModalBottomSheet(
                sheetState = addToGroupSheetState,
                onDismissRequest = {
                    uiScope.launch { addToGroupSheetState.hide() }.invokeOnCompletion {
                        if (!addToGroupSheetState.isVisible) {
                            messageActionViewModel.dismissSheet()
                        }
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
                        text = UiText.StringResource(R.string.add_to_group).asString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addToGroupState.querySearch,
                        onValueChange = onGroupSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Tìm kiếm nhóm...",
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
                        trailingIcon = {
                            if (addToGroupState.querySearch.isNotEmpty()) {
                                IconButton(
                                    onClick = { messageActionViewModel.onGroupSearchQueryChange("") }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "clear",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
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

                    Spacer(modifier = Modifier.height(8.dp))

                    if (addToGroupState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val groupList = addToGroupState.searchResult ?: addToGroupState.localGroups ?: emptyList()

                            if (groupList.isEmpty() && addToGroupState.querySearch.isNotEmpty()) {
                                item {
                                    Text(
                                        text = UiText.StringResource(R.string.no_group_found).asString(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                items(
                                    items = groupList,
                                    key = { it.group.id }
                                ) { item ->
                                    ContactListItem(
                                        avatar = item.group.avatar ?: "",
                                        name = item.group.name,
                                        additionalInfo = if (item.isAlreadyMember) {
                                            UiText.StringResource(R.string.already_in_group).asString()
                                        } else {
                                            UiText.StringResource(R.string.group_status, item.group.members.size).asString()
                                        },
                                        onItemClicked = {
                                            if (!item.isAlreadyMember) {
                                                messageActionViewModel.showAddToGroupConfirmDialog(item.group)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(if (item.isAlreadyMember) 0.5f else 1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = messageActionState.avatar.ifEmpty { null },
            contentDescription = "Avatar",
            placeholder = painterResource(R.drawable.default_avatar),
            error = painterResource(R.drawable.default_avatar),
            fallback = painterResource(R.drawable.default_avatar),
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = messageActionState.fullName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = messageActionState.email,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            modifier = Modifier.height(2.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ToggleSettingItem(
                    icon = Icons.Default.NotificationsOff,
                    title = UiText.StringResource(R.string.mute_notification).asString(),
                    isChecked = messageActionState.muteNotifications,
                    onCheckedChange = onMuteNotificationChange
                )
            }

            item {
                ActionItem(
                    icon = Icons.Default.Image,
                    title = UiText.StringResource(R.string.media_files).asString(),
                    onClick = onMediaFilesClick,
                )
            }

            item {
                ActionItem(
                    icon = Icons.Default.Groups,
                    title = UiText.StringResource(R.string.add_to_group).asString(),
                    onClick = onAddToGroupClick,
                    trailingContent = {}
                )
            }
        }
    }

    when (val dialogState = messageActionState.dialogState) {
        is MessageActionViewModel.MessageActionDialogState.AddToGroupConfirm -> {
            ConfirmationDialog(
                title = UiText.StringResource(R.string.confirm).asString(),
                message = UiText.StringResource(R.string.add_to_group_confirm, dialogState.group.name).asString(),
                dismissText = UiText.StringResource(R.string.cancel).asString(),
                confirmText = UiText.StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onDismiss = {
                    messageActionViewModel.dismissDialog()
                },
                onConfirm = {
                    uiScope.launch { addToGroupSheetState.hide() }.invokeOnCompletion {
                        if (!addToGroupSheetState.isVisible) {
                            messageActionViewModel.dismissSheet()
                        }
                    }
                    messageActionViewModel.addToSelectedGroup(dialogState.group.id)
                }
            )
        }

        is MessageActionViewModel.MessageActionDialogState.AddToGroupSuccess -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.success).asString(),
                message = UiText.StringResource(R.string.add_to_group_success).asString(),
                isSuccess = true,
                onDismiss = {
                    messageActionViewModel.dismissDialog()
                }
            )
        }

        is MessageActionViewModel.MessageActionDialogState.Failure -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.error).asString(),
                message = dialogState.message.asString(),
                isSuccess = false,
                onDismiss = {
                    messageActionViewModel.dismissDialog()
                }
            )
        }

        MessageActionViewModel.MessageActionDialogState.Dismiss -> Unit
    }
}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MessageActionScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = null,
                contentDescription = "Avatar",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar),
                fallback = painterResource(R.drawable.default_avatar),
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Vũ Quốc An",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "an@vu.demo",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.height(2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    ToggleSettingItem(
                        icon = Icons.Default.NotificationsOff,
                        title = UiText.StringResource(R.string.mute_notification).asString(),
                        isChecked = false,
                        onCheckedChange = { }
                    )
                }

                item {
                    ActionItem(
                        icon = Icons.Default.Image,
                        title = UiText.StringResource(R.string.media_files).asString(),
                        onClick = { },
                    )
                }

                item {
                    ActionItem(
                        icon = Icons.Default.Groups,
                        title = UiText.StringResource(R.string.add_to_group).asString(),
                        onClick = {},
                        trailingContent = {}
                    )
                }
            }
        }
    }
}