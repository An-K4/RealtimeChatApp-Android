package com.example.realtimechatapp.domain.repository

interface NetworkChecker {
    fun isNetworkAvailable(): Boolean
}