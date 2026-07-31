package com.example.realtimechatapp.ui.screens.groups.crud

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.realtimechatapp.ui.components.BadgedAvatar
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun EditGroupScreen(
    navController: NavController,
    editGroupViewModel: EditGroupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val editGroupState by editGroupViewModel.editGroupState.collectAsStateWithLifecycle()
    
    // Remember callbacks
    val onPhotoSelected = remember {
        { uri: android.net.Uri? ->
            if (uri != null) {
                editGroupViewModel.onGroupAvatarChange(uri)
            }
        }
    }
    
    val onGroupNameChange = remember {
        { text: String -> editGroupViewModel.onGroupNameChange(text) }
    }
    
    val onGroupDescriptionChange = remember {
        { text: String -> editGroupViewModel.onGroupDescriptionChange(text) }
    }
    
    val onUpdateGroup = remember {
        { editGroupViewModel.updateGroup() }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPhotoSelected
    )
    
    val onBadgeClick = remember {
        {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }
    
    val onKickedDismiss = remember {
        {
            navController.navigate(Screen.Groups.route) {
                popUpTo(Screen.Groups.route) { inclusive = false }
            }
        }
    }

    // LaunchedEffect with proper key
    LaunchedEffect(lifeCycleOwner) {
        lifeCycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            editGroupViewModel.editGroupEvent.collect { event ->
                when (event) {
                    EditGroupViewModel.EditGroupEvent.EditSuccess -> {
                        Toast.makeText(
                            context,
                            UiText.StringResource(R.string.update_group_success).asString(context),
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }

                    is EditGroupViewModel.EditGroupEvent.Failure -> {
                        Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    when (editGroupState.dialogState) {
        is EditGroupViewModel.EditGroupDialogState.KickedFromGroup -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.warning).asString(context),
                message = "Bạn đã bị xóa khỏi nhóm. Liên hệ admin để biết thêm thông tin.",
                isSuccess = false,
                onDismiss = onKickedDismiss
            )
        }

        EditGroupViewModel.EditGroupDialogState.Dismiss -> {
            // No dialog
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedAvatar(
            currentAvatar = editGroupState.groupAvatar,
            onBadgeClick = onBadgeClick
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editGroupState.groupName,
            onValueChange = onGroupNameChange,
            label = { Text(stringResource(R.string.group_name)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editGroupState.groupDescription ?: "",
            onValueChange = onGroupDescriptionChange,
            label = { Text(stringResource(R.string.group_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 250.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onUpdateGroup,
            modifier = Modifier.fillMaxWidth(),
            enabled = editGroupState.isUpdateEnable && !editGroupState.isUpdating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (editGroupState.isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(R.string.save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun EditGroupScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgedAvatar(
                currentAvatar = "",
                onBadgeClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "Group Demo",
                onValueChange = { },
                label = { Text(stringResource(R.string.group_name)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "Group Demo Description",
                onValueChange = { },
                label = { Text(stringResource(R.string.group_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}