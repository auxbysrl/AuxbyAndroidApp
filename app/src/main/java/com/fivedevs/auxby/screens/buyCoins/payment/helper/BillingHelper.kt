package com.fivedevs.auxby.screens.buyCoins.payment.helper

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.fivedevs.auxby.screens.buyCoins.payment.dataProvider.DataProvider
import com.fivedevs.auxby.screens.buyCoins.payment.enums.BillingState
import com.fivedevs.auxby.screens.buyCoins.payment.status.State.getBillingState
import com.fivedevs.auxby.screens.buyCoins.payment.status.State.setBillingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


@Suppress("unused")
abstract class BillingHelper(private val context: Context) {

    private val dpProvider by lazy { DataProvider() }
    private var connectionCallback: ((connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit)? =
        null
    private var purchaseCallback: ((isPurchased: Boolean, message: String) -> Unit)? = null

    /* ------------------------------------------------ Initializations ------------------------------------------------ */

    private val billingClient by lazy {
        BillingClient.newBuilder(context).setListener(purchasesUpdatedListener)
            .enablePendingPurchases().build()
    }

    /* ------------------------------------------------ Establish Connection ------------------------------------------------ */

    abstract fun startConnection(
        productIdsList: List<String>,
        callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit
    )

    abstract fun startOldPurchaseConnection(
        productIdsList: List<String>,
        callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit
    )


    /**
     *  Get a single testing product_id ("android.test.purchased")
     */
    fun getDebugProductIDList() = dpProvider.getDebugProductIDList()

    /**
     *  Get a single testing product_id ("android.test.item_unavailable")
     */
    fun getDebugProductIDUnavailableList() = dpProvider.getDebugProductIDUnavailableList()


    /**
     *  Get a single testing product_id ("android.test.purchased")
     */
    fun getProductDetailsList() = dpProvider.getProductDetailsList()

    /**
     *  Get multiple testing product_ids
     */
    fun getDebugProductIDsList() = dpProvider.getDebugProductIDsList()

    protected fun startBillingConnection(
        productIdsList: List<String>,
        callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit
    ) {
        connectionCallback = callback
        if (productIdsList.isEmpty()) {
            setBillingState(BillingState.EMPTY_PRODUCT_ID_LIST)
            callback.invoke(false, false, BillingState.EMPTY_PRODUCT_ID_LIST.message)
            return
        }
        dpProvider.setProductIdsList(productIdsList)

        if (!isInternetConnected) {
            setBillingState(BillingState.NO_INTERNET_CONNECTION)
            callback.invoke(false, false, BillingState.NO_INTERNET_CONNECTION.message)
            return
        }

        setBillingState(BillingState.CONNECTION_ESTABLISHING)
        if (billingClient.isReady) {
            setBillingState(BillingState.CONNECTION_ESTABLISHED)
            Handler(Looper.getMainLooper()).post {
                callback.invoke(
                    true,
                    false,
                    BillingState.CONNECTION_ESTABLISHED.message
                )
            }
            queryForAvailableProducts()
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    setBillingState(BillingState.CONNECTION_DISCONNECTED)
                    Handler(Looper.getMainLooper()).post {
                        callback.invoke(
                            false,
                            false,
                            BillingState.CONNECTION_DISCONNECTED.message
                        )
                    }
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val isBillingReady =
                        billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    if (isBillingReady) {
                        setBillingState(BillingState.CONNECTION_ESTABLISHED)
                        checkOldPurchases()
                        Handler(Looper.getMainLooper()).post {
                            callback.invoke(
                                true,
                                false,
                                BillingState.CONNECTION_ESTABLISHED.message
                            )
                        }
                    } else {
                        setBillingState(BillingState.CONNECTION_FAILED)
                        Handler(Looper.getMainLooper()).post {
                            callback.invoke(
                                false,
                                false,
                                billingResult.debugMessage
                            )
                        }
                    }
                }
            })
        }
    }

    protected fun getOldPurchases(
        productIdsList: List<String>,
        callback: (connectionResult: Boolean, alreadyPurchased: Boolean, message: String) -> Unit
    ) {
        connectionCallback = callback
        if (productIdsList.isEmpty()) {
            setBillingState(BillingState.EMPTY_PRODUCT_ID_LIST)
            callback.invoke(false, false, BillingState.EMPTY_PRODUCT_ID_LIST.message)
            return
        }
        dpProvider.setProductIdsList(productIdsList)

        if (!isInternetConnected) {
            setBillingState(BillingState.NO_INTERNET_CONNECTION)
            callback.invoke(false, false, BillingState.NO_INTERNET_CONNECTION.message)
            return
        }

        setBillingState(BillingState.CONNECTION_ESTABLISHING)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                setBillingState(BillingState.CONNECTION_DISCONNECTED)
                Handler(Looper.getMainLooper()).post {
                    callback.invoke(
                        false,
                        false,
                        BillingState.CONNECTION_DISCONNECTED.message
                    )
                }
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val isBillingReady =
                    billingResult.responseCode == BillingClient.BillingResponseCode.OK
                if (isBillingReady) {
                    setBillingState(BillingState.CONNECTION_ESTABLISHED)
                    checkOldPurchases()
                    Handler(Looper.getMainLooper()).post {
                        callback.invoke(
                            true,
                            false,
                            BillingState.CONNECTION_ESTABLISHED.message
                        )
                    }
                } else {
                    setBillingState(BillingState.CONNECTION_FAILED)
                    Handler(Looper.getMainLooper()).post {
                        callback.invoke(
                            false,
                            false,
                            billingResult.debugMessage
                        )
                    }
                }
            }
        })
    }


    private fun checkOldPurchases() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_FETCHING)
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { _, purchases ->
            purchases.forEach { purchase ->
                if (purchase.products.isEmpty()) {
                    setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_NOT_FOUND)
                }
                purchase.products.forEach { product ->
                    dpProvider.getProductIdsList().forEach {
                        if (product == it) {
                            setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_OWNED)
                            Handler(Looper.getMainLooper()).post {
                                connectionCallback?.invoke(
                                    true,
                                    true,
                                    BillingState.CONSOLE_OLD_PRODUCTS_OWNED.message
                                )
                            }
                        }
                    }
                }
            }
            if (purchases.isEmpty()) {
                setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_NOT_FOUND)
            }
            queryForAvailableProducts()
        }
    }

    /* -------------------------------------------- Query available console products  -------------------------------------------- */

    private fun queryForAvailableProducts() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_PRODUCTS_FETCHING)
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(
                QueryProductDetailsParams.newBuilder().setProductList(dpProvider.getProductList())
                    .build()
            )
        }
        // Process the result.
        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_FETCHED_SUCCESSFULLY)
            if (productDetailsResult.productDetailsList.isNullOrEmpty()) {

            } else {
                productDetailsResult.productDetailsList?.let {
                    dpProvider.setProductDetailsList(it)
                    setBillingState(BillingState.CONSOLE_PRODUCTS_AVAILABLE)
                }
            }
        } else {
            setBillingState(BillingState.CONSOLE_PRODUCTS_FETCHING_FAILED)
        }
    }

    /* --------------------------------------------------- Make Purchase  --------------------------------------------------- */

    protected fun purchase(
        activity: Activity?,
        productId: String,
        callback: (isPurchased: Boolean, message: String) -> Unit
    ) {
        this.purchaseCallback = callback

        if (activity == null) return
        if (checkValidations(callback)) return

        val product = dpProvider.getProductDetailById(productId)
        if (product == null) {
            Timber.e("Product with $productId not found!")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product).build()
        )

        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY)
            BillingClient.BillingResponseCode.USER_CANCELED -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED)
            else -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
        }
    }

    private fun checkValidations(callback: (isPurchased: Boolean, message: String) -> Unit): Boolean {
        if (getBillingState() == BillingState.EMPTY_PRODUCT_ID_LIST) {
            callback.invoke(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.NO_INTERNET_CONNECTION) {
            if (isInternetConnected && connectionCallback != null) {
                startBillingConnection(
                    productIdsList = dpProvider.getProductIdsList(),
                    callback = connectionCallback!!
                )
                return true
            }
            callback.invoke(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONNECTION_FAILED || getBillingState() == BillingState.CONNECTION_DISCONNECTED || getBillingState() == BillingState.CONNECTION_ESTABLISHING) {
            callback.invoke(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_FETCHING || getBillingState() == BillingState.CONSOLE_PRODUCTS_FETCHING_FAILED) {
            callback.invoke(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_NOT_EXIST) {
            callback.invoke(false, BillingState.CONSOLE_PRODUCTS_NOT_EXIST.message)
            return true
        }

        val productIdsList = dpProvider.getProductIdsList()
        dpProvider.getProductDetailsList()?.forEach { productDetails ->
            if (!productIdsList.contains(productDetails.productId)) {
                setBillingState(BillingState.CONSOLE_PRODUCTS_NOT_FOUND)
                callback.invoke(false, BillingState.CONSOLE_PRODUCTS_NOT_FOUND.message)
                return true
            }
        }

        if (billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).responseCode != BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.FEATURE_NOT_SUPPORTED)
            return true
        }
        return false
    }

    /* --------------------------------------------------- Purchase Response  --------------------------------------------------- */

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchaseMutableList: MutableList<Purchase>? ->
            Timber.tag(TAG).i("purchasesUpdatedListener: $purchaseMutableList")

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Timber.tag(TAG).d("BillingResponseCode.OK PURCHASED_SUCCESSFULLY")
                    setBillingState(BillingState.PURCHASED_SUCCESSFULLY)
                    handlePurchase(purchaseMutableList)
                    return@PurchasesUpdatedListener
                }

                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    Timber.tag(TAG).d("BillingResponseCode.BILLING_UNAVAILABLE")
                }

                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    Timber.tag(TAG).d("BillingResponseCode.DEVELOPER_ERROR")
                }

                BillingClient.BillingResponseCode.ERROR -> {
                    Timber.tag(TAG).d("BillingResponseCode.ERROR")
                    setBillingState(BillingState.PURCHASING_ERROR)
                }

                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                    Timber.tag(TAG).d("BillingResponseCode.FEATURE_NOT_SUPPORTED")
                }

                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Timber.tag(TAG).d("BillingResponseCode.ITEM_ALREADY_OWNED")
                    setBillingState(BillingState.PURCHASING_ALREADY_OWNED)
                    purchaseCallback?.invoke(true, BillingState.PURCHASING_ALREADY_OWNED.message)
                    return@PurchasesUpdatedListener
                }

                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                    Timber.tag(TAG).d("BillingResponseCode.ITEM_NOT_OWNED")
                }

                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                    Timber.tag(TAG).d("BillingResponseCode.ITEM_UNAVAILABLE")
                }

                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Timber.tag(TAG).d("BillingResponseCode.SERVICE_DISCONNECTED")
                }

                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                    Timber.tag(TAG).d("BillingResponseCode.SERVICE_TIMEOUT")
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Timber.tag(TAG).d("BillingResponseCode.USER_CANCELED")
                    setBillingState(BillingState.PURCHASING_USER_CANCELLED)
                }
            }
            purchaseCallback?.invoke(false, getBillingState().message)
        }

    private fun handlePurchase(purchases: MutableList<Purchase>?) = CoroutineScope(Dispatchers.Main).launch {
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                setBillingState(BillingState.PURCHASED_SUCCESSFULLY)
                purchaseCallback?.invoke(true, BillingState.PURCHASED_SUCCESSFULLY.message)
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                } else {
                    consumePurchase(purchase)
                }
            } else {
                setBillingState(BillingState.PURCHASING_FAILURE)
            }
        } ?: run {
            setBillingState(BillingState.PURCHASING_USER_CANCELLED)
        }
        purchaseCallback?.invoke(false, getBillingState().message)
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) = withContext(Dispatchers.IO) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken).build()
        val listener = AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.tag(TAG).d("Acknowledge purchase success")
                consumePurchase(purchase)
            } else {
                Timber.tag(TAG).d("Acknowledge purchase failure: ${billingResult.debugMessage}")
            }
        }
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, listener)
    }

    private fun consumePurchase(purchase: Purchase) {
        Timber.tag(TAG).d("consumePurchase $purchase")
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(params) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Consume purchase success: ${billingResult.responseCode} ${billingResult.debugMessage}")
            } else {
                Timber.e("Consume purchase error: ${billingResult.responseCode} ${billingResult.debugMessage}")
            }
        }
    }

    private val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.tag(TAG).d("acknowledgePurchaseResponseListener: Acknowledged successfully")
        } else {
            Timber.tag(TAG).d("acknowledgePurchaseResponseListener: Acknowledgment failure")
        }
    }

    /* ------------------------------------- Internet Connection ------------------------------------- */

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val isInternetConnected: Boolean
        get() {
            try {
                val network = connectivityManager.activeNetwork ?: return false
                val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } catch (ex: Exception) {
                return false
            }
        }

    fun startBillingClientConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                } else {
                    Timber.tag(TAG).e("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle the disconnection
                Timber.tag(TAG).e("Billing service disconnected")
            }
        })
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchase(purchasesList)
            } else {
                Timber.tag(TAG).e("Error querying purchases: ${billingResult.debugMessage}")
            }
        }
    }

    companion object {
        const val TAG = "BillingManager"
    }
}