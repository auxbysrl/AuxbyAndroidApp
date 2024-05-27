package com.fivedevs.auxby.screens.addOffer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentPromoteOfferBinding
import com.fivedevs.auxby.domain.models.PromoteOfferModel
import com.fivedevs.auxby.domain.models.enums.PromoteOfferEnum
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setWhiteStatusBar
import com.fivedevs.auxby.screens.addOffer.AddOfferViewModel
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.AdapterPromoteOffer
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity

class PromoteOfferFragment(val isFromAddOffer: Boolean = false) : Fragment() {

    private lateinit var binding: FragmentPromoteOfferBinding
    private val parentViewModel: AddOfferViewModel by activityViewModels()
    private var selectedPromo = PromoteOfferEnum.ONE_WEEK

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_promote_offer, container, false)
        binding.viewModel = parentViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        requireActivity().setWhiteStatusBar()
        initListeners()
        initPromoteRv()
    }

    private fun initViews() {
        if (isFromAddOffer) {
            binding.inclToolbar.materialToolbar.navigationIcon = null
        } else {
            binding.btnContinueWithoutPromote.text = getString(R.string.cancel)
        }
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            handleBackPressed()
        }

        binding.btnPromoteOffer.setOnClickListenerWithDelay {
            handleBackPressed()
        }

        binding.btnContinueWithoutPromote.setOnClickListenerWithDelay {
            handleBackPressed()
        }
    }

    private fun handleBackPressed() {
        if (activity is OfferDetailsActivity) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            requireActivity().finish()
        }
    }

    private fun initPromoteRv() {
        val promoteOfferAdapter = AdapterPromoteOffer(getListOfPromotions(), requireContext(), ::onPromoteClicked)
        binding.rvPromoteOffer.apply {
            adapter = promoteOfferAdapter
            setHasFixedSize(true)
        }
    }

    private fun getListOfPromotions(): ArrayList<PromoteOfferModel> {
        val listOfPromotions = arrayListOf<PromoteOfferModel>()
        listOfPromotions.add(PromoteOfferModel(R.drawable.ic_promote_24_hours, getString(R.string.promote_for_24_hours)))
        listOfPromotions.add(PromoteOfferModel(R.drawable.ic_promote_7_days, getString(R.string.promote_for_7_days), isChecked = true))
        listOfPromotions.add(PromoteOfferModel(R.drawable.ic_promote_30_days, getString(R.string.promote_for_30_days)))
        return listOfPromotions
    }

    private fun onPromoteClicked(selectedPosition: Int) {
        val promoteOfferTypes = PromoteOfferEnum.values()
        selectedPromo = promoteOfferTypes[selectedPosition]
    }

    companion object {
        fun newInstance(isFromAddOffer: Boolean) = PromoteOfferFragment(isFromAddOffer)
    }
}