package com.fivedevs.auxby.data.services.firebase

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.TokenFcmEvent
import com.fivedevs.auxby.domain.utils.Utils.isForegrounded
import com.fivedevs.auxby.domain.utils.rx.RxBus
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var rxBus: RxBus

    @Inject
    lateinit var preferencesService: PreferencesService

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (::preferencesService.isInitialized) {
            if (preferencesService.isUserLoggedIn()) {
                val dataJson = Gson().toJson(remoteMessage.notification)
                val originalNotificationModel = Gson().fromJson(dataJson, NotificationItem::class.java)

                // Create a copy with the updated message
                val updatedNotificationModel = originalNotificationModel.copy(message = remoteMessage.notification?.body.orEmpty())

                createNotificationBuilder(updatedNotificationModel)
                Timber.tag(TAG).d("Payload notification: ${remoteMessage.notification}")
            }
        }
        Timber.tag(TAG).d("Payload notification: ${remoteMessage.notification}")
        Timber.tag(TAG).d("Payload data: ${remoteMessage.data}")
    }

    override fun onNewToken(token: String) {
        Timber.tag(TAG).d("Refresh token: $token")
        val rxBus = RxBus()
        rxBus.send(TokenFcmEvent(token))
    }

    @SuppressLint("MissingPermission")
    private fun createNotificationBuilder(notificationModel: NotificationItem) {
        NOTIFICATION_ID++
        if (::rxBus.isInitialized) {
            rxBus.send(notificationModel)
        }
        createNotificationChanel()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationBuilder: Notification
        val notificationTitle = notificationModel.title
        val notificationDescription = notificationModel.message
        val notificationIcon = R.mipmap.ic_launcher

        val intent = Intent(applicationContext, DashboardActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val notificationPendingIntent = if (isForegrounded()) {
            PendingIntent.getActivity(
                applicationContext, NOTIFICATION_ID, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                applicationContext, NOTIFICATION_ID, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        notificationBuilder = NotificationCompat.Builder(applicationContext, CHANEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setContentIntent(notificationPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(NOTIFICATIONS_GROUP_KEY)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder)
    }

    private fun createNotificationChanel() {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(CHANEL_ID) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val chanel = NotificationChannel(
                CHANEL_ID,
                CHANEL_NAME, importance
            )
            notificationManager.createNotificationChannel(chanel)
        }
    }

    companion object {
        var NOTIFICATION_ID = 1
        const val TAG = "FirebaseMessagingService"
        const val CHANEL_ID = "HEADS_UP_NOTIFICATION"
        const val CHANEL_NAME = "Auxby Notifications Channel"
        const val NOTIFICATIONS_GROUP_KEY = "AuxbyGroup"
    }
}