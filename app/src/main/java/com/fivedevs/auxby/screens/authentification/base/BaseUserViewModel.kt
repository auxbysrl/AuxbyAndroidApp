package com.fivedevs.auxby.screens.authentification.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.models.UserLoginRequest
import com.fivedevs.auxby.domain.models.UserRegisterRequest
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

abstract class BaseUserViewModel(
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val compositeDisposable: CompositeDisposable,
    private val preferencesService: PreferencesService
) : ViewModel() {

    val emailCheckEvent = SingleLiveEvent<Boolean>()
    val resendEmailEvent = MutableLiveData<String?>()

    val isUserLoggedIn = MutableLiveData<Boolean>().apply {
        value = preferencesService.isUserLoggedIn()
    }
    val userRequest = MutableLiveData<UserRegisterRequest>().apply { value = UserRegisterRequest() }

    fun checkIfUserLoggedIn() {
        isUserLoggedIn.value = preferencesService.isUserLoggedIn()
    }

    fun checkConfirmationEmail() {
        userRequest.value?.let { user ->
            userApi.loginUser(UserLoginRequest(user.email, user.password))
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext {
                    preferencesService.setValue(PreferencesService.USER_TOKEN, it.token)
                    getUser()
                }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({
                    emailCheckEvent.value = true
                }, {
                    emailCheckEvent.value = false
                }).disposeBy(compositeDisposable)
        }
    }

    fun resendEmailVerification() {
        userRequest.value?.email?.let { email ->
            userApi.resendEmailVerification(email.trim())
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    resendEmailEvent.value = null
                }, {
                    resendEmailEvent.value = Constants.DEFAULT_ERROR_MSG
                }).disposeBy(compositeDisposable)
        }
    }

    fun getUser() {
        if (isUserLoggedIn.value == true) {
            userApi.getUser()
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext { userRepository.insertUser(it) }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({ }, {
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    companion object {
        const val CHECK_CONFIRMATION_EMAIL = 5000L
    }
}