package com.fivedevs.auxby.screens.dashboard.offers.bottomSheets

import android.content.DialogInterface
import androidx.databinding.ViewDataBinding
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ViewOwnerRatingBottomSheetBinding
import com.fivedevs.auxby.domain.models.SellerRatingModel
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog

class SellerRatingBottomSheet(private val callback: (SellerRatingModel) -> Unit) :
    BaseBottomSheetDialog(R.layout.view_owner_rating_bottom_sheet, false) {

    private lateinit var root: ViewOwnerRatingBottomSheetBinding
    private val sellerRatingModel = SellerRatingModel()

    override fun getContentView(binding: ViewDataBinding) {
        root = binding as ViewOwnerRatingBottomSheetBinding
        initListeners()
    }

    private fun initListeners() {
        root.tvMaybeLater.setOnClickListenerWithDelay {
            dismiss()
        }

        root.ivClose.setOnClickListenerWithDelay {
            dismiss()
        }

        root.btnSend.setOnClickListenerWithDelay {
            callback(sellerRatingModel.apply {
                rate = root.sellerRatingBar.rating.toInt()
            })
            getRating()
        }

        root.inclCongrats.btnClose.setOnClickListenerWithDelay {
            dismiss()
        }
    }

    private fun getRating() {
        root.inclCongrats.root.show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}