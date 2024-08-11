package com.fivedevs.auxby.screens.dashboard.offers.userOffers

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.models.SellerRatingModel
import com.fivedevs.auxby.domain.models.enums.FiltersEnum
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ViewUserOffersViewModel @Inject constructor(
    private val dataApi: DataApi,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val offersRepository: OffersRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseOffersViewModel(
    userApi, dataApi, rxSchedulers, userRepository, offersRepository, preferencesService, compositeDisposable
) {

    val allowRatingStatus = MutableLiveData<Boolean>()

    private var ownerUsername = ""

    fun initData(username: String) {
        ownerUsername = username
        getAllowRatingStatus(username)
        getLocalOffersByUsername()
    }

    fun rateSeller(sellerRatingModel: SellerRatingModel) {
        userApi.rateSeller(sellerRatingModel.apply { username = ownerUsername })
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({
                Timber.i("Success")
            }, {
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getLocalOffersByUsername() {
        offersRepository
            .getActiveOffers()
            .subscribeOn(rxSchedulers.background())
            .flatMap { response ->
                val userOffer = response.filter { it.owner?.userName.orEmpty() == ownerUsername }
                Flowable.just(userOffer)
            }
            .doOnNext {
                loadFirstPage()
            }
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                offersResponse.value = it
            }, {
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun loadMoreOffers() {
        currentPage += 1
        getApiOffers(PaginationConstants.paginationFilters.toMutableMap().apply {
            put(FiltersEnum.PAGE_KEY.type, currentPage.toString())
            put(FiltersEnum.USER_OFFERS_KEY.type, ownerUsername)

        })
    }

    private fun loadFirstPage() {
        getApiOffers(PaginationConstants.paginationFilters.toMutableMap().apply {
            put(FiltersEnum.PAGE_KEY.type, "0")
            put(FiltersEnum.USER_OFFERS_KEY.type, ownerUsername)
        })
    }

    private fun getAllowRatingStatus(username: String) {
        userApi.getAllowRatingStatus(username)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                allowRatingStatus.value = it
            }, {
                allowRatingStatus.value = false
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}