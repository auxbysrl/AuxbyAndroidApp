package com.fivedevs.auxby.screens.dashboard.bids

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.OfferStateEnum
import com.fivedevs.auxby.domain.utils.Constants.MY_BIDS_TYPE
import com.fivedevs.auxby.domain.utils.Constants.MY_OFFERS_TYPE
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyOffersViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val offersRepository: OffersRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi,
    dataApi,
    rxSchedulers,
    userRepository,
    offersRepository,
    preferencesService,
    compositeDisposable
) {

    val activeOffers: MutableLiveData<MutableList<OfferModel>> = MutableLiveData()
    val inactiveOffers: MutableLiveData<MutableList<OfferModel>> = MutableLiveData()
    var fragmentType = MY_BIDS_TYPE

    fun onViewCreated(type: Int) {
        fragmentType = type
        when (type) {
            MY_OFFERS_TYPE -> {
                getLocalMyOffers()
            }
            else -> {
                getLocalMyBids()
            }
        }
    }

    private fun getLocalMyBids() {
        offersRepository.getBids()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ offers ->
                Timber.i("getLocalMyBids")
                myBidOffers.value = offers
            }, {
                myBidOffers.value = mutableListOf()
            }).disposeBy(compositeDisposable)
    }

    private fun getLocalMyOffers() {
        offersRepository.getMyOffers()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ offers ->
                Timber.i("getLocalMyOffers")
                myOffers.value = offers
            }, {
                myOffers.value = mutableListOf()
            }).disposeBy(compositeDisposable)
    }

    fun filterOffers(offers: List<OfferModel>) {
        activeOffers.value = offers.filter {
            it.status.equals(
                OfferStateEnum.ACTIVE.name,
                true
            )
        } as MutableList<OfferModel>
        inactiveOffers.value = offers.filter {
            !it.status.equals(
                OfferStateEnum.ACTIVE.name,
                true
            )
        } as MutableList<OfferModel>
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}