package com.fivedevs.auxby.screens.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.data.prefs.PreferencesService.Companion.LANGUAGE_APP_SELECTED
import com.fivedevs.auxby.domain.models.LanguageModel
import com.fivedevs.auxby.domain.models.enums.LanguagesCodeEnum
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.disposeBy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userApi: UserApi,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val preferencesService: PreferencesService,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val languageApp = MutableLiveData<String>()
    val languageChanged = SingleLiveEvent<Any>()
    val errorUpdateNewsLetter = SingleLiveEvent<Any>()
    val languagesList = MutableLiveData<List<LanguageModel>>()
    val languageCodeSelected = MutableLiveData<LanguageModel>()
    val user = MutableLiveData<User>().apply { value = User() }
    val showEditLanguage = MutableLiveData<Boolean>().apply { value = false }
    val showEditNotifications = MutableLiveData<Boolean>().apply { value = false }
    val newsletterStatus = MutableLiveData<Boolean>().apply { value = true }

    val languageSelectedPublishSubject: PublishSubject<LanguageModel> = PublishSubject.create()

    init {
        onLanguageClicked()
        getUser()
    }

    fun changeEditLanguageVisibility() {
        showEditLanguage.value?.let {
            showEditLanguage.value = !it
        }
    }

    fun changeEditNotificationsVisibility() {
        showEditNotifications.value?.let {
            showEditNotifications.value = !it
        }
    }

    fun setAppLanguage(deviceLanguageCode: String) {
        val savedLanguage = getSavedLanguageCode()
        val language = if (!savedLanguage.isNullOrEmpty()) {
            savedLanguage
        } else {
            getRightSelectedLanguage(deviceLanguageCode)
        }

        languageApp.value = language.uppercase()
    }

    fun createLanguagesList(languageCodeSelected: String) {
        val localLanguages = LanguagesCodeEnum.values()
        val correctLanguageSelected = getRightSelectedLanguage(languageCodeSelected)
        val languages = localLanguages.map { language ->
            LanguageModel().apply {
                codeEnum = language
                isSelected = language.name.equals(correctLanguageSelected, true)
            }
        }
        languagesList.value = languages
        this.languageCodeSelected.value = languages.first { it.isSelected }
    }

    fun changeNewsletterStatus() {
        userApi.changeNewsletterStatus()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                newsletterStatus.value != newsletterStatus.value
            }, {
                errorUpdateNewsLetter.call()
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    fun getUser() {
        userApi.getUser()
            .subscribeOn(rxSchedulers.network())
            .observeOn(rxSchedulers.background())
            .doOnNext { userRepository.insertUser(it) }
            .observeOn(rxSchedulers.androidUI())
            .subscribe({
                user.value = it
                newsletterStatus.value = it.isSubscribedToNewsletter
            }, {
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun getRightSelectedLanguage(deviceLanguageCode: String): String {
        val languages = LanguagesCodeEnum.values()
        return if (languages.any { it.name.equals(deviceLanguageCode, true) }) {
            deviceLanguageCode
        } else {
            LanguagesCodeEnum.EN.name
        }
    }

    private fun getSavedLanguageCode() = preferencesService.getSelectedLanguageApp()

    private fun onLanguageClicked() {
        languageSelectedPublishSubject
            .observeOn(rxSchedulers.androidUI())
            .subscribe({ language ->
                handleLanguageSelected(language)
            }, {
                Timber.e(it)
            })
            .disposeBy(compositeDisposable)
    }

    private fun handleLanguageSelected(language: LanguageModel) {
        if (!preferencesService.getSelectedLanguageApp().equals(language.codeEnum.name, true)) {
            preferencesService.setValue(LANGUAGE_APP_SELECTED, language.codeEnum.name)
            languageChanged.value = language
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}