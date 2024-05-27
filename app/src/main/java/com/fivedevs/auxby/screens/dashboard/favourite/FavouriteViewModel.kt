package com.fivedevs.auxby.screens.dashboard.favourite

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    userApi: UserApi,
    userRepository: UserRepository,
    preferencesService: PreferencesService,
    private val dataApi: DataApi,
    private val rxSchedulers: RxSchedulers,
    private val offersRepository: OffersRepository,
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
    val savedOffers: MutableLiveData<List<OfferModel>> = MutableLiveData()

    fun onViewCreated() {
        getSavedOffers()
    }

    private fun getSavedOffers() {
        offersRepository.getSavedOffers()
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ offers ->
                savedOffers.value = offers
            }, {
                savedOffers.value = listOf()
                Timber.e(it)
            }).disposeBy(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}