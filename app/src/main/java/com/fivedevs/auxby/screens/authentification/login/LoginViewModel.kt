package com.fivedevs.auxby.screens.authentification.login

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.data.prefs.PreferencesService.Companion.IS_GOOGLE_ACCOUNT
import com.fivedevs.auxby.data.prefs.PreferencesService.Companion.USER_TOKEN
import com.fivedevs.auxby.domain.models.GoogleAuthRequest
import com.fivedevs.auxby.domain.models.UserLoginRequest
import com.fivedevs.auxby.domain.utils.Constants.DEFAULT_ERROR_MSG
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_400
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_400_GOOGLE
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_423
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_470
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val googleAuthClient: GoogleSignInClient,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseUserViewModel(userApi, rxSchedulers, userRepository, compositeDisposable, preferencesService) {

    var userReferralId: Int? = null

    val loginEvent = SingleLiveEvent<String?>()
    val checkEmailEvent = SingleLiveEvent<Any>()
    val loginClickEvent = SingleLiveEvent<Any>()
    val signUpClickEvent = SingleLiveEvent<Any>()
    val loginGoogleClickEvent = SingleLiveEvent<Any>()
    val forgotPasswordClickEvent = SingleLiveEvent<Any>()

    val enableLogin = MutableLiveData<Boolean>().apply { value = false }

    fun onLoginClicked() {
        loginClickEvent.call()
    }

    fun onForgotPasswordClicked() {
        forgotPasswordClickEvent.call()
    }

    fun onLoginWithGoogleClicked() {
        loginGoogleClickEvent.call()
    }

    fun onSignUpClicked() {
        signUpClickEvent.call()
    }

    fun loginUser() {
        userRequest.value?.let { user ->
            userApi.loginUser(UserLoginRequest(user.email, user.password))
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext {
                    preferencesService.setValue(USER_TOKEN, it.token)
                    getUserOnLoginEvent()
                }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { Timber.e(it) }
                .subscribe({
                    loginEvent.call()
                }, {
                    handleLoginError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    fun googleAuth(idToken: String) {
        userApi.googleAuth(GoogleAuthRequest(idToken, userReferralId))
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .doOnNext {
                preferencesService.setValue(USER_TOKEN, it.token)
                preferencesService.setValue(IS_GOOGLE_ACCOUNT, true)
                getUserOnLoginEvent()
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { Timber.e(it) }
            .subscribe({
                loginEvent.call()
            }, {
                handleGoogleLoginError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun handleLoginError(it: Throwable) {
        if (it is HttpException) {
            when (it.code()) {
                RESPONSE_CODE_423 -> checkEmailEvent.call()
                RESPONSE_CODE_470 -> loginEvent.value = RESPONSE_CODE_470.toString()
                RESPONSE_CODE_400 -> loginEvent.value = RESPONSE_CODE_400.toString()
                else -> loginEvent.value = DEFAULT_ERROR_MSG
            }
        } else {
            loginEvent.value = DEFAULT_ERROR_MSG
        }
    }

    private fun handleGoogleLoginError(it: Throwable) {
        if (it is HttpException) {
            when (it.code()) {
                RESPONSE_CODE_400 -> loginEvent.value = RESPONSE_CODE_400_GOOGLE
                else -> loginEvent.value = DEFAULT_ERROR_MSG
            }
        } else {
            loginEvent.value = DEFAULT_ERROR_MSG
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}