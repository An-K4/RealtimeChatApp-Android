package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.theme.Green
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import com.example.realtimechatapp.ui.theme.Red

@Composable
fun NotificationDialog(
    title: String,
    message: String,
    isSuccess: Boolean, // true = green, false = red
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(15.dp)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSuccess) Green else Red,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        UiText.StringResource(R.string.ok).asString(),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    dismissText: String,
    confirmText: String,
    isDangerConfirm: Boolean, // true = red, false = green
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (isDangerConfirm) Color.Red else RealtimeGreen
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(Color.Gray.copy(0.75f)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(horizontal = 5.dp)
                    ) {
                        Text(dismissText, color = Color.White)
                    }

                    Button(
                        onClick = { onConfirm() },
                        colors = ButtonDefaults.buttonColors(
                            if (isDangerConfirm) Color.Red else RealtimeGreen
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(5.dp)
                    ) {
                        Text(confirmText, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationDialog() {
    RealtimeChatAppTheme {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(15.dp)
                    ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Thành Công",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Đăng nhập thành công",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            UiText.StringResource(R.string.ok).asString(),
                            color = MaterialTheme.colorScheme.onError
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
fun ConfirmationDialog() {
    RealtimeChatAppTheme {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cảnh Báo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Bạn Chắc Chắn Muốn Đăng Xuất?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        thickness = 2.dp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(Color.Gray.copy(0.75f)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(horizontal = 5.dp)
                        ) {
                            Text("Hủy", color = Color.White)
                        }

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(Color.Red),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(5.dp)
                        ) {
                            Text("Xác Nhận", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}