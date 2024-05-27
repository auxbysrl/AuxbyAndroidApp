package com.fivedevs.auxby.screens.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivitySplashBinding
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.fivedevs.auxby.screens.onBoarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity() : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.apply {
            viewModel = this@SplashActivity.viewModel
            lifecycleOwner = this@SplashActivity
        }
    }

    private fun initObservers() {
        viewModel.onDashboardNavigation.observe(this) {
            launchActivityWithFinish<DashboardActivity>()
        }

        viewModel.onOnBoardingNavigation.observe(this) {
            launchActivityWithFinish<OnBoardingActivity>()
        }
    }
}