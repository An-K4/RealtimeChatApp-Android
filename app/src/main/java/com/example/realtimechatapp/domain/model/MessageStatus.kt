package com.example.realtimechatapp.domain.model

/**
 * Enum representing the status of a message in the chat system.
 * Used for optimistic UI and tracking message delivery/read state.
 */
enum class MessageStatus {
    /** Message is being sent (optimistic UI state) */
    SENDING,
    
    /** Message has been successfully sent to server */
    SENT,
    
    /** Message has been seen/read by recipient */
    SEEN,
    
    /** Message failed to send */
    ERROR
}
