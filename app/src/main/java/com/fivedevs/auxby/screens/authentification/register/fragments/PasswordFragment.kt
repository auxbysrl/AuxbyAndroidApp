package com.fivedevs.auxby.screens.authentification.register.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentPasswordBinding
import com.fivedevs.auxby.domain.utils.Validator
import com.fivedevs.auxby.domain.utils.buttonAnimator.TransitionButton
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setTint
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.authentification.register.RegisterViewModel
import com.tapadoo.alerter.Alerter

class PasswordFragment : Fragment() {

    private lateinit var binding: FragmentPasswordBinding
    private val parentViewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_password, container, false)
        binding.viewModel = parentViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInputFieldListeners()
        initListeners()
        initObservers()
    }

    private fun initObservers() {
        parentViewModel.signUpEvent.observe(viewLifecycleOwner) { signUpMsg ->
            handleSignUpEvent(signUpMsg)
        }
    }

    private fun handleSignUpEvent(signUpMsg: String?) {
        if (signUpMsg.isNullOrEmpty()) {
            binding.btnSignUp.stopAnimation(
                TransitionButton.StopAnimationStyle.EXPAND,
                object : TransitionButton.OnAnimationStopEndListener {
                    override fun onAnimationStopEnd() {
                        parentViewModel.showCheckEmailScreenEvent.call()
                    }
                })
        } else {
            showErrorMessage()
        }
    }

    private fun showErrorMessage() {
        AlerterUtils.showErrorAlert(requireActivity(), resources.getString(R.string.something_went_wrong))
        binding.btnSignUp.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE)
    }

    private fun initListeners() {
        binding.btnSignUp.setOnClickListenerWithDelay {
            binding.btnSignUp.startAnimation()
            parentViewModel.registerUser()
            parentViewModel.registerUser()
            Alerter.hide()
        }
    }

    private fun initInputFieldListeners() {
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            if (!Validator.validatePassword(text.toString())) {
                binding.tilPassword.error = " "
                binding.tilPassword.errorIconDrawable = null
                parentViewModel.enableSignUpBtn.value = false
                handleMessagePwdError(true)
            } else {
                binding.tilPassword.error = null
                binding.ivInfoIcon.setTint(R.color.dark_text)
                binding.tvPasswordHasToInclude.text = getString(R.string.your_password_has_to_include)
                enableSignUpBtn()
            }
        }

        binding.etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            if (!Validator.validatePassword(text.toString())) {
                parentViewModel.enableSignUpBtn.value = false
                handleConfirmPwdError()
                handleMessagePwdError(true)
            } else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
                handleConfirmPwdError()
                handleMessagePwdError(false)
            } else {
                handleConfirmPwdValid()
                handleMessagePwdValid()
                enableSignUpBtn()
            }
        }

        binding.checkBoxTermsConditions.setOnCheckedChangeListener { _, isChecked ->
            parentViewModel.isTermsChecked.value = isChecked
            enableSignUpBtn()
        }
    }

    private fun enableSignUpBtn() {
        if (binding.etConfirmPassword.text.toString().isEmpty()) {
            if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
                handleConfirmPwdError()
                handleMessagePwdError(false)
            } else {
                handleConfirmPwdValid()
                handleMessagePwdValid()
            }
        }

        parentViewModel.enableSignUpBtn.value =
            binding.checkBoxTermsConditions.isChecked && Validator.validatePassword(binding.etPassword.text.toString()) && Validator.validatePassword(
                binding.etConfirmPassword.text.toString()
            ) && binding.etPassword.text.toString() == binding.etConfirmPassword.text.toString()
    }

    private fun handleConfirmPwdError() {
        binding.tilConfirmPassword.error = " "
        binding.tilConfirmPassword.errorIconDrawable = null
    }

    private fun handleConfirmPwdValid() {
        binding.tilConfirmPassword.error = null
    }

    private fun handleMessagePwdError(isInvalidMsg: Boolean) {
        binding.ivInfoIcon.setTint(R.color.red)
        binding.tvPasswordHasToInclude.text = if (isInvalidMsg) getString(R.string.your_password_has_to_include)
        else getString(R.string.passwords_should_the_same)
    }

    private fun handleMessagePwdValid() {
        binding.ivInfoIcon.setTint(R.color.green)
        binding.tvPasswordHasToInclude.text = getString(R.string.password_is_valid)
    }
}