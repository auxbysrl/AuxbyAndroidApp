package com.fivedevs.auxby.screens.splash

import android.os.Handler
import android.os.Looper
import com.fivedevs.auxby.data.api.UserApi
import com.fivedevs.auxby.data.database.repositories.UserRepository
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.rx.RxSchedulers
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesService: PreferencesService,
    compositeDisposable: CompositeDisposable,
    userApi: UserApi,
    userRepository: UserRepository,
    rxSchedulers: RxSchedulers
) : BaseUserViewModel(userApi, rxSchedulers, userRepository, compositeDisposable, preferencesService) {

    val onDashboardNavigation = SingleLiveEvent<Any>()
    val onOnBoardingNavigation = SingleLiveEvent<Any>()

    private var handler: Handler = Handler(Looper.getMainLooper())

    init {
        getUser()
        getOnBoardingStatus()
        setCurrentLocale()
    }

    private fun getOnBoardingStatus() {
        initSplashTimer(preferencesService.getOnBoardingStatus())
    }

    private fun initSplashTimer(onBoardingStatus: Boolean) {
        handler.postDelayed({
            if (!onBoardingStatus) onOnBoardingNavigation.call()
            else onDashboardNavigation.call()
        }, SPLASH_DELAY)
    }

    private fun setCurrentLocale() {
        DateUtils.currentLocale =
            Locale(preferencesService.getSelectedLanguageApp().lowercase(), preferencesService.getSelectedLanguageApp().uppercase())
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SPLASH_DELAY = 3000L
    }
}