package com.fivedevs.auxby.screens.profile.userActions

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.DialogUserActionsBinding
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.domain.utils.views.LoaderDialog
import com.fivedevs.auxby.screens.dashboard.DashboardActivity
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActionsDialog : DialogFragment() {

    private lateinit var binding: DialogUserActionsBinding
    private val viewModel by viewModels<UserActionsViewModel>()

    private val loaderDialog: LoaderDialog by lazy { LoaderDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_user_actions, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setGravity(Gravity.CENTER_VERTICAL)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            isCancelable = true
        }
    }

    private fun initListeners() {
        binding.btnLogout.setOnClickListener {
            val logoutDialog = GenericDialog(::logoutUser, DialogTypes.LOG_OUT)
            logoutDialog.show(requireActivity().supportFragmentManager, GenericDialog.LOGOUT_DIALOG_TAG)
        }

        binding.btnDeleteAccount.setOnClickListener {
            val deleteAccountDialog = GenericDialog(::onDeleteAccountClicked, DialogTypes.DELETE_ACCOUNT)
            deleteAccountDialog.show(requireActivity().supportFragmentManager, GenericDialog.DELETE_ACCOUNT_DIALOG_TAG)
        }

        binding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        binding.container.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun initObservers() {
        viewModel.accountDeletedEvent.observe(this) {
            loaderDialog.dismissDialog()
            logoutUser()
        }

        viewModel.somethingWentWrongEvent.observe(this) {
            showErrorMessage()
        }

        viewModel.userLogoutEvent.observe(this) { isSuccess ->
            if (isSuccess) {
                requireActivity().launchActivity<DashboardActivity>()
                dismiss()
            } else {
                toast(getString(R.string.something_went_wrong))
            }
        }
    }

    private fun logoutUser() {
        viewModel.logoutUser()
    }

    private fun onDeleteAccountClicked() {
        loaderDialog.showDialog()
        viewModel.deleteUserAccount()
    }

    private fun showErrorMessage() {
        loaderDialog.dismissDialog()
        AlerterUtils.showErrorAlert(requireActivity(), resources.getString(R.string.something_went_wrong))
    }
}