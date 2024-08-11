package com.fivedevs.auxby.screens.buyCoins

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.CoinBundle
import com.fivedevs.auxby.domain.models.TransactionModel
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.Utils.convertMicrosToPrice
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BuyCoinsViewModel @Inject constructor(
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseUserViewModel(
    userApi,
    rxSchedulers,
    userRepository,
    compositeDisposable,
    preferencesService
) {

    val redirectToDashboard: SingleLiveEvent<Any> = SingleLiveEvent()
    val bundleSelectorPublishSubject: PublishSubject<CoinBundle> = PublishSubject.create()

    var selectedBundle = CoinBundle()
    var showShimmerAnimation = MutableLiveData<Boolean>().apply { value = false }

    init {
        onBundleClicked()
    }

    fun makeTransaction() {
        userApi.sendTransaction(
            TransactionModel(
                selectedBundle.coins.toInt(),
                convertMicrosToPrice(selectedBundle.priceInMicros),
                "Auxby - ${selectedBundle.name} pack"
            )
        )
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .doOnError {
                handleDoOnError(it)
            }
            .subscribe({
                showShimmerAnimation.value = false
                getUser()
                redirectToDashboard.call()
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun onBundleClicked() {
        bundleSelectorPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ bundle ->
                selectedBundle = bundle
            }, {
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
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