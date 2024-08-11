package com.fivedevs.auxby.screens.changePassword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.domain.models.ChangePasswordRequest
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val compositeDisposable: CompositeDisposable,
    private val rxSchedulers: RxSchedulers,
    private val userApi: UserApi
) : ViewModel() {

    val onBackPressed =
        SingleLiveEvent<Boolean>()
    val returnToProfile =
        SingleLiveEvent<Boolean>()

    val enableConfirmPwdBtn = MutableLiveData<Boolean>().apply { value = false }
    val changePwdRequest = MutableLiveData<ChangePasswordRequest>().apply { value = ChangePasswordRequest() }

    fun onBackToProfileClicked() {
        onBackPressed.call()
    }

    fun changePassword() {
        changePwdRequest.value?.let {
            userApi.changePassword(it)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    returnToProfile.value = true
                }, {
                    returnToProfile.value = false
                    handleDoOnError(it)
                })
                .disposeBy(compositeDisposable)
        }
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}