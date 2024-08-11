package com.fivedevs.auxby.screens.tutorials.guestMode

import androidx.databinding.ViewDataBinding
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.DialogGuestModeBinding
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog

class GuestModeDialog(
    private val descriptionText: String
) : BaseBottomSheetDialog(R.layout.dialog_guest_mode, true) {

    private lateinit var root: DialogGuestModeBinding

    override fun getContentView(binding: ViewDataBinding) {
        root = binding as DialogGuestModeBinding

        root.tvCancel.setOnClickListenerWithDelay {
            dismiss()
        }

        root.joinButton.setOnClickListenerWithDelay {
            requireContext().launchActivity<LoginActivity> { }
            dismiss()
        }

        root.loginGuestButton.setOnClickListenerWithDelay {
            requireContext().launchActivity<LoginActivity> { }
            dismiss()
        }

        root.tvDescription.text = descriptionText
    }
}