package com.example.realtimechatapp.domain.usecase.socket.group

import android.net.Uri
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.model.MessageStatus
import com.example.realtimechatapp.domain.model.SendGroupMessageParam
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SendGroupMessageUseCase @Inject constructor(
    private val socketRepository: SocketRepository,
    private val mediaRepository: MediaRepository,
    private val groupRepository: GroupRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    suspend operator fun invoke(
        groupId: String,
        content: String,
        replyTo: String? = null,
        selectedImageUri: Uri? = null
    ): Result<String> {
        return try {
            // Step 1: Insert optimistic message with SENDING status
            val tempIdResult = groupRepository.insertOptimisticGroupMessage(
                groupId = groupId,
                content = content,
                attachment = selectedImageUri?.toString(),
                replyTo = replyTo
            )
            val tempId = tempIdResult.getOrElse { return Result.failure(it) }
            
            // Step 2: Upload attachment in background (doesn't block send)
            var fileUrl: String? = null
            if (selectedImageUri != null) {
                try {
                    val uploadResult = mediaRepository.upload(selectedImageUri)
                    fileUrl = uploadResult.getOrThrow()
                    groupRepository.updateMessageAttachments(tempId, fileUrl)
                    Timber.d("Upload ảnh nhóm thành công: $fileUrl")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Timber.e(e, "Upload ảnh nhóm thất bại")
                    // Don't throw - message still sent even if attachment fails
                }
            }
            
            // Step 3: Create SendGroupMessageParam with fileUrl
            val sendGroupMessageParam = SendGroupMessageParam(
                groupId = groupId,
                content = content,
                replyTo = replyTo,
                fileUrl = fileUrl
            )
            
            // Step 4: Send via socket with ACK callback
            socketRepository.sendGroupMessageWithAck(
                message = sendGroupMessageParam,
                onAck = { success, realMessageId ->
                    // Launch coroutine để gọi suspend functions
                    applicationScope.launch {
                        try {
                            if (success && realMessageId != null) {
                                groupRepository.replaceMessageId(tempId, realMessageId)
                                groupRepository.updateMessageStatus(realMessageId, MessageStatus.SENT)
                                Timber.d("sendGroupMessageWithAck success: $realMessageId")
                            } else {
                                groupRepository.updateMessageStatus(tempId, MessageStatus.ERROR)
                                Timber.w("sendGroupMessageWithAck failed")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error in sendGroupMessageWithAck callback")
                        }
                    }
                }
            )
            
            Result.success(tempId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to send group message")
            Result.failure(e)
        }
    }
}