package com.fivedevs.auxby.screens.dashboard.offers.bottomSheets

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ViewBidHistoryBottomSheetBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog
import com.fivedevs.auxby.screens.dashboard.offers.adapters.BidHistoryAdapter

class BidHistoryBottomSheet(private val offerModel: MutableLiveData<OfferModel>) : BaseBottomSheetDialog(R.layout.view_bid_history_bottom_sheet, false) {

    private lateinit var root: ViewBidHistoryBottomSheetBinding

    override fun getContentView(binding: ViewDataBinding) {
        root = binding as ViewBidHistoryBottomSheetBinding
        initListeners()
        populateBidHistoryList()
    }

    private fun populateBidHistoryList() {
        offerModel.let { offer ->
            offer.value?.bids?.let { bids ->
                val sortedBids = bids.sortedByDescending { it?.bidValue }.take(10)
                root.rvBidHistory.apply {
                    adapter = BidHistoryAdapter(requireContext(), sortedBids, offer.value?.currencyType)
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
            }
        }
    }

    private fun initListeners() {
        root.ivClose.setOnClickListenerWithDelay {
            dismiss()
        }

        root.btnClose.setOnClickListenerWithDelay {
            dismiss()
        }
    }
}