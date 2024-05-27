package com.fivedevs.auxby.domain.utils.views

import android.app.Activity
import com.fivedevs.auxby.R
import com.tapadoo.alerter.Alerter

object AlerterUtils {

    fun showErrorAlert(activity: Activity, errorMessage: String) {
        Alerter.create(activity)
            .setTitle(activity.getString(R.string.error_title))
            .setText(errorMessage)
            .setBackgroundColorInt(activity.getColor(R.color.red))
            .enableInfiniteDuration(true)
            .setTitleAppearance(R.style.AlertErrorTitleAppearance)
            .setTextAppearance(R.style.AlertErrorSubTitleAppearance)
            .addButton(activity.getString(R.string.ok), R.style.AlertErrorButtonAppearance) {
                Alerter.hide()
            }
            .show()
    }

    fun showSuccessAlert(activity: Activity, message: String, callbackFunction: (() -> Unit?)? = null) {
        Alerter.create(activity)
            .setTitle(activity.getString(R.string.success_title))
            .setText(message)
            .setBackgroundColorInt(activity.getColor(R.color.green))
            .enableInfiniteDuration(false)
            .setTitleAppearance(R.style.AlertErrorTitleAppearance)
            .setTextAppearance(R.style.AlertErrorSubTitleAppearance)
            .addButton(activity.getString(R.string.ok), R.style.AlertErrorButtonAppearance) {
                Alerter.hide()
                callbackFunction?.invoke()
            }
            .show()
    }
}