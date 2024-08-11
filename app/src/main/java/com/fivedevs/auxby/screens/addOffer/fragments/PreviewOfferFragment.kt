package com.fivedevs.auxby.screens.addOffer.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentPreviewOfferBinding
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.models.enums.ConditionTypeEnum
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.OfferUtils.getMultipartOfferPhotos
import com.fivedevs.auxby.domain.utils.OfferUtils.getOfferPreviewPrice
import com.fivedevs.auxby.domain.utils.Utils.setCollapsingToolbarTitle
import com.fivedevs.auxby.domain.utils.ZoomOutPageTransformer
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.isTextEllipsized
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.loadImage
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.addOffer.AddOfferViewModel
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.AdapterOfferTags
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.DotIndicatorPager2Adapter
import com.fivedevs.auxby.screens.buyCoins.BuyCoinsActivity
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import com.fivedevs.auxby.screens.dialogs.GenericDialog.Companion.PUBLISH_ACCOUNT_DIALOG_TAG
import com.fivedevs.auxby.screens.dialogs.GenericDialog.Companion.PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG
import com.google.android.material.appbar.AppBarLayout
import qiu.niorgai.StatusBarCompat

class PreviewOfferFragment : Fragment() {

    private lateinit var binding: FragmentPreviewOfferBinding
    private val parentViewModel: AddOfferViewModel by activityViewModels()
    private var adapterOfferTags: AdapterOfferTags? = null
    private var noCoinsBalanceDialog: GenericDialog? = null
    private var confirmOfferPublishDialog: GenericDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_preview_offer, container, false)
        binding.parentViewModel = parentViewModel
        binding.lifecycleOwner = this@PreviewOfferFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentStatusBar()
        initListeners()
        initObservers()
        populateOfferPreview()
        initAppBarLayoutListener()
        initDialogs()
    }

    override fun onResume() {
        super.onResume()
        checkIfWasLightStatusBar()
    }

    private fun setTransparentStatusBar() {
        StatusBarCompat.setStatusBarColorForCollapsingToolbar(
            requireActivity(),
            binding.appbar,
            binding.collapsingToolbar,
            binding.toolbar,
            Color.TRANSPARENT
        )
    }

    private fun checkIfWasLightStatusBar() {
        if (parentViewModel.isLightStatusBar.value == true)
            StatusBarCompat.changeToLightStatusBar(requireActivity())
        else
            StatusBarCompat.cancelLightStatusBar(requireActivity())
    }

    private fun initAppBarLayoutListener() {
        binding.appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    StatusBarCompat.changeToLightStatusBar(requireActivity())
                    isShow = true
                    parentViewModel.isLightStatusBar.value = true
                } else if (isShow) {
                    StatusBarCompat.cancelLightStatusBar(requireActivity())
                    isShow = false
                    parentViewModel.isLightStatusBar.value = false
                }
            }
        })
    }

    private fun populateOfferPreview() {
        if (parentViewModel.offerRequestModel.auctionEndDate.isNotEmpty()) {
            binding.inclOfferEndDate.tvEndDate.text =
                DateUtils().getRemainingTimeForAuction(parentViewModel.offerRequestModel.auctionEndDate, DateUtils.FORMAT_YYYY_MM_DD)
        }
        binding.tvPrice.text =
            getOfferPreviewPrice(requireContext(), parentViewModel.offerRequestModel)
        binding.viewOfferStatusContainer.tvOfferStatus.text =
            getString(R.string.this_is_a_preview_of_your_offer)
        binding.tvOfferDate.text =
            DateUtils().getFormattedDateForPreview(parentViewModel.offerRequestModel.publishDate)
        binding.ivUserAvatar.loadImage(
            parentViewModel.localUser.value?.avatar ?: Constants.EMPTY,
            R.drawable.ic_profile_placeholder,
            circleCrop = true
        )
        binding.btnPublishOffer.text = resources.getString(
            R.string.publish_for_n_coins,
            getOfferCost().toString()
        )
        addDescriptionAnimation()
        initTagsRv()
        setCollapsingToolbarTitle(binding.appbar, binding.toolbarTitle)
        initPhotosViewPager(parentViewModel.offerRequestModel.photos)
    }

    private fun initTagsRv() {
        val offerTags: MutableList<CategoryFieldsValue> = mutableListOf()
        parentViewModel.offerRequestModel.categoryDetails.forEach {
            offerTags.add(CategoryFieldsValue(it.key, it.value))
        }

//        if (offerTags.isEmpty()) {
//            binding.cvTagsContainer.hide()
//            return
//        }

        val userSelectedLanguage = parentViewModel.selectedLanguage
        val categoryTags = parentViewModel.categoryDetailsResponse.value?.categoryDetails
        val finalList: MutableList<CategoryFieldsValue> = offerTags

        finalList.forEachIndexed { index, categoryFieldsValue ->
            val translatedKey =
                categoryTags?.firstOrNull { categoryFieldsValue.key.lowercase().equals(it.name.lowercase(), true) }?.label?.firstOrNull { it.language == userSelectedLanguage }?.translation
            translatedKey?.let {
                finalList[index].apply { key = it }
            }
        }
        finalList.add(
            offerTags.size,
            CategoryFieldsValue(
                resources.getString(R.string.condition).replace("*", ""),
                ConditionTypeEnum.valueOf(parentViewModel.offerRequestModel.conditionType.uppercase()).getConditionTranslatedName(requireContext())
            )
        )

        adapterOfferTags = AdapterOfferTags(finalList)
        binding.rvCategoryTags.apply {
            adapter = adapterOfferTags
            layoutManager = GridLayoutManager(this@PreviewOfferFragment.context, 2)
        }
    }

    private fun addDescriptionAnimation() {
        binding.tvDescription.apply {
            setInterpolator(OvershootInterpolator())
            expandInterpolator = OvershootInterpolator()
            collapseInterpolator = OvershootInterpolator()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.tvDescription.layout.isTextEllipsized()) binding.btnReadMore.show()
            else binding.btnReadMore.hide()
        }, 200)
    }

    private fun initPhotosViewPager(photos: MutableList<OfferPhoto>?) {
        photos?.let {
            if (photos.isEmpty()) photos.add(OfferPhoto(""))
            photos.filter { it.isCover }
            val adapter = DotIndicatorPager2Adapter(photos)
            val zoomOutPageTransformer = ZoomOutPageTransformer()

            binding.apply {
                vpOfferImages.adapter = adapter
                vpOfferImages.setPageTransformer { page, position ->
                    zoomOutPageTransformer.transformPage(page, position)
                }
                vpDotsIndicator.attachTo(vpOfferImages)
            }
        }
    }

    private fun initObservers() {
        parentViewModel.shouldShowWholeDescription.observe(viewLifecycleOwner) {
            binding.btnReadMore.setText(if (binding.tvDescription.isExpanded) R.string.read_more else R.string.read_less)
            binding.tvDescription.toggle()
        }
    }

    private fun initListeners() {
        with(binding) {
            btnBackArrow.setOnClickListenerWithDelay {
                onBackPressed()
            }

            btnReturnToEdit.setOnClickListenerWithDelay {
                onBackPressed()
            }

            btnPublishOffer.setOnClickListenerWithDelay {
                checkForCoins()
            }
        }
    }

    private fun initDialogs() {
        noCoinsBalanceDialog = GenericDialog(::onBuyMoreClicked, DialogTypes.PUBLISH_OFFER_NO_COINS)
        confirmOfferPublishDialog = GenericDialog(::onConfirmClicked, DialogTypes.PUBLISH_OFFER)

        parentViewModel.localUser.value?.availableCoins?.let {
            val offerCost = parentViewModel.categoryDetailsResponse.value?.addOfferCost ?: 0
            noCoinsBalanceDialog?.setCoinsAmount(it, offerCost)
            confirmOfferPublishDialog?.setCoinsAmount(it, offerCost)
        }
    }

    private fun checkForCoins() {
        val availableCoins = parentViewModel.localUser.value?.availableCoins ?: 0
        noCoinsBalanceDialog?.setCoinsAmount(availableCoins, getOfferCost())
        confirmOfferPublishDialog?.setCoinsAmount(availableCoins, getOfferCost())
        if (availableCoins == 0 || availableCoins < getOfferCost()) {
            showNotEnoughCoinsDialog()
        } else {
            showPublishOfferConfirmDialog()
        }
    }

    private fun getOfferCost(): Int {
        var offerCost = parentViewModel.categoryDetailsResponse.value?.addOfferCost ?: 0
        if (parentViewModel.offerRequestModel.auctionEndDate.isNotEmpty()) {
            val auctionDays = DateUtils().calculateDaysLeft(parentViewModel.offerRequestModel.auctionEndDate).toInt()
            offerCost = if (auctionDays <= 30) {
                offerCost
            } else if (auctionDays in 31..60) {
                offerCost * 2
            } else {
                offerCost * 3
            }
        }
        return offerCost
    }

    private fun showPublishOfferConfirmDialog() {
        confirmOfferPublishDialog?.show(childFragmentManager, PUBLISH_ACCOUNT_DIALOG_TAG)
    }

    private fun showNotEnoughCoinsDialog() {
        noCoinsBalanceDialog?.show(childFragmentManager, PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG)
    }

    private fun onBuyMoreClicked() {
        requireActivity().launchActivity<BuyCoinsActivity>()
        noCoinsBalanceDialog?.dismiss()
    }

    private fun onConfirmClicked() {
        parentViewModel.categoryDetailsResponse.value?.addOfferCost?.let {
            parentViewModel.offerRequestModel.requiredCoins = it
        }
        parentViewModel.publishNewOffer(
            getMultipartOfferPhotos(
                parentViewModel.offerRequestModel.photos.filter { it.url.isNotEmpty() },
                requireContext()
            )
        )
        confirmOfferPublishDialog?.dismiss()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}