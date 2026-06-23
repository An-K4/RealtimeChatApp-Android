package com.example.realtimechatapp.ui.screens.messages

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.ActionItem
import com.example.realtimechatapp.ui.components.ToggleSettingItem
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun MessageActionScreen(
    navController: NavController,
    // messageActionViewModel: MessageActionViewModel = hiltViewModel()
) {

}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MessageActionScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = null,
                contentDescription = "Avatar",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar),
                fallback = painterResource(R.drawable.default_avatar),
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Vũ Quốc An",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "an@vu.demo",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.height(2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    ToggleSettingItem(
                        icon = Icons.Default.NotificationsOff,
                        title = UiText.StringResource(R.string.mute_notification).asString(),
                        isChecked = false,
                        onCheckedChange = { }
                    )
                }

                item {
                    ActionItem(
                        icon = Icons.Default.Image,
                        title = UiText.StringResource(R.string.media_files).asString(),
                        onClick = { },
                    )
                }

                item {
                    ActionItem(
                        icon = Icons.Default.Groups,
                        title = UiText.StringResource(R.string.add_to_group).asString(),
                        onClick = { },
                    )
                }

                item {
                    ActionItem(
                        icon = Icons.Default.Block,
                        title = UiText.StringResource(R.string.block).asString(),
                        isDangerAction = true,
                        onClick = { },
                        trailingContent = { Spacer(modifier = Modifier.weight(1f)) }
                    )
                }
            }
        }
    }
}