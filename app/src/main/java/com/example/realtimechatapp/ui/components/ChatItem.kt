package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.domain.model.LastMessage
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun ChatItem(
    isGroup: Boolean,
    avatar: String?,
    name: String,
    unreadCount: Int,
    lastMessage: LastMessage?,
    isOnline: Boolean = false,
    isTyping: Boolean = false,
    onItemClicked: () -> Unit
) {
    val previewLastMessage = lastMessage?.let {
        if (lastMessage.isMine) {
            UiText.StringResource(R.string.you_with_arg, lastMessage.content).asString()
        } else {
            if (isGroup) {
                "${lastMessage.senderName}: ${lastMessage.content}"
            } else {
                lastMessage.content
            }
        }
    }
    val onlineColor =
        if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .clickable { onItemClicked() }
    )
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            AsyncImage(
                model = avatar ?: R.drawable.default_avatar,
                contentDescription = "small preview avatar",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar),
                fallback = painterResource(R.drawable.default_avatar),
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentScale = ContentScale.Crop
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = 22.dp, y = 22.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .background(onlineColor, CircleShape)
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
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isTyping) {
                Text(
                    text = if (isGroup) {
                        UiText.StringResource(R.string.someone_is_typing).asString()
                    } else {
                        UiText.StringResource(R.string.typing).asString()
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = previewLastMessage ?: UiText.StringResource(R.string.new_group).asString(),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 5.dp)
        ) {
            Text(
                text = lastMessage?.createdAt ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            if (unreadCount < 10) {
                                Text(unreadCount.toString())
                            } else {
                                Text("9+")
                            }
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatItem() {
    RealtimeChatAppTheme {
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
                    model = R.drawable.default_avatar,
                    contentDescription = "Small Preview Avatar",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = 17.dp, y = 17.dp)
                        .size(15.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
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
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Đây là bản xem trước.",
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
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
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(5.dp))
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Text("1")
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}