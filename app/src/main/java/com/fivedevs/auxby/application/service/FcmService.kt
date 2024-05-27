package com.fivedevs.auxby.application.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(FcmService::class.java.name, "Received FCM from ${remoteMessage.notification?.body}")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(FcmService::class.java.name, "Received FCM token: $token")
        //TODO: save and send to server
    }
}