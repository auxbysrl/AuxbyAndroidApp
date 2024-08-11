package com.fivedevs.auxby.screens.dashboard.bids.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentMyOffersBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants.IS_INACTIVE_OFFER
import com.fivedevs.auxby.domain.utils.Constants.MY_BIDS_TYPE
import com.fivedevs.auxby.domain.utils.Constants.MY_OFFERS_TYPE
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.screens.dashboard.bids.MyOffersViewModel
import com.fivedevs.auxby.screens.dashboard.bids.adapters.MyBidsPagerAdapter.Companion.ACTIVE_OFFERS_TAB_POS
import com.fivedevs.auxby.screens.dashboard.bids.adapters.MyBidsPagerAdapter.Companion.INACTIVE_OFFERS_TAB_POS
import com.fivedevs.auxby.screens.dashboard.offers.adapters.OfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyOffersTabFragment(private val tabPosition: Int = 0) : Fragment() {

    private lateinit var binding: FragmentMyOffersBinding
    private val shareViewModel: MyOffersViewModel by viewModels({ requireParentFragment() })
    private var offerAdapter: OfferAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_offers, container, false)
        binding.viewModel = this.shareViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        removeBottomPadding()
        initEmptyStateView()
        initOffersAdapter()
        initObservers()
    }

    private fun removeBottomPadding() {
        if (shareViewModel.fragmentType == MY_OFFERS_TYPE) {
            binding.rvOffers.setPadding(0, 0, 0, 0)
        }
    }

    private fun initOffersAdapter() {
        offerAdapter = OfferAdapter(
            requireContext(),
            mutableListOf(),
            this::onOfferSelected,
            shareViewModel.shouldSaveOfferPublishSubject,
            shareViewModel.isUserLoggedIn.value ?: false
        )
        binding.rvOffers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = offerAdapter
        }
    }

    private fun initObservers() {
        when (tabPosition) {
            ACTIVE_OFFERS_TAB_POS -> {
                observeActiveOffers()
            }
            INACTIVE_OFFERS_TAB_POS -> {
                observeInactiveOffers()
            }
        }

        shareViewModel.shouldSaveOfferPage.observe(viewLifecycleOwner) {
            handleSaveOffer(it)
        }
    }

    private fun handleSaveOffer(it: OfferModel) {
        if (requireContext().isNetworkConnected()) {
            shareViewModel.saveOfferToFavorites(it)
        } else {
            requireActivity().showInternetConnectionDialog(false)
        }
    }

    private fun observeActiveOffers() {
        shareViewModel.activeOffers.observe(viewLifecycleOwner) { activeOffers ->
            updateAdapterList(activeOffers)
        }
    }

    private fun observeInactiveOffers() {
        shareViewModel.inactiveOffers.observe(viewLifecycleOwner) { inactiveOffers ->
            offerAdapter?.isInactiveTab = true
            updateAdapterList(inactiveOffers)
        }
    }

    private fun updateAdapterList(offersList: MutableList<OfferModel>) {
        offerAdapter?.isMyOffersActivity = shareViewModel.fragmentType == MY_OFFERS_TYPE
        if (offersList.isEmpty()) {
            binding.inclEmptyListView.root.show()
            binding.rvOffers.hide()
        } else {
            binding.inclEmptyListView.root.hide()
            binding.rvOffers.show()
            shareViewModel.localUser.value?.let {
                offerAdapter?.user = it
            }
            offerAdapter?.updateOffersList(offersList)
        }
    }

    private fun initEmptyStateView() {
        binding.emptyListTitle = getEmptyListText()
    }

    private fun getEmptyListText(): String {
        return when (tabPosition) {
            ACTIVE_OFFERS_TAB_POS -> getString(R.string.no_active_offers)
            else -> getString(R.string.no_inactive_offers)
        }
    }

    private fun onOfferSelected(offerId: Long) {
        requireContext().launchActivity<OfferDetailsActivity> {
            putExtra(SELECTED_OFFER_ID, offerId)
            putExtra(IS_INACTIVE_OFFER, shareViewModel.fragmentType == MY_BIDS_TYPE)
        }
    }

    companion object {
        fun newInstance(tabPosition: Int) =
            MyOffersTabFragment(tabPosition)
    }
}