package com.fivedevs.auxby.screens.dashboard.offers.userOffers

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityViewUserOffersBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.OfferOwner
import com.fivedevs.auxby.domain.models.SellerRatingModel
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.OfferUtils
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.loadImage
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.OfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.bottomSheets.SellerRatingBottomSheet
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity.Companion.USER_OFFERS
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewUserOffersActivity : BaseActivity() {

    private var offerAdapter: OfferAdapter? = null
    private lateinit var binding: ActivityViewUserOffersBinding

    private val viewModel by viewModels<ViewUserOffersViewModel>()

    private val rateUserInfoDialog: GenericDialog by lazy {
        GenericDialog(
            null,
            DialogTypes.RATE_USER_INFO
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        getIntentData()
        initListeners()
        initObservers()
        initOffersRv()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_user_offers)
        binding.apply {
            lifecycleOwner = this@ViewUserOffersActivity
        }
    }

    private fun getIntentData() {
        val offerOwner = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(USER_OFFERS, OfferOwner::class.java)
        } else {
            intent.getParcelableExtra(USER_OFFERS)
        }

        offerOwner?.let {
            populateUserDetailsFields(it)
            viewModel.initData(it.userName)
        }
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setOnClickListener {
            finish()
        }

        binding.tvRateSeller.setOnClickListener {
            showSellerRatingBottomSheet()
        }

        binding.ivRateInfo.setOnClickListener {
            rateUserInfoDialog.show(supportFragmentManager, rateUserInfoDialog.tag)
        }
    }

    private fun initObservers() {
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

        viewModel.allowRatingStatus.observe(this) { status ->
            if (status) {
                binding.ivRateInfo.hide()
                binding.tvRateSeller.show()
            }
        }
    }

    private fun handleSaveOffer(it: OfferModel) {
        if (this.isNetworkConnected()) {
            viewModel.saveOfferToFavorites(it)
        } else {
            this.showInternetConnectionDialog(false)
        }
    }

    private fun populateUserDetailsFields(owner: OfferOwner) {
        binding.apply {
            tvUsername.text = getString(R.string.first_and_last_name, owner.firstName, owner.lastName)
            sellerRatingBar.rating = owner.rating
            tvUserActiveTime.text = OfferUtils.getUserLastSeenTime(this@ViewUserOffersActivity, owner.lastSeen)
            ivUserAvatar.loadImage(
                owner.avatarUrl.toString(),
                R.drawable.ic_profile_placeholder,
                circleCrop = true
            )
        }
    }

    private fun startStopShimmer(shouldStart: Boolean) {
        if (shouldStart) {
            binding.inclShimmerCategoryOffers.shimmerContainer.show()
        } else {
            binding.inclShimmerCategoryOffers.shimmerContainer.hide()
        }
    }

    private fun showSellerRatingBottomSheet() {
        val sellerRatingBottomSheet = SellerRatingBottomSheet(::sendSellerRating)
        sellerRatingBottomSheet.show(this.supportFragmentManager, sellerRatingBottomSheet.tag)
    }

    private fun sendSellerRating(sellerRatingModel: SellerRatingModel) {
        viewModel.rateSeller(sellerRatingModel)
    }

    private fun initOffersRv() {
        offerAdapter = OfferAdapter(
            this,
            mutableListOf(),
            ::onOfferSelected,
            viewModel.shouldSaveOfferPublishSubject,
            viewModel.isUserLoggedIn.value ?: false
        )
        val layoutManager = LinearLayoutManager(this@ViewUserOffersActivity)
        binding.rvOffers.apply {
            this.layoutManager = layoutManager
            adapter = offerAdapter
        }

        binding.rvOffers.itemAnimator = null
        binding.rvOffers.initPagination(layoutManager, ::loadMoreItems)
    }

    private fun loadMoreItems() {
        binding.rvOffers.isLoading = true
        offerAdapter?.addLoadingFooter()
        viewModel.loadMoreOffers()
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
            putExtra(Constants.SELECTED_OFFER_ID, offerId)
        }
    }
}