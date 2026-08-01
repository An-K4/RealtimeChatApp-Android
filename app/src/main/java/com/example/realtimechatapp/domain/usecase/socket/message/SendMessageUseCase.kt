package com.example.realtimechatapp.domain.usecase.socket.message

import android.net.Uri
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.model.MessageStatus
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val socketRepository: SocketRepository,
    private val mediaRepository: MediaRepository,
    private val messageRepository: MessageRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    suspend operator fun invoke(
        content: String,
        receiverId: String,
        replyTo: String? = null,
        selectedImageUri: Uri? = null
    ): Result<String> {  // Trả về temp message ID cho UI
        return try {
            // Step 1: Insert optimistic message ngay lập tức (SENDING status)
            val tempIdResult = messageRepository.insertOptimisticMessage(
                receiverId = receiverId,
                content = content,
                attachment = selectedImageUri?.toString(),
                replyTo = replyTo
            )
            val tempId = tempIdResult.getOrElse {
                Timber.e(it, "Không thể insert optimistic message")
                return Result.failure(it)
            }
            Timber.d("Inserted optimistic message with tempId: $tempId")

            // Step 2: Upload attachment nếu có (background, không block gửi message)
            var fileUrl: String? = null
            if (selectedImageUri != null) {
                try {
                    val uploadResult = mediaRepository.upload(selectedImageUri)
                    fileUrl = uploadResult.getOrThrow()
                    Timber.d("Upload ảnh thành công: $fileUrl")

                    // Update attachments trong DB ngay sau khi upload xong
                    messageRepository.updateMessageAttachments(tempId, fileUrl)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Timber.e(e, "Upload ảnh thất bại, message vẫn gửi")
                    // KHÔNG throw - message vẫn gửi kể cả attachment fail
                }
            }

            // Step 3: Tạo SendMessageParam với fileUrl (nếu có)
            val sendMessageParam = SendMessageParam(
                content = content,
                receiverId = receiverId,
                replyTo = replyTo,
                fileUrl = fileUrl
            )

            // Step 4: Gửi qua socket với ACK callback
            socketRepository.sendMessageWithAck(
                message = sendMessageParam,
                onAck = { success, realMessageId ->
                    // Callback chạy trên background thread, cần launch coroutine để gọi suspend functions
                    applicationScope.launch {
                        try {
                            if (success && realMessageId != null) {
                                // Success: Replace temp ID với real ID + update status SENT
                                messageRepository.replaceMessageId(tempId, realMessageId)
                                messageRepository.updateMessageStatus(realMessageId, MessageStatus.SENT)
                                Timber.d("Message sent successfully, replaced $tempId -> $realMessageId")
                            } else {
                                // Failure: Đánh dấu ERROR
                                messageRepository.updateMessageStatus(tempId, MessageStatus.ERROR)
                                Timber.w("Message ACK failed for tempId: $tempId")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Lỗi trong ACK callback cho tempId: $tempId")
                        }
                    }
                }
            )

            Timber.d("Đã gọi tin nhắn qua socket")
            Result.success(tempId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Lỗi trong quá trình gửi tin nhắn")
            Result.failure(e)
        }
    }
}