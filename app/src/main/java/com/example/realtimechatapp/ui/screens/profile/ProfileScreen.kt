package com.example.realtimechatapp.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.ui.components.AvatarPicker
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.LightBlue
import com.example.realtimechatapp.ui.theme.LightRed
import com.example.realtimechatapp.ui.theme.Red

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileState by profileViewModel.profileState.collectAsState()
    var dialogState by remember { mutableStateOf<ProfileViewModel.ProfileEvent?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                profileViewModel.onAvatarChange(uri)
            }
        }
    )

    LaunchedEffect(Unit) {
        profileViewModel.getMe()
        profileViewModel.profileEvent.collect(){ event ->
            dialogState = event
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.matchParentSize()
            ) {
                AvatarPicker(
                    currentAvatar = profileState.avatar,
                    onAvatarPickerClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profileState.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Tên Đăng Nhập:", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Email:", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Ngày tham gia:", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(profileState.username, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(profileState.email, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(profileState.createdAt.formatToTime(false), fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 20.dp),
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
                        .padding(vertical = 5.dp, horizontal = 20.dp),
                    enabled = true,
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
                    onClick = { profileViewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp, horizontal = 20.dp),
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

                when(dialogState){
                    is ProfileViewModel.ProfileEvent.UpdateProfileSuccess -> {
                        NotificationDialog(
                            title = "Thành Công",
                            message = "Cập nhật thông tin thành công!",
                            isSuccess = true,
                            onDismiss = { dialogState = null }
                        )
                    }
                    is ProfileViewModel.ProfileEvent.ChangePasswordSuccess -> {
                        NotificationDialog(
                            title = "Thành Công",
                            message = "Đổi mật khẩu thành công!",
                            isSuccess = true,
                            onDismiss = { dialogState = null }
                        )
                    }
                    is ProfileViewModel.ProfileEvent.LogoutSuccess -> {
                        NotificationDialog(
                            title = "Thành Công",
                            message = "Đăng xuất thành công!",
                            isSuccess = true,
                            onDismiss = {
                                dialogState = null
                                navController.navigate(Screen.Login.route){
                                    popUpTo(0){
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    is ProfileViewModel.ProfileEvent.Failure -> {
                        NotificationDialog(
                            title = "Lỗi",
                            message = (dialogState as ProfileViewModel.ProfileEvent.Failure).message,
                            isSuccess = false,
                            onDismiss = { dialogState = null }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AvatarPicker(
            currentAvatar = null,
            onAvatarPickerClick = {}
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = "Vũ Quốc An",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text("Tên Đăng Nhập:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Email:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Ngày tham gia:", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(30.dp))
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text("An_K4", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("an@vu.demo", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("31/10/2025", fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 20.dp),
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
                .padding(vertical = 5.dp, horizontal = 20.dp),
            enabled = true,
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
                .padding(vertical = 5.dp, horizontal = 20.dp),
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