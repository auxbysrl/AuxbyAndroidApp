package com.fivedevs.auxby.screens.profile.userActions

import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserActionsViewModel @Inject constructor(
    val googleAuthClient: GoogleSignInClient,
    val preferencesService: PreferencesService,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val accountDeletedEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var userLogoutEvent = SingleLiveEvent<Boolean>()
    var somethingWentWrongEvent = SingleLiveEvent<Any>()

    fun deleteUserAccount() {
        userApi.deleteUser()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                accountDeletedEvent.call()
            }, {
                somethingWentWrongEvent.call()
                Timber.e(it)
            }).disposeBy(compositeDisposable)
    }

    fun logoutUser() {
        userApi.logoutUser()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .doOnError {   Timber.e(it) }
            .subscribe({
                googleAuthClient.signOut()
                userRepository.clearUserData()
                preferencesService.clearUserDetails()
                userLogoutEvent.value = true
            }, {
                userLogoutEvent.value = false
                Timber.e(it)
            }).disposeBy(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
