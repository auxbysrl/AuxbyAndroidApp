package com.fivedevs.auxby.screens.dashboard.offers.bottomSheets

import android.content.DialogInterface
import androidx.databinding.ViewDataBinding
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ViewReportOfferBottomSheetBinding
import com.fivedevs.auxby.domain.models.ReportOfferModel
import com.fivedevs.auxby.domain.models.enums.ReportTypeEnum
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
        reportOfferModel.apply {
            type = getReportType()
            comment = root.etFeedback.text.toString()
            callback(reportOfferModel)
        }
    }

    private fun getReportType(): String {
        val selectedIndex = root.rgContainer.indexOfChild(root.rgContainer.findViewById(root.rgContainer.checkedRadioButtonId))
        return when (selectedIndex) {
            0 -> {
                ReportTypeEnum.ABUSE.value
            }
            1 -> {
                ReportTypeEnum.DECEPTIVE_AD.value
            }
            else -> {
                ReportTypeEnum.DIRTY_LANGUAGE.value
            }
        }
    }

    fun showCongratsView() {
        root.inclCongrats.root.show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}