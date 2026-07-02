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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.realtimechatapp.R

@Composable
fun BadgedAvatar(
    size: Dp = 150.dp,
    currentAvatar: Any?,
    icon: ImageVector = Icons.Default.Edit,
    onBadgeClick: () -> Unit
) {
    val badgeSize = size * 0.3f          // 45/150 = 0.3
    val badgeOffset = size * 0.333f      // 50/150 ≈ 0.333
    val outerBorder = size * 0.0267f     // 4/150
    val actionBorder = size * 0.0133f     // 2/150
    val iconSize = badgeSize * 0.5f      // 15/30 = 0.5

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
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
                .border(outerBorder, MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentScale = ContentScale.Crop
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(badgeOffset, -badgeOffset)
                .size(badgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(actionBorder, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clickable { onBadgeClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BadgedAvatar() {
    val size = 150.dp
    val badgeSize = size * 0.3f          // 45/150 = 0.3
    val badgeOffset = size * 0.333f      // 50/150 ≈ 0.333
    val outerBorder = size * 0.0267f     // 4/150
    val badgeBorder = size * 0.0133f     // 2/150
    val iconSize = badgeSize * 0.5f      // 15/30 = 0.5

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Image(
            painter = painterResource(R.drawable.default_avatar),
            contentDescription = "Avatar",
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(outerBorder, MaterialTheme.colorScheme.primaryContainer, CircleShape),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(badgeOffset, -badgeOffset)
                .size(badgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(badgeBorder, MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}