package com.fivedevs.auxby.screens.forgotPassword

import android.os.Bundle
import android.util.Patterns
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityForgotPasswordBinding
import com.fivedevs.auxby.domain.utils.Utils.showEmailChooser
import com.fivedevs.auxby.domain.utils.buttonAnimator.TransitionButton
import com.fivedevs.auxby.domain.utils.extensions.hideKeyboard
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.views.AlerterUtils.showErrorAlert
import com.fivedevs.auxby.domain.utils.views.AlerterUtils.showSuccessAlert
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPassActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel by viewModels<ForgotPassViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initInputFieldListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        binding.apply {
            viewModel = this@ForgotPassActivity.viewModel
            lifecycleOwner = this@ForgotPassActivity
        }
    }

    private fun initObservers() {
        viewModel.backToLoginEvent.observe(this) {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel.onCloseToOffersEvent.observe(this) {
            launchActivity<DashboardActivity> { }
            finishAffinity()
        }

        viewModel.sendResetLinkEvent.observe(this) {
            sendResetLink()
        }

        viewModel.resetLinkResponse.observe(this) { errorMessage ->
            if (errorMessage.isEmpty()) {
                showSuccessMessage()
            } else {
                showErrorMessage()
            }
        }
    }

    private fun initInputFieldListeners() {
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isEmpty()) {
                binding.tilEmail.error = getString(R.string.cannot_be_empty)
                viewModel.enableResetLink.value = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                binding.tilEmail.error = getString(R.string.invalid_email_address)
                viewModel.enableResetLink.value = false
            } else {
                binding.tilEmail.error = null
                viewModel.enableResetLink.value = true
            }
        }
    }

    private fun sendResetLink() {
        binding.btnSendResetLink.startAnimation()
        binding.clForgotPassParent.hideKeyboard()
        viewModel.sendResetLink(binding.etEmail.text.toString().trim())
    }

    private fun showSuccessMessage() {
        binding.btnSendResetLink.stopAnimation(TransitionButton.StopAnimationStyle.SUCCESS, null)
        showSuccessAlert(this, getString(R.string.reset_link_sent), ::openEmailChooserIntent)
    }

    private fun showErrorMessage() {
        showErrorAlert(this, resources.getString(R.string.something_went_wrong))
        binding.btnSendResetLink.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null)
    }

    private fun openEmailChooserIntent() {
        showEmailChooser(this)
    }
}