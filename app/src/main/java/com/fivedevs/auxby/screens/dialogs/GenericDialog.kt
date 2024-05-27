package com.fivedevs.auxby.screens.dialogs

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.DialogGenericBinding
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.utils.extensions.getColorCompat
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.show

class GenericDialog(
    private var confirmListener: (() -> Unit?)?,
    private var dialogType: DialogTypes
) : DialogFragment() {

    private lateinit var binding: DialogGenericBinding
    private var availableCoins = 0
    private var addOfferCost = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_generic, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customizeDialog()
        initListeners()
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
            isCancelable = false
        }
    }

    private fun customizeDialog() {
        when (dialogType) {
            DialogTypes.DEFAULT -> {

            }
            DialogTypes.LOG_OUT -> {
                showLogOutState()
            }
            DialogTypes.DELETE_ACCOUNT -> {
                showDeleteAccountState()
            }
            DialogTypes.PUBLISH_OFFER -> {
                showPublishOfferState(hasCoins = true)
            }
            DialogTypes.PUBLISH_OFFER_NO_COINS -> {
                showPublishOfferState(hasCoins = false)
            }
            DialogTypes.CLOSE_AUCTION -> {
                showCloseAuctionState()
            }
            DialogTypes.DELETE_OFFER -> {
                showDeleteOfferState()
            }
            DialogTypes.CONTACT_US -> {
                showContactUsState()
            }
            DialogTypes.ENABLE_OFFER -> {
                showEnableOfferState()
            }
            DialogTypes.DISABLE_OFFER -> {
                showDisableOfferState()
            }
        }
    }

    private fun showDisableOfferState() {
        binding.tvTittle.text = resources.getString(R.string.disable_offer)
        binding.tvTittle.setTextColor(requireContext().getColorCompat(R.color.red))
        binding.tvDescription.text = resources.getString(R.string.disable_offer_description)
        binding.btnConfirm.setBackgroundColor(requireContext().getColorCompat(R.color.red))
    }

    private fun showEnableOfferState() {
        binding.tvTittle.text = resources.getString(R.string.enable_offer)
        binding.tvDescription.text = resources.getString(R.string.enable_offer_description)
        binding.tvCoinsBalance.text =  resources.getString(R.string.coins_balance, availableCoins.toString())
        binding.tvCoinsBalance.show()
    }

    private fun showContactUsState() {
        binding.tvTittle.hide()
        binding.btnCancel.hide()
        binding.tvDescription.text = resources.getString(R.string.contact_us_need_help)
        binding.btnConfirm.text = resources.getString(R.string.ok)
    }

    private fun showDeleteOfferState() {
        binding.tvTittle.text = resources.getString(R.string.delete_offer)
        binding.tvTittle.setTextColor(requireContext().getColorCompat(R.color.red))
        binding.tvDescription.text = resources.getString(R.string.delete_offer_description)
        binding.btnConfirm.setBackgroundColor(requireContext().getColorCompat(R.color.red))
    }

    private fun showCloseAuctionState() {
        binding.tvTittle.text = resources.getString(R.string.close_auction)
        binding.tvTittle.setTextColor(requireContext().getColorCompat(R.color.red))
        binding.tvDescription.text = resources.getString(R.string.close_auction_description)
        binding.btnConfirm.setBackgroundColor(requireContext().getColorCompat(R.color.red))
    }

    private fun showPublishOfferState(hasCoins: Boolean) {
        binding.tvTittle.hide()
        binding.tvDescription.text =
            if (hasCoins) resources.getString(
                R.string.publish_your_offer_for_n_coins,
                addOfferCost.toString()
            )
            else resources.getString(R.string.contact_us_for_more_coins)
        binding.tvCoinsBalance.show()
        binding.tvCoinsBalance.text =
            resources.getString(R.string.coins_balance, availableCoins.toString())
        binding.btnConfirm.text = if (hasCoins) resources.getString(R.string.confirm)
        else resources.getString(R.string.message)
    }

    private fun showLogOutState() {
        binding.tvTittle.text = resources.getString(R.string.log_out)
        binding.tvDescription.text = resources.getString(R.string.log_out_description)
    }

    private fun showDeleteAccountState() {
        binding.tvTittle.text = resources.getString(R.string.delete_account)
        binding.tvTittle.setTextColor(requireContext().getColorCompat(R.color.red))
        binding.tvDescription.text = resources.getString(R.string.delete_account_description)
        binding.btnConfirm.setBackgroundColor(requireContext().getColorCompat(R.color.red))
    }

    private fun initListeners() {
        binding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            confirmListener?.invoke()
            dialog?.dismiss()
        }
    }

    fun setCoinsAmount(coins: Int, offerCost: Int = 0) {
        availableCoins = coins
        addOfferCost = offerCost
    }

    companion object {
        const val LOGOUT_DIALOG_TAG = "LOGOUT_DIALOG_TAG"
        const val CLOSE_AUCTION_DIALOG_TAG = "CLOSE_AUCTION_DIALOG_TAG"
        const val DELETE_ACCOUNT_DIALOG_TAG = "DELETE_ACCOUNT_DIALOG_TAG"
        const val PUBLISH_ACCOUNT_DIALOG_TAG = "PUBLISH_ACCOUNT_DIALOG_TAG"
        const val PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG = "PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG"
    }
}