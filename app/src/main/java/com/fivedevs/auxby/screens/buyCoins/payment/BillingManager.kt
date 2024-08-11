package com.fivedevs.auxby.screens.buyCoins.payment

import android.app.Activity
import android.content.Context
import com.fivedevs.auxby.screens.buyCoins.payment.helper.BillingHelper

/**
 * @param context: Context can be of Application class
 */

class BillingManager(private val context: Context) : BillingHelper(context) {

    override fun startConnection(productIdsList: List<String>, callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit) = startBillingConnection(productIdsList = productIdsList, callback = callback)
    override fun startOldPurchaseConnection(productIdsList: List<String>, callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit) = getOldPurchases(productIdsList = productIdsList, callback = callback)

    fun makePurchase(activity: Activity?, productId: String, callback: (isPurchased: Boolean, message: String) -> Unit) = purchase(activity, productId,callback)

    fun preConsumePurchases() = startBillingClientConnection()
}