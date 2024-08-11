package com.fivedevs.auxby.domain.managers

import android.os.Handler
import android.os.Looper
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class OffersUpdateManager @Inject constructor(
    val dataApi: DataApi,
    val rxSchedulers: RxSchedulers,
    val offersRepository: OffersRepository
) {

    private val compositeDisposable = CompositeDisposable()
    private val configureUpdateTimer: Handler = Handler(Looper.getMainLooper())
    private var errorTries = 0

    fun initOffersUpdateManager() {
        getAuctionOffers()
    }

    private fun getAuctionOffers() {
        offersRepository
            .getAuctionOffers()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.network())
            .flatMap { bidOffers ->
                val bidIds = bidOffers.map { it.id }
                if (bidIds.isEmpty()) return@flatMap Single.fromCallable { emptyList() }

                dataApi.getOfferByIds(bidIds)
            }
            .observeOn(rxSchedulers.background())
            .subscribe({
                if(it.isNotEmpty()) offersRepository.insertOffers(it)
                errorTries = 0
                startUpdateTimer()
            }, {
                errorTries += 1
                Timber.e(it)
                startUpdateTimer()
            })
            .disposeBy(compositeDisposable)
    }


    private fun startUpdateTimer() {
        if (errorTries == MAX_ERROR_TRIES) return
        configureUpdateTimer.postDelayed({
            getAuctionOffers()
        }, PERIODICALLY_UPDATE_DELAY)
    }

    companion object {
        private const val MAX_ERROR_TRIES = 10
        private const val PERIODICALLY_UPDATE_DELAY = 30000L // 30 sec
    }
}