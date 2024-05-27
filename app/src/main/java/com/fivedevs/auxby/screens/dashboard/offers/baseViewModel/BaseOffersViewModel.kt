package com.fivedevs.auxby.screens.dashboard.offers.baseViewModel

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.OffersResponse
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

abstract class BaseOffersViewModel(
    private val userApi: UserApi,
    private val dataApi: DataApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val offersRepository: OffersRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseUserViewModel(userApi, rxSchedulers, userRepository, compositeDisposable, preferencesService) {

    var totalPagesCall = 0
    var currentPage = PaginationConstants.PAGINATION_PAGE_START

    val shouldShowSaveGuestMode = SingleLiveEvent<Any>()

    val offerDetailsModel = MutableLiveData<OfferModel>()
    val shouldSaveOfferPage = MutableLiveData<OfferModel>()
    val myOffers: MutableLiveData<List<OfferModel>> = MutableLiveData()
    val myBidOffers: MutableLiveData<List<OfferModel>> = MutableLiveData()
    val offersResponse: MutableLiveData<List<OfferModel>> = MutableLiveData()
    val paginationOffersResponse: MutableLiveData<List<OfferModel>> = MutableLiveData()

    var shouldShowLoader = MutableLiveData<Boolean>().apply { value = false }
    var showShimmerAnimation = MutableLiveData<Boolean>().apply { value = false }

    val shouldSaveOfferPublishSubject: PublishSubject<OfferModel> = PublishSubject.create()

    init {
        saveOfferClicked()
    }

    fun getApiOffers(filters: Map<String, String>) {
        dataApi
            .getOffers(filters)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                handleOffersResponse(it)
            }, {
                handleOffersResponse(OffersResponse())
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun handleOffersResponse(response: OffersResponse) {
        if (totalPagesCall == 0) {
            totalPagesCall = response.totalPages
        }
        if (currentPage == 0) offersResponse.value = response.content.toList()
        else {
            offersRepository.insertOffers(response.content)
            paginationOffersResponse.value = response.content.toList()
        }
    }

    fun saveOfferToFavorites(offerModel: OfferModel) {
        dataApi.saveOfferToFavorites(offerModel.id, offerModel)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({
                offersRepository.handlingSavedOffer(offerModel)
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun saveOfferClicked() {
        shouldSaveOfferPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .subscribe {
                if (preferencesService.isUserLoggedIn()) {
                    shouldSaveOfferPage.value = it
                } else {
                    shouldShowSaveGuestMode.call()
                }
            }
            .disposeBy(compositeDisposable)
    }

    fun getOfferById(offerId: Long) {
        dataApi.getOfferById(offerId)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .doOnNext { offersRepository.updateOffer(it) }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                offerDetailsModel.value = it
                shouldShowLoader.value = false
            }, {
                shouldShowLoader.value = false
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getMyBids() {
        if (isUserLoggedIn.value == true) {
            dataApi.getMyBids()
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .subscribe({ offers ->
                    offersRepository.insertBids(offers)
                }, {
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    fun getMyOffers() {
        if (isUserLoggedIn.value == true) {
            dataApi.getMyOffers()
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .subscribe({ offers ->
                    offersRepository.insertMyOffers(offers)
                }, {
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }
}