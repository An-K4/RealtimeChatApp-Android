package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R

@Composable
fun AvatarPicker(
    currentAvatar: Any?,
    onAvatarPickerClick: () -> Unit
) {
    val avatarToDisplay = currentAvatar?:R.drawable.default_avatar

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(150.dp)
    ) {
        AsyncImage(
            model = avatarToDisplay,
            contentDescription = "Avatar",
            modifier = Modifier.matchParentSize()
                .clip(CircleShape)
                .border(4.dp, Color.Gray, CircleShape)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(50.dp, 50.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.Gray, CircleShape)
                .clickable{ onAvatarPickerClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarPickerUI() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(150.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.default_avatar),
            contentDescription = "Avatar",
            modifier = Modifier.matchParentSize()
                .clip(CircleShape)
                .border(4.dp, Color.Gray, CircleShape)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(50.dp, 50.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.Gray, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}