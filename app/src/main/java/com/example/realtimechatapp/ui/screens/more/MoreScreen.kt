package com.example.realtimechatapp.ui.screens.more

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.domain.repository.ThemeMode
import com.example.realtimechatapp.ui.components.DropDownSettingItem
import com.example.realtimechatapp.ui.components.ToggleSettingItem

@Composable
fun MoreScreen(
    navController: NavController,
    moreViewModel: MoreViewModel = hiltViewModel()
){
    val moreScreenState by moreViewModel.moreScreenState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            DropDownSettingItem(
                icon = Icons.Default.Language,
                title = UiText.StringResource(R.string.language).asString(),
                options = moreScreenState.supportedLanguages,
                selectedOption = moreScreenState.selectedLanguage,
                displayText = { it.displayName },
                onOptionSelected = {
                    moreViewModel.changeLanguage(it)
                }
            )
        }

        item {
            ToggleSettingItem(
                icon = Icons.Default.DarkMode,
                title = UiText.StringResource(R.string.dark_mode).asString(),
                isChecked = moreScreenState.isDarkTheme,
                onCheckedChange = {
                    moreViewModel.changeTheme(if (it) ThemeMode.DARK else ThemeMode.LIGHT)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview(){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            DropDownSettingItem(
                icon = Icons.Default.Language,
                title = "Ngôn Ngữ",
                options = listOf("Tiếng Việt", "English"),
                selectedOption = "Tiếng Việt",
                displayText = { it },
                onOptionSelected = {

                }
            )
        }

        item {
            ToggleSettingItem(
                icon = Icons.Default.DarkMode,
                title = "Chế Độ Tối",
                isChecked = false,
                onCheckedChange = {

                }
            )
        }
    }
}