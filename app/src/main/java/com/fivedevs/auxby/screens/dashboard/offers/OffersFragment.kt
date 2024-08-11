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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentOffersBinding
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.autoScroll
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.screens.dashboard.DashboardActivity.Companion.showGuestModeBottomSheet
import com.fivedevs.auxby.screens.dashboard.DashboardViewModel
import com.fivedevs.auxby.screens.dashboard.offers.adapters.CategoryAdapter
import com.fivedevs.auxby.screens.dashboard.offers.adapters.PromotedOffersAdapter
import com.fivedevs.auxby.screens.dashboard.offers.adapters.SmallOfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.categories.AllCategoriesActivity
import com.fivedevs.auxby.screens.dashboard.offers.categories.categoryOffers.CategoryOffersActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.dashboard.offers.utils.CarouselPageTransformer
import com.fivedevs.auxby.screens.dashboard.offers.utils.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OffersFragment : Fragment() {

    private lateinit var binding: FragmentOffersBinding
    private val viewModel by viewModels<OffersViewModel>()
    private val parentViewModel: DashboardViewModel by activityViewModels()

    private var offerAdapter: SmallOfferAdapter? = null
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
        binding.nestedScroll.recyclerView = binding.rvOffers
        initPullToRefreshPage()
        initCategoryRv()
        initObservers()
        initOffersRv()
        viewModel.onViewCreated()
    }

    private fun initPullToRefreshPage() {
        binding.pullToRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(), R.color.colorPrimary
            )
        )

        binding.pullToRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.showShimmerAnimation.value = true
//        parentViewModel.clearOffers()
        binding.pullToRefreshLayout.isEnabled = requireContext().isNetworkConnected()
        resetPagination()
        parentViewModel.getData()
        parentViewModel.getUser()
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
            if (offers.isNotEmpty()) {
                handleOfferResponse(offers)
            }
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

        parentViewModel.refreshDataEvent.observe(viewLifecycleOwner) {
            refreshData()
        }
    }

    private fun handleOfferResponse(offers: List<OfferModel>) {
        parentViewModel.localUser.value?.let {
            offerAdapter?.user = it
        }
        if (offers.isNotEmpty()) {
            offerAdapter?.updateOffersList(offers)
            //offerAdapter?.addNewOffers(offers, true)
        }
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
        binding.carouselViewPager.setPageTransformer(CarouselPageTransformer())
        binding.carouselViewPager.currentItem = 1
        binding.carouselViewPager.offscreenPageLimit = 3
        binding.carouselViewPager.autoScroll(carouselHandler)
    }

    private fun initOffersRv() {
        offerAdapter = SmallOfferAdapter(
            requireContext(),
            mutableListOf(),
            this::onOfferSelected,
            viewModel.shouldSaveOfferPublishSubject,
            viewModel.isUserLoggedIn.value ?: false
        )
        val layoutManager = GridLayoutManager(requireContext(), 2)
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
        binding.nestedScroll.initPagination(layoutManager, ::loadMoreItems)
    }

    private fun loadMoreItems() {
        viewModel.currentPage += 1
        if (viewModel.currentPage < viewModel.totalPagesCall || viewModel.totalPagesCall == 0) {
            binding.nestedScroll.isLoading = true
            offerAdapter?.addLoadingFooter()
            viewModel.loadMoreOffers()
        } else {
            Timber.d("Reached the end of the pages")
        }
    }

    private fun updatePaginationAdapter(offers: List<OfferModel>) {
        offerAdapter?.removeLoadingFooter()

        if (offers.isNotEmpty()) {
            offerAdapter?.addNewOffers(offers)
        }

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