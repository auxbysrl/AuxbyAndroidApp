package com.fivedevs.auxby.screens.authentification.register

import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Utils.handleApiError
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : BaseUserViewModel(userApi, rxSchedulers, userRepository, compositeDisposable, preferencesService) {

    val loginClickEvent = SingleLiveEvent<Any>()
    val exitClickEvent = SingleLiveEvent<Any>()
    val backClickEvent = SingleLiveEvent<Any>()
    val userExistsEvent = SingleLiveEvent<Any>()
    val signUpEvent = SingleLiveEvent<String?>()
    val showCheckEmailScreenEvent = SingleLiveEvent<Any>()

    val nextScreenEvent = MutableLiveData<String?>()
    val enableNextBtn = MutableLiveData<Boolean>().apply { value = false }
    val showBackArrow = MutableLiveData<Boolean>().apply { value = false }
    val isTermsChecked = MutableLiveData<Boolean>().apply { value = false }
    val enableSignUpBtn = MutableLiveData<Boolean>().apply { value = false }

    fun registerUser() {
        userRequest.value?.let { user ->
            userApi.registerUser(user)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    signUpEvent.call()
                }, { t ->
                    signUpEvent.value = handleApiError(t)
                }).disposeBy(compositeDisposable)
        }
    }

    fun checkEmailExists() {
        userRequest.value?.email?.let { email ->
            userApi.checkEmailExists(email.trim())
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({ emailExists ->
                    if (emailExists) {
                        userExistsEvent.call()
                    } else {
                        nextScreenEvent.value = null
                    }
                }, {
                    nextScreenEvent.value = Constants.DEFAULT_ERROR_MSG
                }).disposeBy(compositeDisposable)
        }
    }

    fun onLoginClicked() {
        loginClickEvent.call()
    }

    fun onExitClicked() {
        exitClickEvent.call()
    }

    fun onBackClicked() {
        backClickEvent.call()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}