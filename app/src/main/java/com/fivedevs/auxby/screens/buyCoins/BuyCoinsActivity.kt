package com.fivedevs.auxby.screens.buyCoins

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityBuyCoinsBinding
import com.fivedevs.auxby.domain.models.CoinBundle
import com.fivedevs.auxby.domain.models.enums.ProductTypeEnum
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setStatusBarColor
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.buyCoins.adapter.AdapterCoinsBundle
import com.fivedevs.auxby.screens.buyCoins.payment.BillingManager
import com.fivedevs.auxby.screens.buyCoins.payment.enums.BillingState
import com.fivedevs.auxby.screens.buyCoins.payment.status.State
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BuyCoinsActivity : BaseActivity() {

    private lateinit var binding: ActivityBuyCoinsBinding
    private val viewModel by viewModels<BuyCoinsViewModel>()
    private val billingManager by lazy { BillingManager(this) }

    private val productsList = listOf(
        ProductTypeEnum.SILVER.value,
        ProductTypeEnum.GOLD.value,
        ProductTypeEnum.DIAMOND.value
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(getColor(R.color.colorPrimary), switchToLightStatusBar = false)
        initBinding()
        initBilling()
        initObservers()
        initListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_buy_coins)
        binding.apply {
            viewModel = this@BuyCoinsActivity.viewModel
            lifecycleOwner = this@BuyCoinsActivity
        }
    }

    private fun initObservers() {
        viewModel.showShimmerAnimation.observe(this) { shouldStart ->
            startStopShimmer(shouldStart)
        }

        viewModel.redirectToDashboard.observe(this) {
            finish()
        }

        State.billingState.observe(this) { it ->
            Log.d("BillingManager", "initObserver: $it")
            if (it.message.equals(
                    BillingState.CONSOLE_PRODUCTS_AVAILABLE.message,
                    ignoreCase = true
                )
            ) {
                // get the list of products and map them to CoinBundle
                val products = billingManager.getProductDetailsList()?.sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros }

                products?.let {
                    val bundles: MutableList<CoinBundle> = mutableListOf()
                    it.forEach { productItem ->
                        bundles.add(
                            CoinBundle(
                                id = productItem.productId,
                                name = productItem.name,
                                description = productItem.description,
                                price = productItem.oneTimePurchaseOfferDetails!!.formattedPrice,
                                priceInMicros = productItem.oneTimePurchaseOfferDetails!!.priceAmountMicros,
                                isChecked = false
                            )
                        )
                    }

                    // pre-select GOLD bundle
                    bundles.map { bundleProduct ->
                        if (bundleProduct.id == ProductTypeEnum.GOLD.value) {
                            bundleProduct.isChecked = true
                            viewModel.selectedBundle = bundleProduct
                        }
                    }

                    // populate bundles recyclerView
                    populateBundleRv(bundles)
                    hideEmptyStateView()
                }

            } else if (it.message.equals(
                    BillingState.CONSOLE_PRODUCTS_NOT_FOUND.message,
                    ignoreCase = true
                )
            ) {
                showEmptyStateView()
            }
            startStopShimmer(shouldStart = false)
        }
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBuyCoins.setOnClickListenerWithDelay {
            onPurchaseClick()
        }
    }

    private fun populateBundleRv(bundles: MutableList<CoinBundle>) {
        binding.rvCoins.apply {
            layoutManager = LinearLayoutManager(this@BuyCoinsActivity)
            adapter = AdapterCoinsBundle(
                this@BuyCoinsActivity,
                bundles,
                viewModel.bundleSelectorPublishSubject
            )
            itemAnimator = null
        }
    }

    private fun startStopShimmer(shouldStart: Boolean) {
        if (shouldStart) {
            binding.inclShimmerBuyCoins.shimmerContainer.startShimmer()
        } else {
            binding.inclShimmerBuyCoins.shimmerContainer.stopShimmer()
        }
    }

    private fun initBilling() {
        startStopShimmer(shouldStart = true)
        billingManager.startConnection(productsList) { isConnectionEstablished, alreadyPurchased, message ->
            Timber.tag(BILLING_TAG).i(message)
            billingManager.preConsumePurchases()
        }
    }


    private fun onPurchaseClick() {
        billingManager.makePurchase(this, viewModel.selectedBundle.id) { isSuccess, message ->
            Timber.d(message)
            if (isSuccess && message != BillingState.PURCHASING_ALREADY_OWNED.message) {
                viewModel.makeTransaction()
                showMessage(message)
            } else {
                Timber.d(message)
            }
        }
    }

    private fun showEmptyStateView() {
        binding.rvCoins.hide()
        binding.inclEmptyListView.root.show()
    }

    private fun hideEmptyStateView() {
        binding.rvCoins.show()
        binding.inclEmptyListView.root.hide()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val BILLING_TAG = "BILLING"
    }
}