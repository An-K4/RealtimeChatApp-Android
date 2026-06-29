package com.example.realtimechatapp.ui.screens.groups.crud

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.ui.components.ContactListItem
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun MemberManagementScreen(
    navController: NavController,
    memberManagementViewModel: MemberManagementViewModel = hiltViewModel()
) {

}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MemberManagementScreen() {
    RealtimeChatAppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add"
                )

                Text(UiText.StringResource(R.string.add_member_to_group).asString())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: The member list may not be complete.",
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    ContactListItem(
                        avatar = "",
                        name = "Vũ Quốc An",
                        additionalInfo = "Owner",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "Peter Parker - Spider Man",
                        additionalInfo = "Admin",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "Tony Stark - Iron Man",
                        additionalInfo = "Admin",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC 1",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC 2",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ContactListItem(
                        avatar = "",
                        name = "NPC3",
                        additionalInfo = "Member",
                        onItemClicked = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}