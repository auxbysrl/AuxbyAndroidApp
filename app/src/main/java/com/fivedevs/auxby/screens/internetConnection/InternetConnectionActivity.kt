package com.fivedevs.auxby.screens.internetConnection

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityNoInternetBinding
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.screens.base.BaseActivity

class InternetConnectionActivity : BaseActivity() {

    private lateinit var binding: ActivityNoInternetBinding
    private val viewModel by viewModels<InternetConnectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.networkConnectionState.observe(this) {
            if (it) {
                viewModel.sendDataRefreshEvent()
                finish()
            }
        }
    }

    private fun initListeners() {
        binding.btnTryAgain.setOnClickListenerWithDelay { }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        })
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_no_internet)
        binding.apply {
            lifecycleOwner = this@InternetConnectionActivity
        }
    }
}