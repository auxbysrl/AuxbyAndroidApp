package com.fivedevs.auxby.screens.forgotPassword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ForgotPassViewModel @Inject constructor(
    private val compositeDisposable: CompositeDisposable,
    private val rxSchedulers: RxSchedulers,
    private val userApi: UserApi
) : ViewModel() {

    val onCloseToOffersEvent = SingleLiveEvent<Any>()
    val backToLoginEvent = SingleLiveEvent<Any>()
    val sendResetLinkEvent = SingleLiveEvent<Any>()
    val resetLinkResponse = SingleLiveEvent<String>()

    val enableResetLink = MutableLiveData<Boolean>().apply { value = false }

    fun sendResetLink(emailAddress: String) {
        userApi.sendResetLink(emailAddress)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                resetLinkResponse.value = Constants.EMPTY
            }, {
                handleOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun handleOnError(error: Throwable) {
        resetLinkResponse.value = Constants.DEFAULT_ERROR_MSG
        Timber.e(error)
    }

    fun onBackToLoginClicked() {
        backToLoginEvent.call()
    }

    fun onCloseToOffersClicked() {
        onCloseToOffersEvent.call()
    }

    fun onSendResetLinkClicked() {
        sendResetLinkEvent.call()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}