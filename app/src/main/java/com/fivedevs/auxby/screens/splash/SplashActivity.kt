package com.fivedevs.auxby.screens.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivitySplashBinding
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.onBoarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import io.branch.referral.Branch
import timber.log.Timber

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

    override fun onStart() {
        super.onStart()
        initializeBranchSession()
    }

    private fun initializeBranchSession() {
        Branch.sessionBuilder(this).withCallback { _, linkProperties, error ->
            if (error != null) {
                Timber.e("Branch - branch init failed. Caused by -%s", error.message)
            } else {

                linkProperties?.controlParams?.let { params ->
                    params["\$offerId"]?.let { offerId ->
                        Timber.i("Branch - offerId $offerId")
                        launchActivity<OfferDetailsActivity> {
                            putExtra(Constants.SELECTED_OFFER_ID, offerId.toLong())
                        }
                    }

                    params["\$userId"]?.let { userId ->
                        Timber.i("Branch - userId $userId")
                        if (viewModel.isUserLoggedIn.value == false) {
                            launchActivity<LoginActivity> {
                                putExtra(Constants.USER_REFERRAL_ID, userId)
                            }
                        }
                    } ?: kotlin.run { Timber.e("Branch - params not found") }
                }
            }
        }.withData(this.intent.data).init()
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