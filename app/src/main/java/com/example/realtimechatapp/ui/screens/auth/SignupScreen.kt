package com.example.realtimechatapp.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.realtimechatapp.ui.components.AvatarPicker
import com.example.realtimechatapp.ui.components.CustomClickableText
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeGreen

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val loadingState by authViewModel.isLoading.collectAsState()
    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()
    val passwordRetype by authViewModel.passwordRetype.collectAsState()
    val fullName by authViewModel.fullName.collectAsState()
    val email by authViewModel.email.collectAsState()
    val avatar by authViewModel.avatar.collectAsState()
    var dialogState by remember { mutableStateOf<AuthViewModel.AuthEvent?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null){
                authViewModel.onAvatarChange(uri)
            }
        }
    )

    LaunchedEffect(Unit) {
        authViewModel.authEvent.collect { event ->
            when(event){
                is AuthViewModel.AuthEvent.Success -> {
                    dialogState = event
                }
                is AuthViewModel.AuthEvent.Failure -> {
                    dialogState = event
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AvatarPicker(
                currentAvatar = avatar,
                onAvatarPickerClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.size(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { authViewModel.onUsernameChange(it) },
                label = { Text("Tên đăng nhập") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = passwordRetype,
                onValueChange = { authViewModel.onPasswordRetypeChange(it) },
                label = { Text("Nhập lại mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { authViewModel.onFullNameChange(it) },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(20.dp))

            Button(
                onClick = { authViewModel.signup() },
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                enabled = !loadingState,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RealtimeGreen,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                if (loadingState){
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Đăng ký")
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            CustomClickableText(
                "Bạn đã có tài khoản? ",
                "Đăng nhập",
                "login",
                "",
                "",
                onTextClicked = {
                    navController.navigate(Screen.Login.route){
                        popUpTo(Screen.Signup.route){ inclusive = true }
                    }
                }
            )
        }

        if(dialogState is AuthViewModel.AuthEvent.Success){
            NotificationDialog(
                title = "Thành Công",
                message = "Đăng ký thành công, chuyển hướng về trang đăng nhập.",
                isSuccess = true,
                onDismiss = {
                    dialogState = null
                    navController.navigate(Screen.Login.route){
                        popUpTo(Screen.Signup.route){ inclusive = true }
                    }
                }
            )
        }

        if(dialogState is AuthViewModel.AuthEvent.Failure){
            val msg = (dialogState as AuthViewModel.AuthEvent.Failure).message
            NotificationDialog(
                title = "Lỗi Đăng Nhập",
                message = msg,
                isSuccess = false,
                onDismiss = { dialogState = null }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupUI(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AvatarPicker(null, onAvatarPickerClick = {})

            Spacer(modifier = Modifier.size(20.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Tên đăng nhập") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Nhập lại mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.size(20.dp))

            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RealtimeGreen,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text("Đăng ký")
            }

            Spacer(modifier = Modifier.size(16.dp))

            CustomClickableText(
                "Bạn đã có tài khoản? ",
                "Đăng nhập",
                "login",
                "",
                "",
                onTextClicked = {}
            )
        }
    }
}