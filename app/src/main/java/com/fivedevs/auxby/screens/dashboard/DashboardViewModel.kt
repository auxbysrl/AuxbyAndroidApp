package com.fivedevs.auxby.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.DataApi
import com.fivedevs.auxby.data.api.NotificationsApi
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.data.database.repositories.CategoryRepository
import com.fivedevs.auxby.data.database.repositories.OffersRepository
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.data.services.firebase.FirebaseMessagingService
import com.fivedevs.auxby.domain.managers.OffersUpdateManager
import com.fivedevs.auxby.domain.models.AdsModel
import com.fivedevs.auxby.domain.models.PushNotificationsSubscribe
import com.fivedevs.auxby.domain.models.TokenFcmEvent
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Constants.REFRESH_DATA_AFTER_INTERNET_CONNECTION
import com.fivedevs.auxby.domain.utils.Currencies.currenciesList
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.network.NetworkConnection
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants.paginationFilters
import com.fivedevs.auxby.domain.utils.rx.RxBus
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.dashboard.offers.baseViewModel.BaseOffersViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val rxBus: RxBus,
    private val userApi: UserApi,
    private val dataApi: DataApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val notificationsApi: NotificationsApi,
    private val offersRepository: OffersRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable,
    networkConnection: NetworkConnection,
    private val offersUpdateManager: OffersUpdateManager
) : BaseOffersViewModel(
    userApi, dataApi, rxSchedulers, userRepository,
    offersRepository, preferencesService, compositeDisposable
) {

    val toolbarTitle = MutableLiveData<String>().apply { value = "" }
    val hasNotifications = MutableLiveData<Boolean>().apply { value = false }
    val networkConnectionState: LiveData<Boolean> = networkConnection

    var loginClickEvent = SingleLiveEvent<Any>()
    var searchClickEvent = SingleLiveEvent<Any>()
    var profileClickEvent = SingleLiveEvent<Any>()
    var addOfferClickEvent = SingleLiveEvent<Any>()
    var policiesClickEvent = SingleLiveEvent<Any>()
    var settingsClickEvent = SingleLiveEvent<Any>()
    var contactUsCLickEvent = SingleLiveEvent<Any>()
    var termsConditionsEvent = SingleLiveEvent<Any>()
    var yourOffersClickEvent = SingleLiveEvent<Any>()
    var closeDrawerClickEvent = SingleLiveEvent<Any>()
    var userOptionsClickEvent = SingleLiveEvent<Any>()
    var notificationsClickEvent = SingleLiveEvent<Any>()
    var refreshChatFromNotificationEvent = SingleLiveEvent<Any>()

    var buyCoinsClickEvent = SingleLiveEvent<Any>()
    var refreshDataEvent = SingleLiveEvent<Any>()
    var appAdsEvent = SingleLiveEvent<AdsModel>()

    fun getOneTimeData() {
        getData()
        getAppAds()
        getAppCurrencies()
        getCategoriesDetails()
        listenForNewFcmToken()
        listenForNewChatMessages()
        refreshDataAfterConnectionLost()
        offersUpdateManager.initOffersUpdateManager()
    }

    fun getData() {
        getOffers()
        getCategories()
    }

    private fun getAppCurrencies() {
        dataApi.getAllCurrencies()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ currencies ->
                currenciesList = currencies
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getAppAds() {
        dataApi.getAppAds()
            .subscribeOn(rxSchedulers.network())
            .delay(5000L, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ ads ->
                ads.firstOrNull()?.let {
                    if (preferencesService.getUserSeenAdCode() != it.code) {
                        appAdsEvent.value = it
                    }
                }
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    fun saveUserAdCode(code: String) {
        preferencesService.setValue(PreferencesService.APP_ADS_CODE, code)
    }

    fun getNotifications() {
        if (isUserLoggedIn.value == true) {
            notificationsApi.getNotifications()
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    hasNotifications.value = it.isNotEmpty()
                }, {
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    private fun clearOffers() {
        offersRepository.clearOffers()
    }

    fun getCategories() {
        dataApi.getAllCategories()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ categories ->
                categoryRepository.insertCategories(categories)
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun getOffers() {
        dataApi.getOffers(paginationFilters.toMap())
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ offersResponse ->
                clearOffers()
                offersRepository.insertOffers(offersResponse.content)
                getPromotedOffers()
                getApiSavedOffers()
                getMyBids()
                getMyOffers()
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getPromotedOffers() {
        dataApi.getPromotedOffers()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ promotedOffers ->
                offersRepository.insertPromotedOffers(promotedOffers)
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getCategoriesDetails() {
        dataApi.getCategoriesDetails().subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ categoriesDetails ->
                categoryRepository.insertCategoriesDetails(categoriesDetails)
            }, {
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getApiSavedOffers() {
        dataApi
            .getSavedOffers()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .subscribe({ offers ->
                offersRepository.insertSavedOffers(offers)
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    fun onLoginClicked() {
        loginClickEvent.call()
    }

    fun onAddOfferClicked() {
        if (isUserLoggedIn.value == true) addOfferClickEvent.call()
        else shouldShowSaveGuestMode.call()
    }

    fun listenForFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                task.exception?.cause?.let { handleDoOnError(it) }
                return@OnCompleteListener
            }

            val token = task.result.toString()
            if (canReceiveNotifications()) {
                sendFCMTokenToServer(token)
            }
        })
    }

    private fun listenForNewFcmToken() {
        rxBus.toObservable()
            .observeOn(rxSchedulers.androidUI())
            .filter { it is TokenFcmEvent }
            .map { it as TokenFcmEvent }
            .subscribe {
                sendFCMTokenToServer(it.fcmToken)
            }
            .disposeBy(compositeDisposable)
    }

    private fun listenForNewChatMessages() {
        rxBus.toObservable()
            .observeOn(rxSchedulers.androidUI())
            .filter { it is NotificationItem }
            .map { it as NotificationItem }
            .subscribe {
                refreshChatFromNotificationEvent.call()
            }
            .disposeBy(compositeDisposable)
    }

    private fun sendFCMTokenToServer(deviceToken: String) {
        if (isUserLoggedIn.value == true) {
            Observable.just(Constants.EMPTY)
                .observeOn(rxSchedulers.network())
                .flatMap {
                    userApi.sendDeviceToken(
                        PushNotificationsSubscribe(
                            deviceToken
                        )
                    )
                }
                .subscribe({
                    Timber.tag(FirebaseMessagingService.TAG).d("sent to server")
                }, {
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    private fun refreshDataAfterConnectionLost() {
        rxBus.toObservable()
            .observeOn(rxSchedulers.androidUI())
            .filter { it == REFRESH_DATA_AFTER_INTERNET_CONNECTION }
            .subscribe {
                getData()
            }
            .disposeBy(compositeDisposable)
    }

    fun onSearchClicked() {
        searchClickEvent.call()
    }

    fun onCloseDrawerClicked() {
        closeDrawerClickEvent.call()
    }

    fun onUserOptionsClicked() {
        userOptionsClickEvent.call()
    }

    fun onProfileClicked() {
        profileClickEvent.call()
    }

    fun onNotificationsClicked() {
        notificationsClickEvent.call()
    }

    fun onYourOffersClicked() {
        yourOffersClickEvent.call()
    }

    fun onSettingsClicked() {
        settingsClickEvent.call()
    }

    fun onBuyCoinsClicked() {
        buyCoinsClickEvent.call()
    }

    fun onContactUsClicked() {
        contactUsCLickEvent.call()
    }

    fun onPoliciesAgreementsClicked() {
        policiesClickEvent.call()
    }

    fun onTermsConditionsClicked() {
        termsConditionsEvent.call()
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    private fun canReceiveNotifications(): Boolean {
        return preferencesService.canReceiveNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}