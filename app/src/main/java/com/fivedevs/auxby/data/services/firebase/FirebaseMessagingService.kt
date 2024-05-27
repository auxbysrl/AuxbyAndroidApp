package com.fivedevs.auxby.data.services.firebase

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.TokenFcmEvent
import com.fivedevs.auxby.domain.models.enums.getNotificationDescription
import com.fivedevs.auxby.domain.models.enums.getNotificationTitle
import com.fivedevs.auxby.domain.utils.rx.RxBus
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber

class FirebaseMessagingService(
    @ApplicationContext val context: Context,
    private val rxBus: RxBus,
    private val preferencesService: PreferencesService
) : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (preferencesService.isUserLoggedIn()) {
            val dataJson = Gson().toJson(remoteMessage.data)
            val notificationModel = Gson().fromJson(dataJson, NotificationItem::class.java)
            createNotificationBuilder(notificationModel)
        }
        Timber.tag(TAG).d("Message data payload: ${remoteMessage.notification}")
    }

    override fun onNewToken(token: String) {
        Timber.tag(TAG).d("Refresh token: $token")
        val rxBus = RxBus()
        rxBus.send(TokenFcmEvent(token))
    }

    @SuppressLint("MissingPermission")
    private fun createNotificationBuilder(notificationModel: NotificationItem) {
        NOTIFICATION_ID++
        rxBus.send(notificationModel)
        createNotificationChanel()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationBuilder: Notification
        val notificationTitle = getNotificationTitle(notificationModel.type, context)
        val notificationDescription = getNotificationDescription(notificationModel.type, context)
        val notificationIcon = R.mipmap.ic_launcher

        val intent = Intent(applicationContext, DashboardActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val notificationPendingIntent = PendingIntent.getActivity(
            applicationContext, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

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