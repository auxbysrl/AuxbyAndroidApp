package com.fivedevs.auxby.screens.settings

import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.domain.models.LanguageModel
import com.fivedevs.auxby.domain.models.enums.LanguagesCodeEnum
import com.fivedevs.auxby.screens.base.BaseViewModelTest
import com.google.common.truth.Truth.assertThat
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.reflect.jvm.isAccessible

class SettingsViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Mockito.`when`(userRepository.getCurrentUser())
            .thenReturn(Observable.just(User()))

        viewModel = SettingsViewModel(testRxSchedulers, userRepository, preferencesService, compositeDisposable)
    }

    @Test
    fun `test set app language should return default`() {
        viewModel.setAppLanguage("ge")

        assertThat(viewModel.languageApp.value).isEqualTo("EN")
    }

    @Test
    fun `test set app language should return ro`() {
        viewModel.setAppLanguage("Ro")

        assertThat(viewModel.languageApp.value).isEqualTo("RO")
    }

    @Test
    fun `test set app language should return fr`() {
        Mockito.`when`(preferencesService.getSelectedLanguageApp())
            .thenReturn("FR")

        viewModel.setAppLanguage("ro")

        assertThat(viewModel.languageApp.value).isEqualTo("FR")
    }

    @Test
    fun `test set app language empty string should return default`() {
        Mockito.`when`(preferencesService.getSelectedLanguageApp())
            .thenReturn("")

        viewModel.setAppLanguage("")

        assertThat(viewModel.languageApp.value).isEqualTo("EN")
    }

    @Test
    fun `test create languages list selected fr language should check default language`() {
        viewModel.createLanguagesList("fr")

        assertThat(viewModel.languagesList.value!!.first { it.codeEnum.name == "EN" }.isSelected).isTrue()
    }

    @Test
    fun `test create languages list selected ro language should check ro language`() {
        viewModel.createLanguagesList("ro")

        assertThat(viewModel.languagesList.value!!.first { it.codeEnum.name == "RO" }.isSelected).isTrue()
    }

    @Test
    fun `test create languages list empty string should return default`() {
        viewModel.createLanguagesList("")

        assertThat(viewModel.languagesList.value!!.first { it.codeEnum.name == "EN" }.isSelected).isTrue()
    }

    @Test
    fun `test on language selected should update local language`() {
        Mockito.`when`(
            preferencesService.getSelectedLanguageApp()
        ).thenReturn("ro")
        viewModel.createLanguagesList("en")
        viewModel.languageSelectedPublishSubject.onNext(LanguageModel(codeEnum = LanguagesCodeEnum.EN))

        assertThat(viewModel.languageCodeSelected.value!!.codeEnum.name).isEqualTo("EN")
    }

    @Test
    fun `test on language selected should call error`() {
        viewModel.languageSelectedPublishSubject.onError(Exception("t"))

        assertThat(viewModel.languageCodeSelected.value).isNull()
    }

    @Test
    fun `test get local user should update local data`() {
        Mockito.`when`(userRepository.getCurrentUser())
            .thenReturn(Observable.just(User(email = "test@test.ro")))
       val viewModel = SettingsViewModel(testRxSchedulers, userRepository, preferencesService, compositeDisposable)

        assertThat(viewModel.user.value!!.email).isEqualTo("test@test.ro")
    }

    @Test
    fun `test get local user should call error`() {
        Mockito.`when`(userRepository.getCurrentUser())
            .thenReturn(Observable.error(Exception("t")))
        val viewModel = SettingsViewModel(testRxSchedulers, userRepository, preferencesService, compositeDisposable)

        assertThat(viewModel.user.value!!.email).isEmpty()
    }

    @Test
    fun `test onCleared calls`() {
        viewModel::class.members
            .single { it.name == "onCleared" }
            .apply { isAccessible = true }
            .call(viewModel)

        Mockito.verify(compositeDisposable, Mockito.times(1)).clear()
    }
}