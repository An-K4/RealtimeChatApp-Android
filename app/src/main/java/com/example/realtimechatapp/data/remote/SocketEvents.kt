package com.example.realtimechatapp.data.remote

object SocketEvents {
    const val RECEIVE_MESSAGE = "receive-message"
    const val SEND_MESSAGE = "send-message"
    const val SEEN_MESSAGE = "seen-message"
    const val TYPING_START = "typing-start"
    const val TYPING_STOP = "typing-stop"
    const val NOTIFY_USER_ONLINE = "noti-online"
    const val NOTIFY_USER_OFFLINE = "noti-offline"
    const val NOTIFY_ONLINE_LIST = "noti-onlineList-toMe"
}