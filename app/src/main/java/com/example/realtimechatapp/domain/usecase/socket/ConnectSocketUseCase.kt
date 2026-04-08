package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.repository.SocketRepository
import javax.inject.Inject

class ConnectSocketUseCase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    suspend operator fun invoke(){
        socketRepository.connect()
    }
}