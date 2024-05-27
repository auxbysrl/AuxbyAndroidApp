package com.fivedevs.auxby.screens.onBoarding

import androidx.lifecycle.ViewModel
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.data.prefs.PreferencesService.Companion.ON_BOARDING_KEY
import com.fivedevs.auxby.domain.SingleLiveEvent
import com.fivedevs.auxby.screens.onBoarding.onBoardingView.model.OnBoardingElement
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val preferencesService: PreferencesService
) : ViewModel() {

    val shouldShowJoinBt = SingleLiveEvent<Boolean>().apply { value = false }

    fun getOnBoardingElements(): MutableList<OnBoardingElement> {
        return mutableListOf<OnBoardingElement>().apply {
            add(
                OnBoardingElement(
                    R.drawable.ic_onboarding_1,
                    R.string.onboarding_title_1,
                    R.string.onboarding_body_1
                )
            )
            add(
                OnBoardingElement(
                    R.drawable.ic_onboarding_2,
                    R.string.onboarding_title_2,
                    R.string.onboarding_body_2
                )
            )
            add(
                OnBoardingElement(
                    R.drawable.ic_onboarding_3,
                    R.string.onboarding_title_3,
                    R.string.onboarding_body_3
                )
            )
        }
    }

    fun saveOnBoardingStatus() {
        preferencesService.setValue(ON_BOARDING_KEY, true)
    }
}