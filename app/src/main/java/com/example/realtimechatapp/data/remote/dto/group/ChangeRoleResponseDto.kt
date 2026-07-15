package com.example.realtimechatapp.data.remote.dto.group

data class ChangeRoleResponseDto(
    val message: String,
    val memberId: String,
    val newRole: RoleDto
)
