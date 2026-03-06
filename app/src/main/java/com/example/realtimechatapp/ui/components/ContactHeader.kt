package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R

@Composable
fun ContactHeader(
    avatarContactPreview: String?,
    contactName: String,
    contactAdditionalInfo: String,
    onVideoCallClick: () -> Unit,
    onVoiceCallClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        AsyncImage(
            model = avatarContactPreview?:R.drawable.default_avatar,
            contentDescription = "avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, color = Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contactName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = contactAdditionalInfo,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = { onVideoCallClick() }) {
            Icon(
                imageVector = Icons.Outlined.Videocam,
                contentDescription = "video call"
            )
        }

        IconButton(onClick = { onVoiceCallClick() }) {
            Icon(
                imageVector = Icons.Outlined.Call,
                contentDescription = "voice call"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactHeader(){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        AsyncImage(
            model = R.drawable.default_avatar,
            contentDescription = "avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, color = Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Vũ Quốc An",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Đang hoạt động",
                fontSize = 12.sp
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Videocam,
                contentDescription = "video call"
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Call,
                contentDescription = "voice call"
            )
        }
    }
}