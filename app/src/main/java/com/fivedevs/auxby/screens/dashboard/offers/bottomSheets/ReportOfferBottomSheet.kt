package com.fivedevs.auxby.screens.dashboard.offers.bottomSheets

import android.content.DialogInterface
import android.widget.RadioButton
import androidx.databinding.ViewDataBinding
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ViewReportOfferBottomSheetBinding
import com.fivedevs.auxby.domain.models.ReportOfferModel
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog

class ReportOfferBottomSheet(private val callback: (ReportOfferModel) -> Unit) :
    BaseBottomSheetDialog(R.layout.view_report_offer_bottom_sheet, true) {

    private lateinit var root: ViewReportOfferBottomSheetBinding
    private val reportOfferModel = ReportOfferModel()

    override fun getContentView(binding: ViewDataBinding) {
        root = binding as ViewReportOfferBottomSheetBinding
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
            getReportFeedback()
        }

        root.inclCongrats.btnClose.setOnClickListenerWithDelay {
            dismiss()
        }
    }

    private fun getReportFeedback() {
        val selectedRadioButton =
            root.root.findViewById<RadioButton>(root.rgContainer.checkedRadioButtonId)
        reportOfferModel.apply {
            type = selectedRadioButton.text.toString()
            comment = root.etFeedback.text.toString()
            callback(reportOfferModel)
        }
    }

    fun showCongratsView() {
        root.inclCongrats.root.show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}