package com.fivedevs.auxby.screens.changePassword

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityChangePasswordBinding
import com.fivedevs.auxby.domain.utils.Validator
import com.fivedevs.auxby.domain.utils.buttonAnimator.TransitionButton
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setTint
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.base.BaseActivity
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel by viewModels<ChangePasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initListeners()
        initInputFieldListeners()
    }

    private fun initListeners() {
        binding.btnConfirmPwd.setOnClickListenerWithDelay {
            binding.btnConfirmPwd.startAnimation()
            viewModel.changePassword()
        }
    }

    private fun initObservers() {
        viewModel.returnToProfile.observe(this) { isSuccess ->
            binding.btnConfirmPwd.stopAnimation(TransitionButton.StopAnimationStyle.SUCCESS)
            showMessage(isSuccess)
        }

        viewModel.onBackPressed.observe(this) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showMessage(success: Boolean) {
        if (success) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            AlerterUtils.showErrorAlert(this, getString(R.string.password_change_error))
        }
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.apply {
            viewModel = this@ChangePasswordActivity.viewModel
            lifecycleOwner = this@ChangePasswordActivity
        }
    }

    private fun initInputFieldListeners() {
        binding.etOldPassword.doOnTextChanged { text, _, _, _ ->
            if (!Validator.validatePassword(text.toString())) {
                showErrorState(binding.tilOldPassword)
            } else {
                binding.tilOldPassword.error = null
                enableSignUpBtn()
            }
        }

        binding.etNewPassword.doOnTextChanged { text, _, _, _ ->
            if (!Validator.validatePassword(text.toString())) {
                showErrorState(binding.tilNewPassword)
                handleMessagePwdError(true, getString(R.string.your_password_has_to_include))
            } else if(text.toString() == binding.etOldPassword.text.toString()) {
                showErrorState(binding.tilNewPassword)
                handleMessagePwdError(true, getString(R.string.your_new_pwd_cannot_be_the_same))
            } else {
                binding.tilNewPassword.error = null
                binding.ivInfoIcon.setTint(R.color.dark_text)
                binding.tvPasswordHasToInclude.text = getString(R.string.your_password_has_to_include)
                enableSignUpBtn()
            }
        }

        binding.etConfirmNewPassword.doOnTextChanged { text, _, _, _ ->
            if (!Validator.validatePassword(text.toString())) {
                viewModel.enableConfirmPwdBtn.value = false
                handleConfirmPwdError()
                handleMessagePwdError(true, getString(R.string.your_password_has_to_include))
            } else if (binding.etNewPassword.text.toString() != binding.etConfirmNewPassword.text.toString()) {
                handleConfirmPwdError()
                handleMessagePwdError(false, getString(R.string.your_password_has_to_include))
            } else {
                handleConfirmPwdValid()
                handleMessagePwdValid()
                enableSignUpBtn()
            }
        }
    }

    private fun enableSignUpBtn() {
        if (binding.etConfirmNewPassword.text.toString().isNotEmpty()) {
            if (binding.etNewPassword.text.toString() != binding.etConfirmNewPassword.text.toString()) {
                handleConfirmPwdError()
                handleMessagePwdError(false, getString(R.string.your_password_has_to_include))
            } else {
                handleConfirmPwdValid()
                handleMessagePwdValid()
            }
        }

        viewModel.enableConfirmPwdBtn.value =
            Validator.validatePassword(binding.etOldPassword.text.toString()) &&
            Validator.validatePassword(binding.etNewPassword.text.toString()) &&
                    Validator.validatePassword(binding.etConfirmNewPassword.text.toString()) &&
                    binding.etNewPassword.text.toString() == binding.etConfirmNewPassword.text.toString()
    }

    private fun showErrorState(textInputLayout: TextInputLayout) {
        textInputLayout.error = " "
        textInputLayout.errorIconDrawable = null
        viewModel.enableConfirmPwdBtn.value = false
    }

    private fun handleConfirmPwdError() {
        binding.tilConfirmNewPassword.error = " "
        binding.tilConfirmNewPassword.errorIconDrawable = null
    }

    private fun handleConfirmPwdValid() {
        binding.tilConfirmNewPassword.error = null
    }

    private fun handleMessagePwdError(isInvalidMsg: Boolean, errorMsg: String) {
        binding.ivInfoIcon.setTint(R.color.red)
        binding.tvPasswordHasToInclude.text = if (isInvalidMsg) errorMsg
        else getString(R.string.passwords_should_the_same)
    }

    private fun handleMessagePwdValid() {
        binding.ivInfoIcon.setTint(R.color.green)
        binding.tvPasswordHasToInclude.text = getString(R.string.password_is_valid)
    }
}