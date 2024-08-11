package com.fivedevs.auxby.screens.dashboard.offers.categories.categoryOffers

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityCategoryOffersBinding
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.OfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.adapters.SmallOfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.categories.AllCategoriesActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.dashboard.offers.utils.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryOffersActivity : BaseActivity() {

    private var offerAdapter: SmallOfferAdapter? = null
    private lateinit var binding: ActivityCategoryOffersBinding

    private val viewModel by viewModels<CategoryOffersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        getIntentData()
        initListeners()
        initObservers()
        initOffersRv()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_category_offers)
        binding.apply {
            viewModel = this.viewModel
            lifecycleOwner = this@CategoryOffersActivity
        }
    }

    private fun getIntentData() {
        val categoryId = intent.getIntExtra(AllCategoriesActivity.SELECTED_CATEGORY_ID, -1)
        viewModel.receivedCategoryId(categoryId)
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setOnClickListener {
            finish()
        }

        binding.inclToolbar.ivFilter.setOnClickListener {
            // TODO
        }
    }

    private fun initObservers() {
        viewModel.categoryModel.observe(this) {
            handleCategoryData(it)
        }

        viewModel.offersResponse.observe(this) {
            viewModel.localUser.value?.let { user ->
                offerAdapter?.user = user
            }
            offerAdapter?.addNewOffers(it)
        }

        viewModel.paginationOffersResponse.observe(this) {
            updatePaginationAdapter(it)
        }

        viewModel.showShimmerAnimation.observe(this) { shouldStart ->
            startStopShimmer(shouldStart)
        }

        viewModel.shouldSaveOfferPage.observe(this) {
            handleSaveOffer(it)
        }
    }

    private fun handleSaveOffer(it: OfferModel) {
        if (this.isNetworkConnected()) {
            viewModel.saveOfferToFavorites(it)
        } else {
            this.showInternetConnectionDialog(false)
        }
    }

    private fun startStopShimmer(shouldStart: Boolean) {
        if (shouldStart) {
            binding.inclShimmerCategoryOffers.shimmerContainer.show()
        } else {
            binding.inclShimmerCategoryOffers.shimmerContainer.hide()
        }
    }

    private fun handleCategoryData(it: CategoryModel) {
        binding.inclToolbar.tvToolbarTitle.text = it.label.getName(this)
        binding.tvNoOffers.text = getString(R.string.n_offers_in, it.noOffers.toString())
        binding.tvCategoryTitle.text = it.label.getName(this)
    }

    private fun initOffersRv() {
        offerAdapter = SmallOfferAdapter(
            this,
            mutableListOf(),
            ::onOfferSelected,
            viewModel.shouldSaveOfferPublishSubject,
            viewModel.isUserLoggedIn.value ?: false
        )
        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (offerAdapter?.getItemViewType(position)) {
                    PaginationConstants.ITEM -> 1
                    else -> 2
                }
            }
        }

        binding.rvOffers.apply {
            this.layoutManager = layoutManager
            adapter = offerAdapter
        }

        binding.rvOffers.addItemDecoration(GridSpacingItemDecoration(2, 20, false))

        binding.rvOffers.itemAnimator = null
        binding.rvOffers.initPagination(layoutManager, ::loadMoreItems)
    }

    private fun loadMoreItems() {
        binding.rvOffers.isLoading = true
        offerAdapter?.addLoadingFooter()
        viewModel.loadMoreOffersByCategoryId()
    }

    private fun updatePaginationAdapter(offers: List<OfferModel>) {
        offerAdapter?.removeLoadingFooter()
        offerAdapter?.addNewOffers(offers)
        if (viewModel.currentPage == viewModel.totalPagesCall) {
            binding.rvOffers.isLastPage = true
        }
        binding.rvOffers.isLoading = false
    }

    private fun onOfferSelected(offerId: Long) {
        launchActivity<OfferDetailsActivity> {
            putExtra(SELECTED_OFFER_ID, offerId)
        }
    }
}