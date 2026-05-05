package com.example.realtimechatapp.domain.validation

import com.example.realtimechatapp.domain.exception.MessageException

object MessageValidator {
    fun validateMessageContactIdExist(contactId: String) {
        if (contactId.isBlank()){
            throw MessageException.ContactIdNotExistException
        }
    }
}