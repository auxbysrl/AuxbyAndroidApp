package com.fivedevs.auxby.screens.dashboard.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentFavouriteBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.isNetworkConnected
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.showInternetConnectionDialog
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.OfferAdapter
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteFragment : Fragment() {

    private lateinit var binding: FragmentFavouriteBinding
    private val viewModel by viewModels<FavouriteViewModel>()
    private var offerAdapter: OfferAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFavouriteOffers()
        initObservers()
        initSavedOffersRv()
        initListeners()
    }

    private fun initListeners() {
        binding.inclGuestFavoriteOffers.btnGetStarted.setOnClickListenerWithDelay {
            requireActivity().launchActivityWithFinish<LoginActivity> {  }
        }
    }

    private fun getFavouriteOffers() {
        if (viewModel.isUserLoggedIn.value == true) {
            viewModel.onViewCreated()
        } else {
            showGuestFavouriteView()
        }
    }

    private fun showGuestFavouriteView() {
        binding.inclGuestFavoriteOffers.root.show()
    }

    private fun initSavedOffersRv() {
        offerAdapter = OfferAdapter(
            requireContext(),
            mutableListOf(),
            this::onOfferSelected,
            viewModel.shouldSaveOfferPublishSubject,
            viewModel.isUserLoggedIn.value ?: false
        )
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedOffers.apply {
            this.layoutManager = layoutManager
            adapter = offerAdapter
        }
    }

    private fun initObservers() {
        viewModel.savedOffers.observe(viewLifecycleOwner) { offers ->
            populateSavedOffersList(offers)
        }

        viewModel.shouldSaveOfferPage.observe(viewLifecycleOwner) {
            handleSaveOffer(it)
        }
    }

    private fun populateSavedOffersList(offers: List<OfferModel>) {
        if (offers.isEmpty()) {
            showNoOffersMessage()
        } else {
            hideNoOffersMessage()
            viewModel.localUser.value?.let {
                offerAdapter?.user = it
            }
            offerAdapter?.updateOffersList(offers)
        }
    }

    private fun hideNoOffersMessage() {
        binding.inclNoFavoriteOffers.root.hide()
        binding.rvSavedOffers.show()
    }

    private fun showNoOffersMessage() {
        binding.inclNoFavoriteOffers.root.show()
        binding.rvSavedOffers.hide()
    }

    private fun onOfferSelected(offerId: Long) {
        requireContext().launchActivity<OfferDetailsActivity> {
            putExtra(SELECTED_OFFER_ID, offerId)
        }
    }

    private fun handleSaveOffer(it: OfferModel) {
        if (requireContext().isNetworkConnected()) {
            viewModel.saveOfferToFavorites(it)
        } else {
            requireActivity().showInternetConnectionDialog(false)
        }
    }

    companion object {
        fun newInstance() = FavouriteFragment()
    }
}