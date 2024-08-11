package com.fivedevs.auxby.screens.dashboard.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.NotificationsApi
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsApi: NotificationsApi,
    private val rxSchedulers: RxSchedulers,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    var showShimmerAnimation = MutableLiveData<Boolean>().apply { value = false }
    val notificationsData = MutableLiveData<List<NotificationItem>>()

    val deleteNotificationPublishSubject: PublishSubject<NotificationItem> = PublishSubject.create()
    val notificationSelectorPublishSubject: PublishSubject<NotificationItem> = PublishSubject.create()

    val onNotificationClickEvent: SingleLiveEvent<NotificationItem> =
        SingleLiveEvent()

    init {
        onNotificationClicked()
        onNotificationDeleteClick()
    }

    private fun onNotificationDeleteClick() {
        deleteNotificationPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .doOnNext { showShimmerAnimation.value = true }
            .subscribe({ notification ->
                deleteNotifications(notification.id)
                getNotifications()
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getNotifications() {
        Observable.just(Constants.EMPTY)
            .doOnNext { showShimmerAnimation.value = true }
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.network())
            .flatMap { notificationsApi.getNotifications() }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({ notifications ->
                showShimmerAnimation.value = false
                notificationsData.value = notifications.reversed()
            }, {
                notificationsData.value = mutableListOf()
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun deleteNotifications(notificationId: Long) {
        notificationsApi.deleteNotification(notificationId)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun onNotificationClicked() {
        notificationSelectorPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ notification ->
                deleteNotifications(notification.id)
                onNotificationClickEvent.value = notification
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun deleteNotificationWhenOfferClosed(notification: NotificationItem) {
        getNotifications()
    }

    private fun handleDoOnError(it: Throwable) {
        showShimmerAnimation.value = false
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}