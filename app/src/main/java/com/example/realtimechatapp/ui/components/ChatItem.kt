package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.toHourMinute
import com.example.realtimechatapp.domain.model.LastMessage
import com.example.realtimechatapp.domain.model.UserContact

@Composable
fun ChatItem(
    isGroup: Boolean,
    avatar: String?,
    name: String,
    unreadCount: Int,
    lastMessage: LastMessage
) {
    val previewLastMessage = if (lastMessage.isMine) {
        "Bạn: ${lastMessage.content}"
    } else {
        if (isGroup){
            "${lastMessage.senderName}: ${lastMessage.content}"
        } else {
            lastMessage.content
        }

    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    )
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            AsyncImage(
                model = avatar ?: R.drawable.logo,
                contentDescription = "Small Preview Avatar",
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = 20.dp, y = 20.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
                    .background(Color.Red, CircleShape)
            ) {}
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = previewLastMessage,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 5.dp)
        ) {
            Text(
                text = lastMessage.createdAt.toHourMinute(),
                fontSize = 14.sp
            )
            if (unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(unreadCount.toString())
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(50.dp)
        ) {
            AsyncImage(
                model = R.drawable.logo,
                contentDescription = "Small Preview Avatar",
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = 17.dp, y = 17.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
                    .background(Color.Red, CircleShape)
            ) {}
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(5.dp)
                .weight(1f)
        ) {
            Text(
                text = "Vũ Quốc An",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Đây là bản xem trước.",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(0.35f)
        ) {
            Text(
                text = "10:37",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text("1")
                    }
                }
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
            }
        }
    }
}