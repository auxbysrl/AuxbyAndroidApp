package com.fivedevs.auxby.screens.authentification.register

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityRegisterBinding
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.replace
import com.fivedevs.auxby.domain.utils.extensions.replaceBackStack
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.authentification.base.CheckEmailFragment
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.authentification.register.fragments.PasswordFragment
import com.fivedevs.auxby.screens.authentification.register.fragments.PersonalDetailsFragment
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        getIntentData()
        showAccountDetailsFragment(savedInstanceState)
        initObservers()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.apply {
            viewModel = this@RegisterActivity.viewModel
            lifecycleOwner = this@RegisterActivity
        }
    }

    private fun getIntentData() {
        intent.getStringExtra(Constants.USER_REFERRAL_ID)?.toIntOrNull().let { userId ->
            viewModel.userRequest.value?.userReferralId = userId
        }
    }

    private fun showAccountDetailsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            replace(PersonalDetailsFragment(), binding.fragmentContainerView.id)
        }
    }

    private fun initObservers() {
        viewModel.exitClickEvent.observe(this) {
            launchActivityWithFinish<DashboardActivity> {}
        }

        viewModel.loginClickEvent.observe(this) {
            launchActivityWithFinish<LoginActivity> {}
        }

        viewModel.backClickEvent.observe(this) {
            supportFragmentManager.popBackStack()
            viewModel.showBackArrow.value = false
        }

        viewModel.nextScreenEvent.observe(this) { msg ->
            if (msg.isNullOrEmpty()) {
                replaceBackStack(PasswordFragment(), binding.fragmentContainerView.id)
                viewModel.showBackArrow.value = true
            } else {
                AlerterUtils.showErrorAlert(this, resources.getString(R.string.something_went_wrong))
            }
        }

        viewModel.showCheckEmailScreenEvent.observe(this) {
            replaceBackStack(CheckEmailFragment(viewModel), binding.fragmentContainerView.id)
            viewModel.showBackArrow.value = false
        }
    }
}