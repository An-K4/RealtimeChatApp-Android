package com.example.realtimechatapp.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MessageRenderItem(
    senderAvatar: String?,
    senderName: String?,
    message: String,
    time: String,
    isSeen: Boolean,
    isGroup: Boolean,
    fromCurrentUser: Boolean
) {
    val currentUserShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )

    val otherUserShape = if (isGroup){
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 16.dp
        )
    }

    Row(
        verticalAlignment = Alignment.Top
    ) {
        if (isGroup && !fromCurrentUser){
            AsyncImage(
                model = senderAvatar,
                contentDescription = "sender avatar",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar),
                fallback = painterResource(R.drawable.default_avatar),
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, color = MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(5.dp))
        }

        Column {
            if (isGroup && !fromCurrentUser){
                Text(
                    text = senderName ?: UiText.StringResource(R.string.clover_chatty_user).asString(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Row(
                horizontalArrangement = if (fromCurrentUser) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .wrapContentWidth(
                            align = if (fromCurrentUser) Alignment.End else Alignment.Start
                        )
                        .background(
                            color = if (fromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                            shape = if (fromCurrentUser) currentUserShape else otherUserShape
                        )
                ) {
                    Column(
                        horizontalAlignment = if (fromCurrentUser) Alignment.End else Alignment.Start,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            color = if (fromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (fromCurrentUser) Arrangement.End else Arrangement.Start
                        ) {
                            Text(
                                time,
                                fontSize = 10.sp,
                                color = if (fromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 3.dp)
                            )
                            if (fromCurrentUser) {
                                Icon(
                                    imageVector = if (isSeen) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = "status",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF838383)
@Preview(showBackground = true, backgroundColor = 0xFF838383, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageRenderItem() {
    RealtimeChatAppTheme {
        val otherUserShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        Row(
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = R.drawable.default_avatar,
                contentDescription = "avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column {
                Text(
                    text = "Clover Chatty",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(bottom = 5.dp)
                )

                Box(
                    modifier = Modifier
                        .widthIn(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = otherUserShape
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Xin chào Vũ Quốc An!",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "10:37",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF838383)
@Preview(showBackground = true, backgroundColor = 0xFF838383, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageRenderItemMine() {
    RealtimeChatAppTheme {
        val currentUserShape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 0.dp
        )

        Row(
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = R.drawable.default_avatar,
                contentDescription = "avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column {
                Box(
                    modifier = Modifier
                        .widthIn(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = currentUserShape
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Xin chào Vũ Quốc An!",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "10:37",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 3.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "status",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}