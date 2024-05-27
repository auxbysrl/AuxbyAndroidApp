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
import com.fivedevs.auxby.databinding.FragmentPersonalDetailsBinding
import com.fivedevs.auxby.domain.utils.Validator.validateEmailField
import com.fivedevs.auxby.domain.utils.Validator.validateOptionalTextField
import com.fivedevs.auxby.domain.utils.Validator.validatePhoneField
import com.fivedevs.auxby.domain.utils.Validator.validateTextField
import com.fivedevs.auxby.domain.utils.buttonAnimator.TransitionButton
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.screens.authentification.register.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout
import com.tapadoo.alerter.Alerter

class PersonalDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPersonalDetailsBinding
    private val parentViewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_details, container, false)
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

    private fun initInputFieldListeners() {
        binding.etFirstName.doOnTextChanged { text, _, _, _ ->
            binding.tilFirstName.error = validateTextField(text?.trim().toString(), requireContext())
            enableNextButton()
        }

        binding.etLastName.doOnTextChanged { text, _, _, _ ->
            binding.tilLastName.error = validateTextField(text?.trim().toString(), requireContext())
            enableNextButton()
        }

        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            binding.tilEmail.error = validateEmailField(text?.trim().toString(), requireContext())
            enableNextButton()
        }

        binding.etPhone.doOnTextChanged { text, _, _, _ ->
            binding.tilPhone.error = validatePhoneField(text.toString(), requireContext())
            enableNextButton()
        }

        binding.etCountry.doOnTextChanged { text, _, _, _ ->
            binding.tilCountry.error = validateOptionalTextField(text.toString(), requireContext())
            enableNextButton()
        }

        binding.etCity.doOnTextChanged { text, _, _, _ ->
            binding.tilCity.error = validateOptionalTextField(text.toString(), requireContext())
            enableNextButton()
        }
    }

    private fun initObservers() {
        parentViewModel.userExistsEvent.observe(viewLifecycleOwner) {
            binding.tilEmail.error = getString(R.string.text_user_already_exists)
            binding.btnNext.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE)
        }
    }

    private fun initListeners() {
        binding.btnNext.setOnClickListenerWithDelay {
            binding.btnNext.startAnimation()
            parentViewModel.checkEmailExists()
            Alerter.hide()
        }
    }

    private fun enableNextButton() {
        parentViewModel.enableNextBtn.value = fieldIsValid(binding.tilFirstName) &&
                fieldIsValid(binding.tilLastName) &&
                fieldIsValid(binding.tilEmail) &&
                fieldIsValid(binding.tilPhone) &&
                binding.tilCountry.error.isNullOrEmpty() &&
                binding.tilCity.error.isNullOrEmpty()
    }

    private fun fieldIsValid(textInput: TextInputLayout): Boolean {
        return textInput.error == null && textInput.editText?.text.toString().isNotEmpty()
    }
}