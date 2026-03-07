package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.realtimechatapp.R

@Composable
fun BeginScreen(
    isGroup: Boolean,
    inDetailScreen: Boolean
){
    val suggestText1 = if (inDetailScreen) "Chưa có tin nhắn nào." else "Chào mừng đến với Clover Chatty"

    val suggestText2 = if (isGroup){
        if (inDetailScreen){
            "Nhập tin nhắn để bắt đầu!"
        } else {
            "Tạo nhóm và chia sẻ những câu chuyện thú vị cùng nhau!"
        }
    } else {
        if (inDetailScreen){
            "Hãy thử làm quen bằng một lời chào!"
        } else {
            "Hãy tìm 1 người bạn và bắt đầu trò chuyện nào!"
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo"
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = suggestText1,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = suggestText2,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BeginScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.default_avatar),
                contentDescription = "Logo"
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chào mừng đến với Clover Chatty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hãy tìm 1 người bạn và bắt đầu trò chuyện nào!",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}