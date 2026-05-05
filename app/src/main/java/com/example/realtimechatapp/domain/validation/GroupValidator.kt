package com.example.realtimechatapp.domain.validation

import com.example.realtimechatapp.domain.exception.GroupException

object GroupValidator {
    fun validateGroupIdExist(groupId: String) {
        if (groupId.isBlank()){
            throw GroupException.GroupIdNotExistException
        }
    }
}