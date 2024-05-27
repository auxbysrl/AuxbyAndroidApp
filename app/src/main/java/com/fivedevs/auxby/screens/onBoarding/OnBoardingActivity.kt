package com.fivedevs.auxby.screens.onBoarding

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityOnBoardingBinding
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.authentification.register.RegisterActivity
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.fivedevs.auxby.screens.onBoarding.onBoardingView.adapter.OnBoardingAdapter
import com.fivedevs.auxby.screens.onBoarding.onBoardingView.model.OnBoardingElement
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private val viewModel by viewModels<OnBoardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        populateOnBoardingView()
        initListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding)
        binding.apply {
            viewModel = this@OnBoardingActivity.viewModel
            lifecycleOwner = this@OnBoardingActivity
        }
    }

    private fun initListeners() {
        with(binding) {
            skipButton.setOnClickListenerWithDelay {
                viewModel?.saveOnBoardingStatus()
                launchActivityWithFinish<DashboardActivity>()
            }

            joinButton.setOnClickListenerWithDelay {
                launchActivityWithFinish<LoginActivity>()
            }

            loginButton.setOnClickListenerWithDelay {
                viewModel?.saveOnBoardingStatus()
                launchActivityWithFinish<LoginActivity>()
            }
        }
    }

    private fun populateOnBoardingView() {
        initOnBoardingAdapter(viewModel.getOnBoardingElements())
    }

    private fun initOnBoardingAdapter(onBoardingElements: MutableList<OnBoardingElement>) {
        val onBoardingElementAdapter = OnBoardingAdapter(onBoardingElements)
        binding.onBoardingViewPager.adapter = onBoardingElementAdapter
        binding.circleIndicator.attachTo(binding.onBoardingViewPager)

        initViewPagerListener(onBoardingElements.size.dec())
    }

    private fun initViewPagerListener(size: Int) {
        binding.onBoardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == size) {
                    viewModel.saveOnBoardingStatus()
                    viewModel.shouldShowJoinBt.value = true
                }
            }
        })
    }

}