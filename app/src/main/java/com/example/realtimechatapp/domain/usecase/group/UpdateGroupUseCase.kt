package com.example.realtimechatapp.domain.usecase.group

import android.net.Uri
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.validation.GroupValidator
import timber.log.Timber
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        groupId: String,
        groupName: String,
        avatar: Uri?,
        description: String?,
        isGroupAvatarChanged: Boolean
    ): Result<Unit> {
        return try {
            GroupValidator.validateGroupNameBlank(groupName)
            val avatarUrl: String? = if (isGroupAvatarChanged) {
                avatar?.let {
                    Timber.d("Đang tải lên avatar")
                    mediaRepository.upload(avatar).getOrThrow()
                }
            } else null
            Timber.d("Tải lên avatar nhóm thành công, url: %s", avatarUrl)

            groupRepository.updateGroup(groupId, groupName, avatarUrl, description)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi cập nhật thông tin nhóm")
            Result.failure(e)
        }
    }
}