package com.example.realtimechatapp.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText() // dynamic string from server

    // strings.xml
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any // pass parameters by varargs %s
    ) : UiText()

    // for viewmodel
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    // for compose
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }
}