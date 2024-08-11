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
import com.fivedevs.auxby.domain.models.PromoteOfferRequest
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.models.enums.PromoteOfferEnum
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.DateUtils.Companion.currentLocale
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setWhiteStatusBar
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.fivedevs.auxby.screens.addOffer.AddOfferViewModel
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.AdapterPromoteOffer
import com.fivedevs.auxby.screens.buyCoins.BuyCoinsActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class PromoteOfferFragment(val isFromAddOffer: Boolean = false, val offerId: Long = 0L) : Fragment() {

    private lateinit var binding: FragmentPromoteOfferBinding
    private val parentViewModel: AddOfferViewModel by activityViewModels()
    private var selectedPromo = PromoteOfferEnum.ONE_WEEK

    // used to calculate the expiry date for promoted offer model
    private val currentDate: Date = Calendar.getInstance().time
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", currentLocale)

    private var noCoinsBalanceDialog: GenericDialog? = null
    private var successfullyPromotedDialog: GenericDialog? = null
    private var selectedPromoOfferModel: PromoteOfferRequest? = null

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
        initObservers()
        initDialogs()
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
            when (selectedPromo) {
                PromoteOfferEnum.ONE_DAY -> {
                    setPromotedForOneDay()
                }

                PromoteOfferEnum.ONE_WEEK -> {
                    setPromotedForOneWeek()
                }

                PromoteOfferEnum.ONE_MONTH -> {
                    setPromotedForOneMonth()
                }
            }
        }

        binding.btnContinueWithoutPromote.setOnClickListenerWithDelay {
            handleBackPressed()
        }
    }

    private fun initObservers() {
        parentViewModel.offerPromotedEvent.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showPromotedSuccessfullyDialog()
            } else {
                toast(resources.getString(R.string.something_went_wrong))
                handleBackPressed()
            }
        }
    }

    private fun showNotEnoughCoinsDialog() {
        noCoinsBalanceDialog?.show(
            childFragmentManager,
            GenericDialog.PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG
        )
    }

    private fun showPromotedSuccessfullyDialog() {
        successfullyPromotedDialog?.show(
            childFragmentManager,
            GenericDialog.DEFAULT_DIALOG_TAG
        )
    }

    private fun handleBackPressed() {
        if (activity is OfferDetailsActivity) {
            parentViewModel.getOfferById(offerId)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            parentViewModel.returnToDashboard.value = true
        }
    }

    private fun initPromoteRv() {
        val promoteOfferAdapter =
            AdapterPromoteOffer(getListOfPromotions(), requireContext(), ::onPromoteClicked)
        binding.rvPromoteOffer.apply {
            adapter = promoteOfferAdapter
            setHasFixedSize(true)
        }
    }

    private fun getListOfPromotions(): ArrayList<PromoteOfferModel> {
        val listOfPromotions = arrayListOf<PromoteOfferModel>()
        listOfPromotions.add(
            PromoteOfferModel(
                R.drawable.ic_promote_24_hours,
                getString(R.string.promote_for_24_hours)
            )
        )
        listOfPromotions.add(
            PromoteOfferModel(
                R.drawable.ic_promote_7_days,
                getString(R.string.promote_for_7_days),
                isChecked = true
            )
        )
        listOfPromotions.add(
            PromoteOfferModel(
                R.drawable.ic_promote_30_days,
                getString(R.string.promote_for_30_days)
            )
        )

        // Pro-populate the selectedPromo. 1 is ONE_WEEK promo package
        onPromoteClicked(1)

        return listOfPromotions
    }

    private fun onPromoteClicked(selectedPosition: Int) {
        val promoteOfferTypes = PromoteOfferEnum.entries.toTypedArray()
        selectedPromo = promoteOfferTypes[selectedPosition]
        binding.btnPromoteOffer.text = resources.getString(R.string.promote_for_coins, selectedPromo.getRequiredCoins().toString())
    }

    private fun setPromotedForOneDay() {
        if (hasEnoughCoins(PromoteOfferEnum.ONE_DAY.getRequiredCoins()))
            showNotEnoughCoinsDialog()
        else {
            selectedPromoOfferModel = PromoteOfferRequest(
                requiredCoins = PromoteOfferEnum.ONE_DAY.getRequiredCoins(),
                expirationDate = dateFormat.format(DateUtils().addDaysToDate(currentDate, 1))
            )

            selectedPromoOfferModel?.let {
                parentViewModel.promoteCurrentOffer(it, offerId)
            }
        }
    }

    private fun setPromotedForOneWeek() {
        if (hasEnoughCoins(PromoteOfferEnum.ONE_DAY.getRequiredCoins()))
            showNotEnoughCoinsDialog()
        else {
            selectedPromoOfferModel = PromoteOfferRequest(
                requiredCoins = PromoteOfferEnum.ONE_WEEK.getRequiredCoins(),
                expirationDate = dateFormat.format(DateUtils().addDaysToDate(currentDate, 7))
            )

            selectedPromoOfferModel?.let {
                parentViewModel.promoteCurrentOffer(it, offerId)
            }
        }
    }

    private fun setPromotedForOneMonth() {
        if (hasEnoughCoins(PromoteOfferEnum.ONE_DAY.getRequiredCoins()))
            showNotEnoughCoinsDialog()
        else {
            selectedPromoOfferModel = PromoteOfferRequest(
                requiredCoins = PromoteOfferEnum.ONE_MONTH.getRequiredCoins(),
                expirationDate = dateFormat.format(DateUtils().addDaysToDate(currentDate, 30))
            )

            selectedPromoOfferModel?.let {
                parentViewModel.promoteCurrentOffer(it, offerId)
            }
        }
    }

    private fun hasEnoughCoins(requiredCoins: Int): Boolean {
        val availableCoins = parentViewModel.localUser.value?.availableCoins ?: 0
        return (availableCoins == 0 || availableCoins < requiredCoins)
    }

    private fun initDialogs() {
        noCoinsBalanceDialog = GenericDialog(::onBuyMoreClicked, DialogTypes.PUBLISH_OFFER_NO_COINS)
        successfullyPromotedDialog = GenericDialog(::handleBackPressed, DialogTypes.SUCCESSFULLY_PROMOTED)
    }

    private fun onBuyMoreClicked() {
        requireActivity().launchActivity<BuyCoinsActivity>()
        noCoinsBalanceDialog?.dismiss()
    }

    companion object {
        fun newInstance(isFromAddOffer: Boolean, offerId: Long) = PromoteOfferFragment(isFromAddOffer, offerId)
    }
}