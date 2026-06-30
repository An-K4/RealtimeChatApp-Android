package com.example.realtimechatapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme

@Composable
fun CustomClickableText(
    startText: String?,
    clickableText: String,
    clickableTextTag: String,
    clickableTextAnnotation: String?,
    endText: String,
    textSize: TextUnit = 16.sp,
    onTextClicked: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = textSize
            )
        ) {
            append("$startText ")
        }

        pushStringAnnotation(
            tag = clickableTextTag,
            annotation = clickableTextAnnotation ?: clickableTextTag
        )
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary, // bright color
                fontWeight = FontWeight.Bold,
                fontSize = textSize
            )
        ) {
            append(clickableText)
        }

        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = textSize
            )
        ) {
            append(endText)
        }
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

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CustomClickableText() {
    RealtimeChatAppTheme {
        val annotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            ) {
                append("Bạn chưa có tài khoản? ")
            }

            pushStringAnnotation(tag = "đăng ký", annotation = "đăng ký")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary, // bright color
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            ) {
                append("Đăng ký")
            }

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            ) {
                append("")
            }
        }

        ClickableText(
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations("đăng ký", start = offset, end = offset)
                    .firstOrNull()?.let {}
            }
        )
    }
}