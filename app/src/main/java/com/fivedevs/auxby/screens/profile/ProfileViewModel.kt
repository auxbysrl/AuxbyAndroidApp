package com.fivedevs.auxby.screens.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    val preferencesService: PreferencesService,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val uploadedAvatarImage = MutableLiveData<String>()
    val showEditProfile = MutableLiveData<Boolean>().apply { value = false }
    val showEditAddress = MutableLiveData<Boolean>().apply { value = false }
    val showEditPassword = MutableLiveData<Boolean>().apply { value = false }
    val showEditPhoneNumber = MutableLiveData<Boolean>().apply { value = false }


    val userUpdateEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val user = MutableLiveData<User>().apply { value = User() }
    var initialUser = MutableLiveData<User>().apply { value = User() }

    init {
        getUserFromDB()
    }

    fun uploadUserAvatar(avatarImage: MultipartBody.Part) {
        userApi.uploadUserAvatar(avatarImage)
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .doOnNext {
                userRepository.updateUserAvatar(it.avatarUrl)
            }
            .observeOn(rxSchedulers.androidUI())
            .doOnError { handleDoOnError(it) }
            .subscribe({
                uploadedAvatarImage.value = it.avatarUrl
            }, {
                getUserFromDB()
                uploadedAvatarImage.value = ""
                handleDoOnError(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun updateUser() {
        user.value?.let { currentUser ->
            userApi.updateUser(currentUser)
                .subscribeOn(rxSchedulers.network())
                .observeOn(rxSchedulers.background())
                .doOnNext { userRepository.insertUser(it) }
                .observeOn(rxSchedulers.androidUI())
                .doOnError { handleDoOnError(it) }
                .subscribe({ updatedUser ->
                    user.value = updatedUser
                    userUpdateEvent.value = true
                    initialUser.value = user.value
                }, {
                    userUpdateEvent.value = false
                    cancelUserDetailsChanges()
                    handleDoOnError(it)
                }).disposeBy(compositeDisposable)
        }
    }

    fun getReferralLink(callback: (link: String) -> Unit) {
        userApi.getUserReferralLink()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                callback(it.url)
            }, {
                callback("")
            })
            .disposeBy(compositeDisposable)
    }

    fun cancelUserDetailsChanges() {
        user.value = initialUser.value
    }

    private fun getUserFromDB() {
        userRepository.getCurrentUser()
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                initialUser.value = it.copy()
                user.value = it
            }, {
                handleDoOnError(it)
            }).disposeBy(compositeDisposable)
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}