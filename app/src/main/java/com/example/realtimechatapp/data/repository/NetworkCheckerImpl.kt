package com.example.realtimechatapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.realtimechatapp.domain.repository.NetworkChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
): NetworkChecker{
    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // project set up api >= 26
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true   // wifi
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true   // mobile data: 3g, 4g,..
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true   // wired: ethernet/LAN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true    // virtual private network
            else -> false
        }
    }

}
