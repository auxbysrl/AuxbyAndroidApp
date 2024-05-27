package com.fivedevs.auxby.domain.utils.views

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import com.fivedevs.auxby.R

class LoaderDialog(val context: Context) {

    var dialog: Dialog = Dialog(context)

    init {
        dialog.setContentView(R.layout.dialog_loader)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    fun showDialog() {
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}