package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun <T> DropDownSettingItem(
    icon: ImageVector,
    title: String,
    options: List<T>,
    selectedOption: T,
    displayText: (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var rowSize by remember { mutableStateOf(Size.Zero) }

    ActionItem(
        icon = icon,
        title = title,
        onClick = { },
        trailingContent = {
            Box(
                modifier = Modifier.clickable(
                    enabled = true,
                    onClick = { isExpanded = true }
                )
            ) {
                Row(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates -> rowSize = coordinates.size.toSize() }
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(corner = CornerSize(10.dp))
                        )
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayText(selectedOption),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "arrow drop down",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                val density = LocalDensity.current
                val dropdownWidth = with(density) { rowSize.width.toDp() }

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.width(dropdownWidth)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = displayText(option),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = {
                                onOptionSelected(option)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DropDownSettingItem() {
    RealtimeChatAppTheme {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionItem(
                icon = Icons.Default.Language,
                title = "Ngôn ngữ",
                onClick = {},
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(corner = CornerSize(10.dp))
                            )
                            .padding(vertical = 5.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiếng Việt",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "arrow drop down",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    }
}