package com.fivedevs.auxby.screens.dashboard.offers.details

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.*
import com.fivedevs.auxby.domain.models.enums.ErrorTypesEnum
import com.fivedevs.auxby.domain.models.enums.OfferStateEnum
import com.fivedevs.auxby.domain.models.enums.UserTypeEnum
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    private val userApi: UserApi,
    private val dataApi: DataApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
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

    val reportOfferEvent = SingleLiveEvent<Any>()
    val offerDeleteEvent = SingleLiveEvent<Any>()
    var somethingWentWrongEvent = SingleLiveEvent<Any>()
    val offerNotAvailableEvent = SingleLiveEvent<Any>()
    val placeBidEvent = SingleLiveEvent<PlaceBidModel>()
    val placeBidSuccessEvent = SingleLiveEvent<Boolean>()
    val favoriteOfferCallEvent = SingleLiveEvent<Boolean>()
    val shouldShowWholeDescription = SingleLiveEvent<Any>()
    val wasBidAccepted = SingleLiveEvent<Boolean>()

    val user = MutableLiveData<User>().apply { value = User() }
    var isLightStatusBar = MutableLiveData<Boolean>().apply { value = false }
    val notEnoughCoinsEvent = SingleLiveEvent<Boolean>().apply { value = false }
    var userType = MutableLiveData<UserTypeEnum>().apply { UserTypeEnum.NORMAL_FIX_PRICE_OFFER }

    private var canIncreaseViewsNumber = true

    var selectedLanguage = preferencesService.getSelectedLanguageApp()
    var categoryDetailsModel = CategoryDetailsModel()
    var phoneNumber = ""

    init {
        if (isUserLoggedIn.value == true) {
            getCurrentUser()
        }
    }

    private fun getCurrentUser() {
        userRepository.getCurrentUser()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                user.value = it
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getCategoryDetails(categoryId: Int) {
        categoryRepository.getCategoryDetailsById(categoryId)
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                categoryDetailsModel = it
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getLocalOffer(offerId: Long) {
        showShimmerAnimation.value = true
        offersRepository.getOfferById(offerId)
            .subscribeOn(rxSchedulers.background())
            .doOnNext { getCategoryDetails(it.categoryId ?: 0) }
            .observeOn(rxSchedulers.network())
            .flatMap {
                dataApi.getOfferById(
                    it.id,
                    increaseView = if (it.status.equals(OfferStateEnum.ACTIVE.getStatusName())) canIncreaseViewsNumber else false
                )
                    .toFlowable(BackpressureStrategy.BUFFER)
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({ offer ->
                canIncreaseViewsNumber = false
                showShimmerAnimation.value = false
                offerDetailsModel.value = offer
                checkUserType(offer)
            }, {
                offerNotAvailableEvent.call()
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun checkUserType(offerModel: OfferModel) {
        val ownerEmailAddress = offerModel.owner?.userName.orEmpty()
        val userEmailAddress = user.value?.email?.trim().orEmpty()
        val isOwner = (userEmailAddress == ownerEmailAddress)
        val isAuction = offerModel.isOnAuction

        checkIfUsernameIsEmpty(userEmailAddress, isAuction)

        userType.value = when {
            isOwner && isAuction -> UserTypeEnum.OWNER_AUCTION_OFFER
            !isOwner && isAuction -> UserTypeEnum.NORMAL_AUCTION_OFFER
            isOwner && !isAuction -> UserTypeEnum.OWNER_FIX_PRICE_OFFER
            else -> UserTypeEnum.NORMAL_FIX_PRICE_OFFER
        }
    }

    private fun checkIfUsernameIsEmpty(userName: String, isAuction: Boolean) {
        if (userName.trim().isEmpty()) {
            if (isAuction) {
                userType.value = UserTypeEnum.NORMAL_AUCTION_OFFER
            } else {
                userType.value = UserTypeEnum.NORMAL_FIX_PRICE_OFFER
            }
            return
        }
    }

    fun sendBidToServer(bidModel: PlaceBidModel) {
        Observable.just(Constants.EMPTY)
            .doOnNext { shouldShowLoader.value = true }
            .observeOn(rxSchedulers.background())
            .flatMap {
                categoryRepository.getCategoryDetailsById(
                    offerDetailsModel.value?.categoryId ?: 0
                ).toObservable()
            }
            .doOnNext { bidModel.apply { requiredCoins = it.placeBidCost } }
            .observeOn(rxSchedulers.network())
            .flatMap {
                checkForAvailableCoins(it, bidModel)
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                getOfferById(bidModel.offerId)
                getUser()
                getMyBids()
                placeBidSuccessEvent.value = true
                wasBidAccepted.value = it.wasBidAccepted
            }, {
                shouldShowLoader.value = false
                handleDoOnError(it)
                checkThrowableSource(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun checkThrowableSource(it: Throwable) {
        if (it.message == ErrorTypesEnum.NOT_ENOUGH_COINS_ERROR.name) {
            notEnoughCoinsEvent.value = true
        } else {
            placeBidSuccessEvent.value = false
        }
    }

    private fun checkForAvailableCoins(it: CategoryDetailsModel, bidModel: PlaceBidModel) =
        if ((user.value?.availableCoins ?: 0) >= it.placeBidCost) {
            dataApi.placeBid(bidModel)
        } else {
            Observable.error(Throwable(ErrorTypesEnum.NOT_ENOUGH_COINS_ERROR.name))
        }

    fun favoriteOfferCall() {
        offerDetailsModel.value?.let { offer ->
            offer.apply { isUserFavorite = !offer.isUserFavorite }
            dataApi.saveOfferToFavorites(offer.id, offer)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext {
                    offersRepository.handlingSavedOffer(offer)
                }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({
                    offerDetailsModel.value = offer
                }, { throwable ->
                    somethingWentWrongEvent.call()
                    favoriteOfferCallEvent.value = false
                    handleDoOnError(throwable)
                }).disposeBy(compositeDisposable)
        }
    }

    fun toggleFullDescription() {
        shouldShowWholeDescription.call()
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    fun sendOfferReport(reportOfferModel: ReportOfferModel) {
        offerDetailsModel.value?.let { offer ->
            shouldShowLoader.value = true
            dataApi.reportOffer(offer.id, reportOfferModel)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    shouldShowLoader.value = false
                    reportOfferEvent.call()
                }, {
                    shouldShowLoader.value = false
                    reportOfferEvent.call()
                    somethingWentWrongEvent.call()
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    fun deleteCurrentOffer() {
        offerDetailsModel.value?.let { offer ->
            shouldShowLoader.value = true
            dataApi.deleteOfferById(offer.id)
                .delay(300, TimeUnit.MILLISECONDS)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext { offersRepository.deleteOfferById(offer.id) }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({
                    shouldShowLoader.value = false
                    offerDeleteEvent.call()
                }, {
                    shouldShowLoader.value = false
                    offerDeleteEvent.call()
                    somethingWentWrongEvent.call()
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    fun changeOfferStatus() {
        offerDetailsModel.value?.let { offer ->
            shouldShowLoader.value = true
            dataApi.changeOfferStatus(
                offer.id,
                RequiredCoinsModel(requiredCoins = categoryDetailsModel.addOfferCost)
            )
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    getOfferById(offerId = offer.id)
                }, {
                    shouldShowLoader.value = false
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    fun isBidWinner(): Boolean {
        return offerDetailsModel.value?.bids?.first {
            it?.winner ?: false
        }?.userName == user.value?.firstName + user.value?.lastName
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun hasEnoughCoins(): Boolean {
        return (user.value?.availableCoins ?: 0) >= categoryDetailsModel.addOfferCost
    }
}