package com.fivedevs.auxby.domain.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import com.fivedevs.auxby.domain.utils.Utils.appIsInBackgroundOrKilled
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivityNewTask
import com.fivedevs.auxby.screens.internetConnection.InternetConnectionActivity

class NetworkConnection(private val context: Context) : LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        updateConnection()
        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
    }

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                if (!appIsInBackgroundOrKilled()) {
                    context.launchActivityNewTask<InternetConnectionActivity> { }
                }
                postValue(false)
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }
        }
        return networkCallback
    }

    private fun updateConnection() {
        val isConnected = context.isNetworkConnected()
        if (!isConnected && !appIsInBackgroundOrKilled()) {
            context.launchActivityNewTask<InternetConnectionActivity> { }
        }
        postValue(isConnected)
    }
}