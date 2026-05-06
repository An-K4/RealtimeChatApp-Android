package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ToggleSettingItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
){
    Box(modifier = Modifier.fillMaxWidth()){
        MoreScreenItem(
            icon = icon,
            title = title,
            onClick = {},
            trailingContent = {
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ToggleSettingItem(){
    Box(modifier = Modifier.fillMaxWidth()){
        MoreScreenItem(
            icon = Icons.Default.DarkMode,
            title = "Chế độ tối",
            onClick = {},
            trailingContent = {
                Switch(
                    checked = false,
                    onCheckedChange = {}
                )
            }
        )
    }
}