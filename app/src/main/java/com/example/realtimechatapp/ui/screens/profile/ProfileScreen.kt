package com.example.realtimechatapp.ui.screens.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
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
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.AvatarPicker
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
    var dialogState by remember { mutableStateOf<ProfileViewModel.ProfileEvent?>(null) }
    var showUpdateSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    val updateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val changePasswordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                dialogState = event
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
                        UiText.StringResource(R.string.username_colon).asString(),
                        profileState.username
                    )
                    ProfileInfoItem(
                        UiText.StringResource(R.string.email_colon).asString(),
                        profileState.email
                    )
                    ProfileInfoItem(
                        UiText.StringResource(R.string.participate_day_colon).asString(),
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
                                profileViewModel.initUpdateSheet()
                                showUpdateSheet = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !updateProfileState.isUpdating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 3.dp
                            )
                        ) {
                            if (updateProfileState.isUpdating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "update profile",
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = UiText.StringResource(R.string.update_profile)
                                        .asString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        Button(
                            onClick = { showChangePasswordSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 3.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "change password",
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = UiText.StringResource(R.string.change_password).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
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
                                text = UiText.StringResource(R.string.log_out).asString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showUpdateSheet) {
        ModalBottomSheet(
            sheetState = updateSheetState,
            onDismissRequest = {
                uiScope.launch { updateSheetState.hide() }.invokeOnCompletion {
                    if (!updateSheetState.isVisible) showUpdateSheet = false
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
                AvatarPicker(
                    currentAvatar = updateProfileState.avatar,
                    onAvatarPickerClick = {
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
                    label = { Text(UiText.StringResource(R.string.fullname).asString()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealtimeGreen,
                        cursorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = updateProfileState.email,
                    onValueChange = { profileViewModel.onUpdateEmailChange(it) },
                    label = { Text(UiText.StringResource(R.string.email).asString()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
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
                                    if (!updateSheetState.isVisible) showUpdateSheet =
                                        false
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
                            uiScope.launch { updateSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!updateSheetState.isVisible) showUpdateSheet =
                                        false
                                }
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
                            text = UiText.StringResource(R.string.save).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }

    if (showChangePasswordSheet) {
        ModalBottomSheet(
            sheetState = changePasswordSheetState,
            onDismissRequest = {
                uiScope.launch { changePasswordSheetState.hide() }.invokeOnCompletion {
                    if (!changePasswordSheetState.isVisible) showChangePasswordSheet =
                        false
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
                    label = { Text(UiText.StringResource(R.string.old_password).asString()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RealtimeGreen,
                        cursorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = changePasswordState.newPassword,
                    onValueChange = { profileViewModel.onNewPasswordChange(it) },
                    label = { Text(UiText.StringResource(R.string.new_password).asString()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
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
                            UiText.StringResource(R.string.confirm_new_password).asString()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
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
                                        showChangePasswordSheet = false
                                    }
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
                            uiScope.launch { changePasswordSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!changePasswordSheetState.isVisible) {
                                        showChangePasswordSheet = false
                                    }
                                }
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
                            text = UiText.StringResource(R.string.save).asString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }

    when (dialogState) {
        is ProfileViewModel.ProfileEvent.UpdateProfileConfirm -> {
            ConfirmationDialog(
                title = UiText.StringResource(R.string.notification).asString(),
                message = UiText.StringResource(R.string.update_profile_confirm_warning).asString(),
                dismissText = UiText.StringResource(R.string.cancel).asString(),
                confirmText = UiText.StringResource(R.string.confirm).asString(),
                isDangerConfirm = false,
                onDismiss = { dialogState = null },
                onConfirm = {
                    dialogState = null
                    profileViewModel.updateProfile()
                }
            )
        }

        is ProfileViewModel.ProfileEvent.UpdateProfileSuccess -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.success).asString(),
                message = UiText.StringResource(R.string.update_profile_success_notification)
                    .asString(),
                isSuccess = true,
                onDismiss = { dialogState = null }
            )
        }

        is ProfileViewModel.ProfileEvent.ChangePasswordConfirm -> {
            ConfirmationDialog(
                title = UiText.StringResource(R.string.notification).asString(),
                message = UiText.StringResource(R.string.change_password_confirm_warning)
                    .asString(),
                dismissText = UiText.StringResource(R.string.cancel).asString(),
                confirmText = UiText.StringResource(R.string.confirm).asString(),
                isDangerConfirm = true,
                onDismiss = { dialogState = null },
                onConfirm = {
                    dialogState = null
                    profileViewModel.changePassword()
                }
            )
        }

        is ProfileViewModel.ProfileEvent.ChangePasswordSuccess -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.success).asString(),
                message = UiText.StringResource(R.string.change_password_success_notification)
                    .asString(),
                isSuccess = true,
                onDismiss = {
                    dialogState = null
                    profileViewModel.logout(false)
                }
            )
        }

        is ProfileViewModel.ProfileEvent.LogoutConfirm -> {
            ConfirmationDialog(
                title = UiText.StringResource(R.string.warning).asString(),
                message = UiText.StringResource(R.string.logout_confirm_warning).asString(),
                dismissText = UiText.StringResource(R.string.cancel).asString(),
                confirmText = UiText.StringResource(R.string.log_out).asString(),
                isDangerConfirm = true,
                onDismiss = { dialogState = null },
                onConfirm = {
                    dialogState = null
                    profileViewModel.logout(true)
                }
            )
        }

        is ProfileViewModel.ProfileEvent.LogoutSuccess -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.success).asString(),
                message = UiText.StringResource(R.string.logged_out_successfully).asString(),
                isSuccess = true,
                onDismiss = {
                    dialogState = null
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

        is ProfileViewModel.ProfileEvent.NavigateToLogin -> {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

        is ProfileViewModel.ProfileEvent.Failure -> {
            NotificationDialog(
                title = UiText.StringResource(R.string.error).asString(),
                message = (dialogState as ProfileViewModel.ProfileEvent.Failure).message.asString(),
                isSuccess = false,
                onDismiss = { dialogState = null }
            )
        }

        else -> {}
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