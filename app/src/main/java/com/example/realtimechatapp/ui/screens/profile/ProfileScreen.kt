package com.example.realtimechatapp.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.realtimechatapp.common.UiText.*
import com.example.realtimechatapp.ui.components.BadgedAvatar
import com.example.realtimechatapp.ui.components.ConfirmationDialog
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.components.ProfileInfoItem
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.LightBlue
import com.example.realtimechatapp.ui.theme.LightRed
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import com.example.realtimechatapp.ui.theme.Red
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val updateProfileState by profileViewModel.updateProfileState.collectAsState()
    val changePasswordState by profileViewModel.changePasswordState.collectAsState()
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }
    val updateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val changePasswordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val uiScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                profileViewModel.onUpdateAvatarChange(uri)
            }
        }
    )

    LaunchedEffect(Unit) {
        lifeCycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            profileViewModel.profileEvent.collect { event ->
                when (event) {
                    ProfileViewModel.ProfileEvent.NavigateToLogin -> {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                navController.clearBackStack(Screen.Groups.route)
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    is ProfileViewModel.ProfileEvent.Failure -> Toast.makeText(
                        context,
                        event.message.asString(context),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp, horizontal = 10.dp)
    ) {
        if (profileState.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    AsyncImage(
                        model = profileState.avatar,
                        contentDescription = "Avatar",
                        placeholder = painterResource(R.drawable.default_avatar),
                        error = painterResource(R.drawable.default_avatar),
                        fallback = painterResource(R.drawable.default_avatar),
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = profileState.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    ProfileInfoItem(
                        StringResource(R.string.username_colon).asString(),
                        profileState.username
                    )
                    ProfileInfoItem(
                        StringResource(R.string.email_colon).asString(),
                        profileState.email
                    )
                    ProfileInfoItem(
                        StringResource(R.string.participate_day_colon).asString(),
                        profileState.createdAt
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))

                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Button(
                            onClick = {
                                profileViewModel.showUpdateProfileSheet()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !profileState.isUpdating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 3.dp
                            )
                        ) {
                            if (profileState.isUpdating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "update profile",
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = StringResource(R.string.update_profile)
                                        .asString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        Button(
                            onClick = { profileViewModel.showChangePasswordSheet() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !profileState.isChanging,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 3.dp
                            )
                        ) {
                            if (profileState.isChanging) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "change password",
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = StringResource(R.string.change_password).asString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        Button(
                            onClick = { profileViewModel.showLogoutConfirmDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp),
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 3.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "logout",
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = StringResource(R.string.log_out).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    when (profileState.sheetState) {
        ProfileViewModel.ProfileSheetState.Dismiss -> {}
        ProfileViewModel.ProfileSheetState.UpdateProfile -> {
            ModalBottomSheet(
                sheetState = updateSheetState,
                onDismissRequest = {
                    uiScope.launch { updateSheetState.hide() }.invokeOnCompletion {
                        if (!updateSheetState.isVisible) profileViewModel.dismissSheet()
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedAvatar(
                        currentAvatar = updateProfileState.avatar,
                        onBadgeClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = updateProfileState.fullName,
                        onValueChange = { profileViewModel.onUpdateFullNameChange(it) },
                        label = { Text(StringResource(R.string.fullname).asString()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = RealtimeGreen,
                            cursorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = updateProfileState.email,
                        onValueChange = { profileViewModel.onUpdateEmailChange(it) },
                        label = { Text(StringResource(R.string.email).asString()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = RealtimeGreen,
                            cursorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        OutlinedButton(
                            onClick = {
                                uiScope.launch { updateSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!updateSheetState.isVisible) profileViewModel.dismissSheet()
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
                            onClick = {
                                profileViewModel.showUpdateProfileConfirmDialog()
                            },
                            enabled = updateProfileState.isUpdateEnable,
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
                                text = StringResource(R.string.save).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        ProfileViewModel.ProfileSheetState.ChangePassword -> {
            ModalBottomSheet(
                sheetState = changePasswordSheetState,
                onDismissRequest = {
                    uiScope.launch { changePasswordSheetState.hide() }.invokeOnCompletion {
                        if (!changePasswordSheetState.isVisible) profileViewModel.dismissSheet()
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = changePasswordState.oldPassword,
                        onValueChange = { profileViewModel.onOldPasswordChange(it) },
                        label = { Text(StringResource(R.string.old_password).asString()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (oldPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image =
                                if (oldPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (oldPasswordVisible) "hide" else "show"

                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = RealtimeGreen,
                            cursorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = changePasswordState.newPassword,
                        onValueChange = { profileViewModel.onNewPasswordChange(it) },
                        label = { Text(StringResource(R.string.new_password).asString()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image =
                                if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (newPasswordVisible) "hide" else "show"

                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = RealtimeGreen,
                            cursorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = changePasswordState.confirmNewPassword,
                        onValueChange = { profileViewModel.onConfirmNewPasswordChange(it) },
                        label = {
                            Text(
                                StringResource(R.string.confirm_new_password).asString()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmNewPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image =
                                if (confirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (confirmNewPasswordVisible) "hide" else "show"

                            IconButton(onClick = {
                                confirmNewPasswordVisible = !confirmNewPasswordVisible
                            }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = RealtimeGreen,
                            cursorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        OutlinedButton(
                            onClick = {
                                uiScope.launch { changePasswordSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!changePasswordSheetState.isVisible) {
                                            profileViewModel.dismissSheet()
                                        }
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
                            onClick = {
                                profileViewModel.showChangePasswordConfirmDialog()
                            },
                            enabled = changePasswordState.isChangePasswordEnable,
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
                                text = StringResource(R.string.save).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    when (val dialogState = profileState.dialogState) {
        is ProfileViewModel.ProfileDialogState.UpdateProfileConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.notification).asString(),
                message = StringResource(R.string.update_profile_confirm_warning).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onDismiss = { profileViewModel.dismissDialog() },
                onConfirm = {
                    with(profileViewModel) {
                        dismissDialog()
                        uiScope.launch { updateSheetState.hide() }.invokeOnCompletion {
                            dismissSheet()
                            updateProfile()
                        }
                    }
                }
            )
        }

        is ProfileViewModel.ProfileDialogState.UpdateProfileSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.update_profile_success_notification)
                    .asString(),
                isSuccess = true,
                onDismiss = { profileViewModel.dismissDialog() }
            )
        }

        is ProfileViewModel.ProfileDialogState.ChangePasswordConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.notification).asString(),
                message = StringResource(R.string.change_password_confirm_warning)
                    .asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.confirm).asString(),
                isDangerConfirm = true,
                onDismiss = { profileViewModel.dismissDialog() },
                onConfirm = {
                    with(profileViewModel) {
                        dismissDialog()
                        uiScope.launch { changePasswordSheetState.hide() }.invokeOnCompletion {
                            dismissSheet()
                            changePassword()
                        }
                    }
                }
            )
        }

        is ProfileViewModel.ProfileDialogState.ChangePasswordSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.change_password_success_notification)
                    .asString(),
                isSuccess = true,
                onDismiss = {
                    with(profileViewModel) {
                        dismissDialog()
                        logout(showLogoutSuccessDialog = false)
                    }
                }
            )
        }

        is ProfileViewModel.ProfileDialogState.LogoutConfirm -> {
            ConfirmationDialog(
                title = StringResource(R.string.warning).asString(),
                message = StringResource(R.string.logout_confirm_warning).asString(),
                dismissText = StringResource(R.string.cancel).asString(),
                confirmText = StringResource(R.string.log_out).asString(),
                isDangerConfirm = true,
                onDismiss = { profileViewModel.dismissDialog() },
                onConfirm = {
                    with(profileViewModel) {
                        dismissDialog()
                        logout(showLogoutSuccessDialog = true)
                    }
                }
            )
        }

        is ProfileViewModel.ProfileDialogState.LogoutSuccess -> {
            NotificationDialog(
                title = StringResource(R.string.success).asString(),
                message = StringResource(R.string.logged_out_successfully).asString(),
                isSuccess = true,
                onDismiss = {
                    profileViewModel.dismissDialog()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            // "group" tab state is saved by BottomNavBar (saveState=true) and not present
                            // in backstack at logout time, so popUpTo(0) won't destroy it — clear manually
                            // if not, when logout again in the same application lifecycle
                            // restoreState will use old instance of GroupViewModel, getGroups in init will not be called
                            navController.clearBackStack(Screen.Groups.route)

                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        is ProfileViewModel.ProfileDialogState.Failure -> {
            NotificationDialog(
                title = StringResource(R.string.error).asString(),
                message = dialogState.message.asString(),
                isSuccess = false,
                onDismiss = { profileViewModel.dismissDialog() }
            )
        }

        ProfileViewModel.ProfileDialogState.Dismiss -> {}
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreen() {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            AsyncImage(
                model = R.drawable.default_avatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Vũ Quốc An",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            ProfileInfoItem("Tên Đăng Nhập:", "An_K4")
            Spacer(modifier = Modifier.height(10.dp))
            ProfileInfoItem("Email:", "ank4@gmail.com")
            Spacer(modifier = Modifier.height(10.dp))
            ProfileInfoItem("Ngày tham gia:", "31/10/2004")
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = LightBlue,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "update profile",
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = "Cập Nhật Thông Tin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "change password",
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = "Đổi Mật Khẩu",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = Red,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "logout",
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = "Đăng Xuất",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}