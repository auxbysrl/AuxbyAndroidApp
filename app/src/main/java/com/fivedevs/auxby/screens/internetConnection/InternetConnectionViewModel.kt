package com.fivedevs.auxby.screens.internetConnection

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.domain.utils.Constants.REFRESH_DATA_AFTER_INTERNET_CONNECTION
import com.fivedevs.auxby.domain.utils.network.NetworkConnection
import com.fivedevs.auxby.domain.utils.rx.RxBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InternetConnectionViewModel @Inject constructor(
    networkConnection: NetworkConnection,
    private val rxBus: RxBus
) : ViewModel() {
    val networkConnectionState: LiveData<Boolean> = networkConnection

    fun sendDataRefreshEvent() {
        rxBus.send(REFRESH_DATA_AFTER_INTERNET_CONNECTION)
    }
}