package com.fivedevs.auxby.screens.search

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivitySearchBinding
import com.fivedevs.auxby.domain.models.AdvancedFiltersModel
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.hideKeyboard
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.domain.utils.views.LoaderDialog
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.SmallOfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.dashboard.offers.utils.GridSpacingItemDecoration
import com.fivedevs.auxby.screens.filterOffers.FilterOffersDialog
import com.fivedevs.auxby.screens.search.adapter.SearchSuggestionsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel by viewModels<SearchViewModel>()

    private val loaderDialog: LoaderDialog by lazy { LoaderDialog(this) }
    private var searchSuggestionsAdapter: SearchSuggestionsAdapter? = null
    private var offerAdapter: SmallOfferAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initListeners()
        initSearchViewListener()
        initSearchSuggestions()
        initOffersAdapter()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.apply {
            viewModel = this@SearchActivity.viewModel
            lifecycleOwner = this@SearchActivity
        }
    }

    private fun initObservers() {
        viewModel.searchSuggestionResponse.observe(this) {
            searchSuggestionsAdapter?.updateAdapter(it)
        }
        viewModel.showSearchNoResultMessage.observe(this) {
            if (it) showQueryNoResultsMessage()
        }

        viewModel.searchOffers.observe(this) {
            if (it.isEmpty()) {
                offerAdapter?.removeOffers()
            } else {
                viewModel.localUser.value?.let { user ->
                    offerAdapter?.user = user
                }
                offerAdapter?.updateOffersList(it)
                setSearchResultText(it)
            }
        }

        viewModel.apiErrorMessage.observe(this) {
            showErrorMessage()
        }

        viewModel.showLoader.observe(this) {
            if (it) loaderDialog.showDialog()
            else {
                loaderDialog.dismissDialog()
                binding.root.clearFocus()
                binding.root.hideKeyboard()
            }
        }

        viewModel.shouldSaveOfferPage.observe(this) {
            handleSaveOffer(it)
        }
    }

    private fun initListeners() {
        binding.appBarSearchView.toolbarArrowBack.setOnClickListenerWithDelay {
            finish()
        }

        binding.appBarSearchView.toolbarSearchFilterIcon.setOnClickListenerWithDelay {
            showFilterOffersDialog()
        }
    }

    private fun initSearchViewListener() {
        with(binding.appBarSearchView.searchView) {
            requestFocusFromTouch()
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.selectedSuggestion = null
                    viewModel.callSearchOffers(query.orEmpty(), listOf())
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        viewModel.onSearchViewAction.onNext(it)
                    }
                    return false
                }
            })
        }
    }

    private fun initSearchSuggestions() {
        searchSuggestionsAdapter = SearchSuggestionsAdapter(this, listOf(), viewModel.onSuggestionClickedPublishSubject)
        binding.rvSearchSuggestions.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchSuggestionsAdapter
            setRecycledViewPool(recycledViewPool)
        }
    }

    private fun initOffersAdapter() {
        offerAdapter = SmallOfferAdapter(
            this,
            mutableListOf(),
            this::onOfferSelected,
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

    }

    private fun setSearchResultText(offers: List<OfferModel>) {
        with(binding) {
            viewModel?.selectedSuggestion?.let { selectedSuggestion ->
                tvNoOffers.text = getString(R.string.n_offers_in, "${selectedSuggestion.noOffers.orZero()}")
                tvCategoryTitle.text = selectedSuggestion.category.label.getName(this@SearchActivity)
            } ?: run {
                tvNoOffers.text = getString(R.string.n_offers_in, "${offers.size}")
                tvCategoryTitle.text = viewModel?.selectedCategory?.ifEmpty { getString(R.string.all_categories) }
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

    private fun showErrorMessage() {
        AlerterUtils.showErrorAlert(this, resources.getString(R.string.something_went_wrong))
    }

    private fun showQueryNoResultsMessage() {
        binding.inclSearchNoResult.noResultsText.text = getString(R.string.no_search_text, "“${binding.appBarSearchView.searchView.query}”")
    }

    private fun onOfferSelected(offerId: Long) {
        launchActivity<OfferDetailsActivity> {
            putExtra(Constants.SELECTED_OFFER_ID, offerId)
        }
    }

    private fun showFilterOffersDialog() {
        val guestModeBottomSheet = FilterOffersDialog(viewModel.localFilters, ::applyFilters)
        guestModeBottomSheet.show(supportFragmentManager, guestModeBottomSheet.tag)
    }

    private fun applyFilters(filters: AdvancedFiltersModel, selectedCategory: String) {
        viewModel.selectedSuggestion = null
        viewModel.selectedCategory = selectedCategory
        viewModel.callSearchOffers(filters)
    }
}