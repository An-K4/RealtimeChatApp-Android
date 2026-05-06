package com.example.realtimechatapp.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.realtimechatapp.R

sealed class UiText {
    data class DynamicString(val value: String) : UiText() // dynamic string from server

    // strings.xml
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any? // pass parameters by varargs %s
    ) : UiText()

    // for viewmodel
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource ->{
                val processedArgs = args.map { arg ->
                    arg ?: context.getString(R.string.unknown)
                }.toTypedArray()

                context.getString(resId, *processedArgs)
            }
        }
    }

    // for compose
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val processedArgs = args.map { arg ->
                    arg ?: stringResource(R.string.unknown)
                }.toTypedArray()

                stringResource(resId, *processedArgs)
            }
        }
    }
}