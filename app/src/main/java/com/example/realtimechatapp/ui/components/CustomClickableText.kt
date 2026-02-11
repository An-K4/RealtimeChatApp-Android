package com.example.realtimechatapp.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.realtimechatapp.ui.theme.RealtimeGreen

@Composable
fun CustomClickableText(
    startText: String?,
    clickableText: String,
    clickableTextTag: String,
    clickableTextAnnotation: String?,
    endText: String,
    onTextClicked: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append(startText)

        pushStringAnnotation(tag = clickableTextTag, annotation = clickableTextAnnotation?:clickableTextTag)
        withStyle(
            style = SpanStyle(
                color = RealtimeGreen, // Chọn màu nổi bật
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        ) {
            append(clickableText)
        }

        append(endText)
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(clickableTextTag, start = offset, end = offset)
                .firstOrNull()?.let {
                    onTextClicked()
                }
        }
    )
}