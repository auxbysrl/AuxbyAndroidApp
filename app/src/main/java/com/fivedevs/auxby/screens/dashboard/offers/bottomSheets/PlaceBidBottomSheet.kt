package com.fivedevs.auxby.screens.dashboard.offers.bottomSheets

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.databinding.ViewPlaceBidButtonExpandedBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.PlaceBidModel
import com.fivedevs.auxby.domain.utils.Formatters.priceDecimalFormat
import com.fivedevs.auxby.domain.utils.OfferUtils.getCurrency
import com.fivedevs.auxby.domain.utils.extensions.*
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog

class PlaceBidBottomSheet(
    private val user: MutableLiveData<User>,
    private val offer: MutableLiveData<OfferModel>,
    private val placeBidCost: Int,
    private val placeBidCallback: (PlaceBidModel) -> Unit
) :
    BaseBottomSheetDialog(R.layout.view_place_bid_button_expanded, false) {

    private lateinit var root: ViewPlaceBidButtonExpandedBinding

    override fun getContentView(binding: ViewDataBinding) {
        root = binding as ViewPlaceBidButtonExpandedBinding
        populateViews()
        initListeners()
    }

    private fun populateViews() {
        with(root) {
            etBidValue.run {
                setText(getInitialBidValue())
            }
            tvCurrency.text = getCurrency(offer.value?.currencyType)
            tvHighestBid.text =
                resources.getString(
                    R.string.highest_bid_value,
                    getHighestBid(),
                    getCurrency(offer.value?.currencyType)
                )
            tvBidCost.text = resources.getString(R.string.bid_cost_value, placeBidCost.toString())
            tvCoinsBalance.text =
                resources.getString(R.string.coins_balance_value, user.value?.availableCoins.toString())
        }
        increaseBid()
    }

    private fun getInitialBidValue(): String {
        return if (offer.value?.highestBid?.equals(0f) == true) {
            priceDecimalFormat.format(offer.value?.price?.toDouble())
        } else {
            priceDecimalFormat.format(offer.value?.highestBid?.toDouble())
        }
    }

    private fun initListeners() {
        root.btnCancel.setOnClickListenerWithDelay {
            dismiss()
        }

        root.btnPlaceBid.setOnClickListenerWithDelay {
            placeBidCallback(getPlaceBid())
        }

        root.ivDecreaseBid.setOnClickListener {
            decreaseBid()
        }

        root.ivIncreaseBid.setOnClickListener {
            increaseBid()
        }

        root.etBidValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val digitsOnly = p0.toString().toDecimalsOnly()
                val amount = if (offer.value?.highestBid?.toString().isNullOrEmpty())
                    offer.value?.price else offer.value?.highestBid

                amount?.let { price ->
                    if (digitsOnly.toLongOrNull().orZero() <= price) {
                        showBidValueErrorMessage()
                    } else {
                        hideBidValueErrorMessage()
                    }
                }
            }
        })
    }

    private fun showBidValueErrorMessage() {
        with(root) {
            tvBidErrorMessage.text = resources.getString(
                R.string.bid_cannot_be_lower_than,
                getHighestBid() + getCurrency(offer.value?.currencyType)
            )
            tvBidErrorMessage.show()
            tvBidConversionValue.invisible()
            btnPlaceBid.isEnabled = false
        }
    }

    private fun hideBidValueErrorMessage() {
        with(root) {
            tvBidErrorMessage.invisible()
            tvBidConversionValue.invisible()
            btnPlaceBid.isEnabled = true
        }
    }

    private fun increaseBid() {
        offer.value?.price?.let { price ->
            if (root.etBidValue.text.toString().isEmpty()) return

            val percentFromPrice = (price * 0.1).toInt()
            val finalBid =
                root.etBidValue.text.toString().toDecimalsOnly().toInt() + percentFromPrice
            root.etBidValue.setText(finalBid.toString())
        }
    }

    private fun decreaseBid() {
        offer.value?.price?.let { price ->
            if (root.etBidValue.text.toString().isEmpty()) return

            val percentFromPrice = (price * 0.1).toInt()
            val finalBid =
                root.etBidValue.text.toString().toDecimalsOnly().toInt().orZero() - percentFromPrice
            root.etBidValue.setText(finalBid.toString())
        }
    }

    private fun getHighestBid(): String {
        var highestBid = offer.value?.price
        if (offer.value?.highestBid != 0f) {
            highestBid = offer.value?.highestBid
        }
        return priceDecimalFormat.format(highestBid)
    }

    private fun getPlaceBid(): PlaceBidModel {
        return PlaceBidModel(
            offer.value?.id ?: 0,
            root.etBidValue.text.toString().toDecimalsOnly().toInt()
        )
    }
}