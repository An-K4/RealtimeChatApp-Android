package com.example.realtimechatapp.domain.validation

import com.example.realtimechatapp.domain.exception.GroupException

object GroupValidator {
    fun validateGroupIdExist(groupId: String) {
        if (groupId.isBlank()){
            throw GroupException.GroupIdNotExistException
        }
    }

    fun validateMemberIdExist(memberId: String) {
        if (memberId.isBlank()){
            throw GroupException.GroupIdNotExistException
        }
    }

    fun validateGroupMemberSize(members: List<String>) {
        if (members.size < 2) {
            throw GroupException.GroupMemberSizeException
        }
    }
}