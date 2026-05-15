package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun AvatarPicker(
    currentAvatar: Any?,
    onAvatarPickerClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(150.dp)
    ) {
        AsyncImage(
            model = currentAvatar,
            contentDescription = "Avatar",
            placeholder = painterResource(R.drawable.default_avatar),
            error = painterResource(R.drawable.default_avatar),
            fallback = painterResource(R.drawable.default_avatar),
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentScale = ContentScale.Crop
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(50.dp, 50.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clickable { onAvatarPickerClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AvatarPickerUI() {
    RealtimeChatAppTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(150.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.default_avatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(50.dp, 50.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change Avatar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}