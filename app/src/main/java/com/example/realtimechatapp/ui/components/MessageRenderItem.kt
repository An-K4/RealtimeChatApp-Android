package com.example.realtimechatapp.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.realtimechatapp.ui.theme.RealtimeGreen

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MessageRenderItem(
    message: String,
    time: String,
    isSeen: Boolean,
    fromCurrentUser: Boolean
){
    val currentUserShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )

    val otherUserShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )

    Row(
        horizontalArrangement = if (fromCurrentUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .fillMaxWidth(0.75f)
            .wrapContentWidth(
                align = if (fromCurrentUser) Alignment.End else Alignment.Start
            )
            .background(
                color = if (fromCurrentUser) RealtimeGreen else Color.White,
                shape = if (fromCurrentUser) currentUserShape else otherUserShape
            )
        ){
            Column(
                horizontalAlignment = if (fromCurrentUser) Alignment.End else Alignment.Start,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = if (fromCurrentUser) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (fromCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        time,
                        fontSize = 10.sp,
                        color = if (fromCurrentUser) Color.White else Color.Gray,
                        modifier = Modifier.padding(end = 3.dp))
                    if (fromCurrentUser){
                        Icon(
                            imageVector = if (isSeen) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = "status",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageRenderItem(){
    val currentUserShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 0.dp
    )

    val otherUserShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 0.dp
    )

    Box(modifier = Modifier
        .widthIn(40.dp)
        .background(
            color = RealtimeGreen,
            shape = currentUserShape
        )
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = "Xin chào Vũ Quốc An!",
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "10:37",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.padding(end = 3.dp))
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "status",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}