package com.example.realtimechatapp.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.ui.navigation.Screen
import com.example.realtimechatapp.ui.theme.Chewy
import com.example.realtimechatapp.ui.theme.RealtimeGreen

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: AuthViewModel = hiltViewModel()
) {
    val loadingState by loginViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loginViewModel.uiEvent.collect { event ->
            when(event){
                is AuthViewModel.UiEvent.LoginSuccess -> {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Login.route){ inclusive = true }
                    }
                }
                is AuthViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Chewy,
                fontStyle = FontStyle.Italic,
                color = RealtimeGreen
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = loginViewModel.username.value,
                onValueChange = { loginViewModel.onUsernameChange(it) },
                label = { Text("Tên đăng nhập") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = loginViewModel.password.value,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RealtimeGreen,
                    cursorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { loginViewModel.login() },
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
                if (loadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else Text(
                    text = "Đăng nhập",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ClickableSignupText(onSignupClicked = {
                Toast.makeText(context, "Đăng ký đã nhấn", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun ClickableSignupText(
    onSignupClicked: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append("Bạn chưa có tài khoản? ")

        pushStringAnnotation(tag = "signup", annotation = "signup")
        withStyle(
            style = SpanStyle(
                color = RealtimeGreen, // Chọn màu nổi bật
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        ) {
            append("Đăng ký")
        }
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations("signup", start = offset, end = offset)
                .firstOrNull()?.let {
                    onSignupClicked()
                }
        }
    )
}