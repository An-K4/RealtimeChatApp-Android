package com.example.realtimechatapp.domain.usecase.socket.message

import android.net.Uri
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.SocketRepository
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val socketRepository: SocketRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        content: String,
        receiverId: String,
        replyTo: String? = null,
        selectedImageUri: Uri? = null
    ){
        var fileUrl: String? = null
        
        // 1. Nếu có ảnh được chọn, upload trước
        if (selectedImageUri != null) {
            try {
                val uploadResult = mediaRepository.upload(selectedImageUri)
                fileUrl = uploadResult.getOrThrow()
                Timber.d("Upload ảnh thành công: $fileUrl")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Upload ảnh thất bại")
                throw e  // Throw để UI catch và show toast
            }
        }
        
        // 2. Tạo SendMessageParam với fileUrl
        val sendMessageParam = SendMessageParam(
            content = content,
            receiverId = receiverId,
            replyTo = replyTo,
            fileUrl = fileUrl
        )
        
        // 3. Gửi qua socket
        Timber.d("Đã gọi hàm gửi tin nhắn")
        socketRepository.sendMessage(sendMessageParam)
    }
}