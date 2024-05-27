package com.fivedevs.auxby.screens.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.domain.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesService: PreferencesService
) : ViewModel() {

    val onDashboardNavigation = SingleLiveEvent<Any>()
    val onOnBoardingNavigation = SingleLiveEvent<Any>()

    private var handler: Handler = Handler(Looper.getMainLooper())

    init {
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
        DateUtils.currentLocale = Locale(preferencesService.getSelectedLanguageApp().lowercase(), preferencesService.getSelectedLanguageApp().uppercase())
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SPLASH_DELAY = 3000L
    }
}