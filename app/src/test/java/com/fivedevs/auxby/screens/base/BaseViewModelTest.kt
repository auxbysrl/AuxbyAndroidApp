package com.fivedevs.auxby.screens.base


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.domain.utils.rx.TestRxSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.junit.Rule
import org.mockito.Mockito


open class BaseViewModelTest {

    val testRxSchedulers = TestRxSchedulers() as RxSchedulers
    val userRepository: UserRepository = Mockito.mock(UserRepository::class.java)
    val preferencesService: PreferencesService = Mockito.mock(PreferencesService::class.java)
    var compositeDisposable: CompositeDisposable = Mockito.mock(CompositeDisposable::class.java)


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

}