package com.example.realtimechatapp.ui.screens.auth

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.ui.components.AvatarPicker
import com.example.realtimechatapp.ui.components.CustomClickableText
import com.example.realtimechatapp.ui.components.NotificationDialog
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val signupState by authViewModel.signupState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<AuthViewModel.AuthEvent?>(null) }
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                authViewModel.onSignupAvatarChange(uri)
            }
        }
    )

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            authViewModel.authEvent.collect { event ->
                when (event) {
                    is AuthViewModel.AuthEvent.AuthSuccess -> {
                        dialogState = event
                    }

                    is AuthViewModel.AuthEvent.Failure -> {
                        dialogState = event
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AvatarPicker(
            currentAvatar = signupState.avatar,
            onAvatarPickerClick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

        Spacer(modifier = Modifier.size(20.dp))

        OutlinedTextField(
            value = signupState.username,
            onValueChange = { authViewModel.onSignupUsernameChange(it) },
            label = { Text(stringResource(R.string.username)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = signupState.password,
            onValueChange = { authViewModel.onSignupPasswordChange(it) },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = signupState.passwordRetype,
            onValueChange = { authViewModel.onSignupPasswordRetypeChange(it) },
            label = { Text(stringResource(R.string.password_retype)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = signupState.fullName,
            onValueChange = { authViewModel.onSignupFullNameChange(it) },
            label = { Text(stringResource(R.string.fullname)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = signupState.email,
            onValueChange = { authViewModel.onSignupEmailChange(it) },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.size(20.dp))

        Button(
            onClick = { authViewModel.signup() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            enabled = !signupState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (signupState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.sign_up))
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        CustomClickableText(
            stringResource(R.string.hint_old_account),
            stringResource(R.string.log_in),
            "login",
            "",
            "",
            onTextClicked = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }
            }
        )
    }

    if (dialogState is AuthViewModel.AuthEvent.AuthSuccess) {
        NotificationDialog(
            title = stringResource(R.string.success),
            message = stringResource(R.string.log_in_success_notification),
            isSuccess = true,
            onDismiss = {
                dialogState = null
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }
            }
        )
    }

    if (dialogState is AuthViewModel.AuthEvent.Failure) {
        val msg = (dialogState as AuthViewModel.AuthEvent.Failure).message
        NotificationDialog(
            title = stringResource(R.string.login_error),
            message = msg,
            isSuccess = false,
            onDismiss = { dialogState = null }
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignupUI() {
    RealtimeChatAppTheme {
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.size(20.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
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
}