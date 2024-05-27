package com.fivedevs.auxby.screens.dashboard.offers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentOffersBinding
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.*
import com.fivedevs.auxby.screens.dashboard.DashboardActivity.Companion.showGuestModeBottomSheet
import com.fivedevs.auxby.screens.dashboard.DashboardViewModel
import com.fivedevs.auxby.screens.dashboard.offers.adapters.CategoryAdapter
import com.fivedevs.auxby.screens.dashboard.offers.adapters.OfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.adapters.PromotedOffersAdapter
import com.fivedevs.auxby.screens.dashboard.offers.categories.AllCategoriesActivity
import com.fivedevs.auxby.screens.dashboard.offers.categories.categoryOffers.CategoryOffersActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OffersFragment : Fragment() {

    private lateinit var binding: FragmentOffersBinding
    private val viewModel by viewModels<OffersViewModel>()
    private val parentViewModel: DashboardViewModel by activityViewModels()

    private var offerAdapter: OfferAdapter? = null
    private var categoryAdapter: CategoryAdapter? = null
    private var promotedOffersAdapter: PromotedOffersAdapter? = null
    private val carouselHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offers, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPullToRefreshPage()
        initCategoryRv()
        initOffersRv()
        initObservers()
        viewModel.onViewCreated()
    }

    private fun initPullToRefreshPage() {
        binding.pullToRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        binding.pullToRefreshLayout.setOnRefreshListener {
            parentViewModel.clearOffers()
            binding.pullToRefreshLayout.isEnabled = requireContext().isNetworkConnected()
            resetPagination()
            parentViewModel.getData()
        }
    }

    private fun initObservers() {
        viewModel.categoriesResponse.observe(viewLifecycleOwner) { categories ->
            updateCategoriesAdapter(categories)
        }

        viewModel.promotedOffersResponse.observe(viewLifecycleOwner) { offers ->
            initPromotedOfferRv(offers.toMutableList())
        }

        viewModel.offersResponse.observe(viewLifecycleOwner) { offers ->
            hidePullRefreshLayout()
            handleOfferResponse(offers)
        }

        viewModel.paginationOffersResponse.observe(viewLifecycleOwner) {
            updatePaginationAdapter(it)
        }

        viewModel.showShimmerAnimation.observe(viewLifecycleOwner) { shouldStart ->
            startStopShimmer(shouldStart)
        }

        viewModel.seeAllCategoriesEvent.observe(viewLifecycleOwner) {
            openAllCategoriesActivity()
        }

        viewModel.categorySelected.observe(viewLifecycleOwner) {
            handleCategorySelected(it)
        }

        viewModel.shouldSaveOfferPage.observe(viewLifecycleOwner) {
            handleSaveOffer(it)
        }

        viewModel.shouldShowSaveGuestMode.observe(viewLifecycleOwner) {
            showGuestModeBottomSheet(childFragmentManager, getString(R.string.guest_save_offer))
        }

        viewModel.openOfferDetails.observe(viewLifecycleOwner) { offerId ->
            requireContext().launchActivity<OfferDetailsActivity> {
                putExtra(SELECTED_OFFER_ID, offerId)
            }
        }
    }

    private fun handleOfferResponse(offers: List<OfferModel>) {
        offerAdapter?.addNewOffers(offers, true)
    }

    private fun hidePullRefreshLayout() {
        if (binding.pullToRefreshLayout.isRefreshing) {
            binding.pullToRefreshLayout.isRefreshing = false
        }
    }

    private fun initCategoryRv() {
        categoryAdapter =
            CategoryAdapter(requireContext(), mutableListOf(), viewModel.onCategorySelected, shouldHighlight = true)
        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun handleSaveOffer(it: OfferModel) {
        if (requireContext().isNetworkConnected()) {
            viewModel.saveOfferToFavorites(it)
        } else {
            requireActivity().showInternetConnectionDialog(false)
        }
    }

    private fun initPromotedOfferRv(offerItem: MutableList<OfferModel>) {
        promotedOffersAdapter = PromotedOffersAdapter(
            requireContext(),
            offerItem,
            this::onOfferSelected,
        )
        binding.carouselViewPager.adapter = promotedOffersAdapter
        binding.carouselViewPager.currentItem = 0
        binding.carouselViewPager.autoScroll(carouselHandler)
    }

    private fun initOffersRv() {
        offerAdapter = OfferAdapter(
            requireContext(),
            mutableListOf(),
            this::onOfferSelected,
            viewModel.shouldSaveOfferPublishSubject,
            viewModel.isUserLoggedIn.value ?: false
        )
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvOffers.apply {
            this.layoutManager = layoutManager
            adapter = offerAdapter
        }

        binding.rvOffers.itemAnimator = null
        binding.nestedScroll.initPagination(layoutManager, ::loadMoreItems)
    }

    private fun loadMoreItems() {
        binding.nestedScroll.isLoading = true
        offerAdapter?.addLoadingFooter()
        viewModel.loadMoreOffers()
    }

    private fun updatePaginationAdapter(offers: List<OfferModel>) {
        offerAdapter?.removeLoadingFooter()
        offerAdapter?.addNewOffers(offers)
        if (viewModel.currentPage == viewModel.totalPagesCall) {
            binding.nestedScroll.isLastPage = true
        }
        binding.nestedScroll.isLoading = false
    }

    private fun handleCategorySelected(it: CategoryModel) {
        when {
            it.noOffers.orZero() > 1 -> {
                requireContext().launchActivity<CategoryOffersActivity> {
                    putExtra(AllCategoriesActivity.SELECTED_CATEGORY_ID, it.id)
                }
            }
            it.noOffers.orZero() == 1 -> {
                viewModel.getFilteredCategory(it)
            }
            else -> {}
        }
    }

    private fun resetPagination() {
        viewModel.currentPage = 0
        binding.nestedScroll.isLoading = false
        binding.nestedScroll.isLastPage = false
    }

    private fun updateCategoriesAdapter(categories: List<CategoryModel>) {
        categoryAdapter?.updateCategoriesList(categories)
    }

    private fun startStopShimmer(shouldStart: Boolean) {
        if (shouldStart) {
            binding.inclShimmerOffers.shimmerContainer.startShimmer()
        } else {
            binding.inclShimmerOffers.shimmerContainer.stopShimmer()
        }
    }

    private fun openAllCategoriesActivity() {
        requireContext().launchActivity<AllCategoriesActivity>()
    }

    private fun onOfferSelected(offerId: Long) {
        requireContext().launchActivity<OfferDetailsActivity> {
            putExtra(SELECTED_OFFER_ID, offerId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        carouselHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance() = OffersFragment()
    }
}