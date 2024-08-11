package com.fivedevs.auxby.screens.authentification.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentCheckEmailBinding
import com.fivedevs.auxby.domain.utils.Utils
import com.fivedevs.auxby.domain.utils.extensions.htmlText
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.authentification.base.BaseUserViewModel.Companion.CHECK_CONFIRMATION_EMAIL
import com.fivedevs.auxby.screens.dashboard.DashboardActivity


class CheckEmailFragment(viewModel: BaseUserViewModel) : Fragment() {

    private lateinit var binding: FragmentCheckEmailBinding
    private var parentViewModel: BaseUserViewModel = viewModel

    private val confirmationEmailHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_check_email, container, false)
        binding.viewModel = parentViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSubtitleText()
        initListeners()
        initObservers()
        checkEmailConfirmation()
    }

    private fun setSubtitleText() {
        parentViewModel.userRequest.value?.email?.let {
            binding.tvCheckEmailSubTitle.htmlText(getString(R.string.confirmation_link_sent_to, it))
        }
    }

    private fun initListeners() {
        binding.btnOpenMailApp.setOnClickListenerWithDelay {
            context?.let {
                Utils.showEmailChooser(it)
            }
        }
    }

    private fun initObservers() {
        parentViewModel.emailCheckEvent.observe(viewLifecycleOwner) { isEmailChecked ->
            if (isEmailChecked) {
                parentViewModel.getUser()
                requireActivity().launchActivity<DashboardActivity> { }
                requireActivity().finishAffinity()
            } else {
                checkEmailConfirmation()
            }
        }

        parentViewModel.resendEmailEvent.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                showSuccessMessage()
            } else {
                showErrorMessage()
            }
        }
    }

    private fun showSuccessMessage() {
        AlerterUtils.showSuccessAlert(requireActivity(), getString(R.string.new_mail_has_been_sent))
    }

    private fun showErrorMessage() {
        AlerterUtils.showErrorAlert(requireActivity(), resources.getString(R.string.something_went_wrong))
    }

    private fun checkEmailConfirmation() {
        confirmationEmailHandler.postDelayed({
            parentViewModel.checkConfirmationEmail()
        }, CHECK_CONFIRMATION_EMAIL)
    }

    override fun onDestroy() {
        super.onDestroy()
        confirmationEmailHandler.removeCallbacksAndMessages(null)
    }

}