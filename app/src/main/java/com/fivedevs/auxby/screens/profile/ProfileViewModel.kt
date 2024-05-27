package com.fivedevs.auxby.screens.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    val googleAuthClient: GoogleSignInClient,
    val preferencesService: PreferencesService,
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val uploadedAvatarImage = MutableLiveData<String>()
    val showMenuPopup = MutableLiveData<Boolean>().apply { value = false }
    val showEditProfile = MutableLiveData<Boolean>().apply { value = false }
    val showEditAddress = MutableLiveData<Boolean>().apply { value = false }
    val showEditPassword = MutableLiveData<Boolean>().apply { value = false }
    val showEditPhoneNumber = MutableLiveData<Boolean>().apply { value = false }

    var somethingWentWrongEvent = SingleLiveEvent<Any>()
    val accountDeletedEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val userUpdateEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val user = MutableLiveData<User>().apply { value = User() }
    var initialUser = MutableLiveData<User>().apply { value = User() }

    init {
        getUserFromDB()
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
        preferencesService.clearUserDetails()
        userRepository.clearUserData()
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
                })
        }
    }

    fun cancelUserDetailsChanges() {
        user.value = initialUser.value
    }

    private fun handleDoOnError(it: Throwable) {
        Timber.e(it)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}