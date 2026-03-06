package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.realtimechatapp.ui.theme.RealtimeGreen
import com.example.realtimechatapp.ui.theme.RealtimeRed

@Composable
fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSendClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { onMessageTextChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Soạn tin nhắn...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = { onCameraClick() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "camera",
                        tint = RealtimeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = { onGalleryClick() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = "gallery",
                        tint = RealtimeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(RealtimeGreen, CircleShape)
                .clickable{ onSendClick() }
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Default.Send,
                contentDescription = "send",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageInput(){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Soạn tin nhắn...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "camera",
                        tint = RealtimeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = "gallery",
                        tint = RealtimeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(RealtimeGreen.copy(0.85f), CircleShape)
                .clickable{}
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Default.Send,
                contentDescription = "send",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
        }
    }
}