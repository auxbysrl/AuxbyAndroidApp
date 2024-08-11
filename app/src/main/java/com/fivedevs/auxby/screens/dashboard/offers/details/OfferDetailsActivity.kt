package com.fivedevs.auxby.screens.dashboard.offers.details

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityOfferDetailsBinding
import com.fivedevs.auxby.domain.models.*
import com.fivedevs.auxby.domain.models.enums.*
import com.fivedevs.auxby.domain.utils.*
import com.fivedevs.auxby.domain.utils.Constants.FACEBOOK_PAGE
import com.fivedevs.auxby.domain.utils.Constants.INSTAGRAM_PAGE
import com.fivedevs.auxby.domain.utils.Constants.IS_EDIT_MODE
import com.fivedevs.auxby.domain.utils.Constants.IS_INACTIVE_OFFER
import com.fivedevs.auxby.domain.utils.Constants.OFFER_ID
import com.fivedevs.auxby.domain.utils.Constants.SELECTED_OFFER_ID
import com.fivedevs.auxby.domain.utils.Constants.TIKTOK_PAGE
import com.fivedevs.auxby.domain.utils.OfferUtils.getFullOwnerName
import com.fivedevs.auxby.domain.utils.OfferUtils.getFullUsername
import com.fivedevs.auxby.domain.utils.OfferUtils.getHighestBidWithCurrency
import com.fivedevs.auxby.domain.utils.OfferUtils.getOfferDate
import com.fivedevs.auxby.domain.utils.OfferUtils.getOfferDetailsFavoriteIconByState
import com.fivedevs.auxby.domain.utils.OfferUtils.getOfferDetailsPrice
import com.fivedevs.auxby.domain.utils.OfferUtils.getUserLastSeenTime
import com.fivedevs.auxby.domain.utils.OfferUtils.getYourBid
import com.fivedevs.auxby.domain.utils.OfferUtils.getYourBidWithCurrency
import com.fivedevs.auxby.domain.utils.Utils.redirectToBrowserLink
import com.fivedevs.auxby.domain.utils.Utils.setCollapsingToolbarTitle
import com.fivedevs.auxby.domain.utils.Utils.shareLink
import com.fivedevs.auxby.domain.utils.Utils.showPhoneDialer
import com.fivedevs.auxby.domain.utils.extensions.*
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.domain.utils.views.LoaderDialog
import com.fivedevs.auxby.screens.addOffer.AddOfferActivity
import com.fivedevs.auxby.screens.addOffer.fragments.PromoteOfferFragment
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.AdapterOfferTags
import com.fivedevs.auxby.screens.addOffer.fragments.adapters.DotIndicatorPager2Adapter
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.buyCoins.BuyCoinsActivity
import com.fivedevs.auxby.screens.dashboard.DashboardActivity.Companion.showGuestModeBottomSheet
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity
import com.fivedevs.auxby.screens.dashboard.offers.bottomSheets.BidHistoryBottomSheet
import com.fivedevs.auxby.screens.dashboard.offers.bottomSheets.PlaceBidBottomSheet
import com.fivedevs.auxby.screens.dashboard.offers.bottomSheets.ReportOfferBottomSheet
import com.fivedevs.auxby.screens.dashboard.offers.imageViewer.ImageViewerActivity
import com.fivedevs.auxby.screens.dashboard.offers.userOffers.ViewUserOffersActivity
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import com.fivedevs.auxby.screens.yourOffers.YourOffersActivity
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import qiu.niorgai.StatusBarCompat
import qiu.niorgai.StatusBarCompat.setStatusBarColorForCollapsingToolbar

@AndroidEntryPoint
class OfferDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityOfferDetailsBinding
    private val viewModel by viewModels<OfferDetailsViewModel>()

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val loaderDialog: LoaderDialog by lazy { LoaderDialog(this) }
    private val noCoinsBalanceDialog: GenericDialog by lazy {
        GenericDialog(
            ::onBuyMoreClicked,
            DialogTypes.PUBLISH_OFFER_NO_COINS
        )
    }
    private val closeAuctionDialog: GenericDialog by lazy {
        GenericDialog(
            ::onCloseAuctionConfirmClicked,
            DialogTypes.CLOSE_AUCTION
        )
    }
    private val deleteConfirmationDialog: GenericDialog by lazy {
        GenericDialog(
            ::onDeleteOfferConfirmation,
            DialogTypes.DELETE_OFFER
        )
    }
    private val disableConfirmationDialog: GenericDialog by lazy {
        GenericDialog(
            ::onDisableOfferConfirmation,
            DialogTypes.DISABLE_OFFER
        )
    }

    private val enableConfirmationDialog: GenericDialog by lazy {
        GenericDialog(
            ::onEnableOfferConfirmation,
            DialogTypes.ENABLE_OFFER
        )
    }

    private val placeBidBottomSheet: PlaceBidBottomSheet by lazy {
        PlaceBidBottomSheet(
            viewModel.user,
            viewModel.offerDetailsModel,
            viewModel.categoryDetailsModel.placeBidCost,
            ::onPlaceBidClicked
        )
    }
    private val contactUsInfoDialog: GenericDialog by lazy {
        GenericDialog(
            null,
            DialogTypes.CONTACT_US
        )
    }

    private val reportOfferBottomSheet: ReportOfferBottomSheet by lazy {
        ReportOfferBottomSheet(::sendOfferReport)
    }
    private val bidHistoryBottomSheet: BidHistoryBottomSheet by lazy {
        BidHistoryBottomSheet(viewModel.offerDetailsModel)
    }
    private var offerID: Long = 0
    private var isInactiveOffer = false
    var lastCarouselImagePosition = 0
    private var offerImagesAdapter: DotIndicatorPager2Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initToolbar()
        initObservers()
        initListeners()
        retrieveOfferIdFromBundle()
        initActivityResultLauncher()
        initPhotosViewPager()
    }

    private fun initActivityResultLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                selectCarouselImage(result)
            }
    }

    private fun initToolbar() {
        setCollapsingToolbarTitle(binding.appbar, binding.toolbarTitle)
        setStatusBarColorForCollapsingToolbar(
            this,
            binding.appbar,
            binding.collapsingToolbar,
            binding.toolbar, Color.TRANSPARENT
        )
        initAppBarLayoutListener()
    }

    private fun retrieveOfferIdFromBundle() {
        val offerId = intent.getLongExtra(SELECTED_OFFER_ID, 0L)
        if (offerId == 0L) {
            viewModel.showShimmerAnimation.value = true
            showAlerterErrorMsg(getString(R.string.offer_no_available))
        } else {
            isInactiveOffer = intent.getBooleanExtra(IS_INACTIVE_OFFER, false)
            viewModel.getLocalOffer(offerId)
            offerID = offerId
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfWasLightStatusBar()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_offer_details)
        binding.apply {
            viewModel = this@OfferDetailsActivity.viewModel
            lifecycleOwner = this@OfferDetailsActivity
        }
    }

    private fun initObservers() {
        viewModel.showShimmerAnimation.observe(this) { shouldStart ->
            startStopShimmer(shouldStart)
        }

        viewModel.offerDetailsModel.observe(this) { offer ->
            viewModel.checkUserType(offer)
            populateOfferDetails(offer)
        }

        viewModel.placeBidEvent.observe(this) {
            viewModel.sendBidToServer(it)
        }

        viewModel.placeBidSuccessEvent.observe(this) { isSuccess ->
            placeBidBottomSheet.dismiss()
            if (!isSuccess) showAlerterErrorMsg(getString(R.string.unable_to_place_bid))
        }

        viewModel.shouldShowLoader.observe(this) {
            if (it) loaderDialog.showDialog()
            else loaderDialog.dismissDialog()
        }

        viewModel.favoriteOfferCallEvent.observe(this) { isFavorite ->
            setFavoriteIconState(isFavorite)
        }

        viewModel.notEnoughCoinsEvent.observe(this) { hasNoCoins ->
            if (hasNoCoins) showNotEnoughCoinsDialog()
        }

        viewModel.shouldShowWholeDescription.observe(this) {
            binding.btnReadMore.setText(if (binding.tvDescription.isExpanded) R.string.read_more else R.string.read_less)
            binding.tvDescription.toggle()
        }

        viewModel.userType.observe(this) {
            showCorrespondingOfferButtons(it)
        }

        viewModel.reportOfferEvent.observe(this) {
            reportOfferBottomSheet.showCongratsView()
        }

        viewModel.offerDeleteEvent.observe(this) {
            finish()
        }

        viewModel.wasBidAccepted.observe(this) { wasAccepted ->
            if (!wasAccepted) {
                toast(resources.getString(R.string.bid_declined_message))
            }
        }

        viewModel.offerNotAvailableEvent.observe(this) {
            viewModel.showShimmerAnimation.value = true
            showAlerterErrorMsg(getString(R.string.offer_no_available))
        }

        viewModel.somethingWentWrongEvent.observe(this) {
            showAlerterErrorMsg(getString(R.string.something_went_wrong))
        }
    }

    private fun showCorrespondingOfferButtons(userType: UserTypeEnum) {
        when (userType) {
            UserTypeEnum.NORMAL_FIX_PRICE_OFFER -> {
                // show Call / Message button when offer is ACTIVE
                binding.inclCallMessageView.root.show()
            }

            UserTypeEnum.NORMAL_AUCTION_OFFER -> {
                if (viewModel.offerDetailsModel.value?.status.equals(
                        OfferStateEnum.INTERRUPTED.getStatusName(), true
                    )
                ) return

                if (viewModel.offerDetailsModel.value?.status.equals(
                        OfferStateEnum.FINISHED.getStatusName(), true
                    )
                ) {
                    binding.inclPlaceBidView.root.hide()
                    viewModel.offerDetailsModel.value?.let {
                        if (isBidWinner(it)) {
                            showContactTheSellerWinnerView(getOfferWinnerDetails(it, false))
                        }
                    }
                } else {
                    binding.inclPlaceBidView.root.show()
                }
            }

            UserTypeEnum.OWNER_FIX_PRICE_OFFER -> {
                if (viewModel.offerDetailsModel.value?.status.equals(
                        OfferStateEnum.INACTIVE.getStatusName(),
                        true
                    )
                ) {
                    // show Edit / Enable buttons when offer is INACTIVE
                    binding.inclEditOfferView.root.show()
                    binding.inclEditOfferView.btnDisable.hide()
                    binding.inclEditOfferView.btnEnable.show()
                } else {
                    // show Edit / Disable buttons when offer is ACTIVE
                    binding.inclEditOfferView.root.show()
                    binding.inclEditOfferView.btnDisable.show()
                    binding.inclEditOfferView.btnEnable.hide()
                }
            }

            UserTypeEnum.OWNER_AUCTION_OFFER -> {
                when (viewModel.offerDetailsModel.value?.status) {
                    OfferStateEnum.FINISHED.getStatusName() -> {
                        viewModel.offerDetailsModel.value?.let {
                            if (it.bids?.isNotEmpty() == true) {
                                showContactTheSellerWinnerView(getOfferWinnerDetails(it, true))
                            }
                        }
                    }

                    OfferStateEnum.INTERRUPTED.getStatusName() -> {
                        binding.inclCloseAuctionView.root.hide()
                    }

                    else -> {
                        binding.inclCloseAuctionView.root.show()
                    }
                }
            }
        }
    }

    private fun getOfferWinnerDetails(offer: OfferModel, isOwner: Boolean): OfferWinnerModel {
        val offerWinnerModel = OfferWinnerModel()
        if (isOwner) {
            val bidWinner = getBidWinner(offer.bids)
            bidWinner?.let { offerBid ->
                offerWinnerModel.userName = offerBid.userName
                offerWinnerModel.userAvatarUrl = offerBid.userAvatar.orEmpty()
                offerWinnerModel.contactSellerWinner =
                    resources.getString(R.string.contact_the_winner)
                offerWinnerModel.activeAt = DateUtils().getFormattedDateForUserActive(
                    offerBid.lastSeen
                )
                viewModel.phoneNumber = offerBid.phone
            }
        } else {
            offer.owner?.let { owner ->
                offerWinnerModel.userName = getFullOwnerName(owner)
                offerWinnerModel.userAvatarUrl = owner.avatarUrl ?: ""
                offerWinnerModel.contactSellerWinner =
                    resources.getString(R.string.contact_the_seller)
                offerWinnerModel.activeAt = DateUtils().getFormattedDateForUserActive(
                    owner.lastSeen
                )
                viewModel.phoneNumber = offer.phoneNumbers
            }
        }
        return offerWinnerModel
    }

    private fun isBidWinner(offerModel: OfferModel): Boolean {
        return viewModel.user.value?.let { getFullUsername(it) } == getBidWinner(offerModel.bids)?.userName
    }

    private fun showNotEnoughCoinsDialog() {
        viewModel.user.value?.availableCoins?.let {
            noCoinsBalanceDialog.setCoinsAmount(it)
        }
        noCoinsBalanceDialog.show(
            supportFragmentManager,
            GenericDialog.PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG
        )
    }

    private fun onBuyMoreClicked() {
        launchActivity<BuyCoinsActivity>()
        noCoinsBalanceDialog.dismiss()
        placeBidBottomSheet.dismiss()
    }

    private fun showAlerterErrorMsg(errorMsg: String) {
        AlerterUtils.showErrorAlert(this, errorMsg)
    }

    private fun setFavoriteIconState(isFavorite: Boolean) {
        Glide.with(this)
            .load(getOfferDetailsFavoriteIconByState(isFavorite))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.ivFavorite)
    }

    private fun populateOfferDetails(offer: OfferModel) {
        populateStaticFields(offer)
        Handler(Looper.getMainLooper()).postDelayed({
            initTagsRv(offer)
        }, 100)
        showOfferDeleteIcon(offer)
        populateAuctionDetails(offer)
        setFavoriteIconState(offer.isUserFavorite)
        addDescriptionAnimation()
        updateOfferPhotos(offer.photos.toMutableList())
        showOfferStateHeader(offer)
        handleReportButton()
        showPromoteIcon(offer)
    }

    private fun populateStaticFields(offer: OfferModel) {
        binding.apply {
            offerPrice.tvPrice.text = getOfferDetailsPrice(this@OfferDetailsActivity, offer)
            tvOfferDate.text = DateUtils().getFormattedDateForOffer(offer.publishDate.orEmpty())
            sellerRatingBar.rating = offer.owner?.rating ?: 5f
            tvUserActiveTime.text = getUserLastSeenTime(this@OfferDetailsActivity, offer.owner?.lastSeen.orEmpty())
            offer.owner?.let {
                ivUserAvatar.loadImage(
                    it.avatarUrl.toString(),
                    R.drawable.ic_profile_placeholder,
                    circleCrop = true
                )
            }
            viewModel?.phoneNumber = offer.phoneNumbers
            if (offer.isOnAuction) {
                if (offer.status?.equals(OfferStateEnum.FINISHED.getStatusName()) == true
                    || offer.status?.equals(OfferStateEnum.INTERRUPTED.getStatusName()) == true
                ) {
                    viewCurrentYourBid.tvCurrentBidTitle.text = resources.getString(R.string.start_price)
                    viewCurrentYourBid.tvYourBidTitle.text = resources.getString(R.string.final_bid)
                } else if (offer.bids.isNullOrEmpty() || isOwner()) {
                    viewCurrentYourBid.tvCurrentBidTitle.text = resources.getString(R.string.start_price)
                    viewCurrentYourBid.tvYourBidTitle.text = resources.getString(R.string.current_bid)
                } else {
                    viewCurrentYourBid.tvCurrentBidTitle.text = resources.getString(R.string.current_bid)
                    viewCurrentYourBid.tvYourBidTitle.text = resources.getString(R.string.your_bid)
                }
            }
        }
    }

    private fun showOfferDeleteIcon(offer: OfferModel) {
        if ((offer.status.orEmpty().equals(OfferStateEnum.INACTIVE.name, true) ||
                    offer.status.orEmpty().equals(OfferStateEnum.INTERRUPTED.name, true) ||
                    offer.status.orEmpty().equals(OfferStateEnum.FINISHED.name, true))
        ) {
            binding.ivDeleteIcon.show()
        }
    }

    private fun handleReportButton() {
        if (isOwner()) {
            binding.tvReportOffer.hide()
        }
    }

    private fun offerCanBePromoted(offer: OfferModel): Boolean {
        return offer.status.equals(OfferStateEnum.ACTIVE.name, true) &&
                isOwner() && !offer.isPromoted
    }

    private fun showPromoteIcon(offer: OfferModel) {
        if (offerCanBePromoted(offer)) {
            binding.ivPromoteIcon.show()
        } else {
            binding.ivPromoteIcon.hide()
        }
    }

    private fun showOfferStateHeader(offer: OfferModel) {
        val status = offer.status
        val isOwner =
            offer.owner?.userName.orEmpty() == viewModel.user.value?.email?.trim().orEmpty()
        if (!isOwner || isInactiveOffer || status.isNullOrEmpty()) return
        with(binding.inclOfferStateHeader) {
            root.show()
            llStateBackground.setBackgroundResource(
                getStateBackgroundByStatus(status)
            )
            ivOfferStateIcon.loadImageResource(
                getStateIconByStatus(
                    status
                )
            )
            tvOfferState.text = getStateTitleByStatus(status, this@OfferDetailsActivity)
        }
    }

    private fun showContactTheSellerWinnerView(offerWinnerModel: OfferWinnerModel) {
        if (offerWinnerModel.userName.isEmpty() || offerWinnerModel.contactSellerWinner.isEmpty()) return
        with(binding.inclContactSellerView) {
            root.show()
            tvContactTheSeller.text = offerWinnerModel.contactSellerWinner
            ivSellerAvatar.loadImage(
                offerWinnerModel.userAvatarUrl,
                R.drawable.ic_profile_placeholder,
                circleCrop = true
            )
            tvSellerName.text = offerWinnerModel.userName
            tvSellerActiveTime.text = resources.getString(R.string.active_today_at, offerWinnerModel.activeAt)
        }

        binding.contactSellerExtraSpace.layoutParams.apply {
            height = resources.getDimensionPixelSize(R.dimen.space_220dp)
        }
    }

    private fun getBidWinner(bids: MutableList<OfferBid?>?): OfferBid? {
        return bids?.firstOrNull { it?.winner ?: false }
    }

    @SuppressLint("SetTextI18n")
    private fun populateAuctionDetails(offer: OfferModel) {
        if (!offer.isOnAuction) return

        with(binding) {
            if (offer.status?.equals(OfferStateEnum.FINISHED.getStatusName()) == true
                || offer.status?.equals(OfferStateEnum.INTERRUPTED.getStatusName()) == true
            ) {
                viewCurrentYourBid.tvCurrentBidValue.text =
                    Formatters.priceDecimalFormat.format(offer.price) + OfferUtils.getCurrency(offer.currencyType)
                viewCurrentYourBid.tvYourBidValue.text = getHighestBidWithCurrency(offer)
            } else {
                if (isOwner()) {
                    viewCurrentYourBid.tvCurrentBidValue.text = getOfferDetailsPrice(this@OfferDetailsActivity, offer)
                    viewCurrentYourBid.tvYourBidValue.text = getHighestBidWithCurrency(offer)
                } else {
                    viewCurrentYourBid.tvCurrentBidValue.text = getHighestBidWithCurrency(offer)
                    viewCurrentYourBid.tvYourBidValue.text = getYourBidWithCurrency(offer, viewModel?.user?.value)
                    if (viewCurrentYourBid.tvYourBidValue.text != "-") {
                        viewCurrentYourBid.tvYourBidValue.setTextColor(getYourBidColor(offer))
                    }
                }
            }

            val bids = offer.bids.orEmpty().sortedByDescending { it?.bidValue.orElse(0f) }
                .distinctBy { it?.email }
            bids.let {
                if (it.size > 3) {
                    binding.viewBiddersAndMore.tvDescriptionAuction.show()
                    binding.viewBiddersAndMore.tvDescriptionAuction.text =
                        resources.getString(R.string.text_n_others, "${it.size - 3}")
                } else if (it.isEmpty()) {
                    binding.viewBiddersAndMore.tvNoBids.show()
                }
                binding.tvDate.text = getOfferDate(offer, this@OfferDetailsActivity)
                showBidders(it)
            }
        }
    }

    private fun showBidders(bids: List<OfferBid?>) {
        bids.take(3).forEachIndexed { index, bid ->
            when (index) {
                0 -> {
                    binding.viewBiddersAndMore.ivBidder1.show()
                    populateBidderAvatar(bid, binding.viewBiddersAndMore.ivBidder1)
                }

                1 -> {
                    binding.viewBiddersAndMore.ivBidder2.show()
                    populateBidderAvatar(bid, binding.viewBiddersAndMore.ivBidder2)
                }

                2 -> {
                    binding.viewBiddersAndMore.ivBidder3.show()
                    populateBidderAvatar(bid, binding.viewBiddersAndMore.ivBidder3)
                }
            }
        }
    }

    private fun populateBidderAvatar(
        bid: OfferBid?,
        bidView: AppCompatImageView
    ) {
        Glide.with(this)
            .load(bid?.userAvatar)
            .error(getDrawableCompat(R.drawable.ic_profile_placeholder))
            .placeholder(getDrawableCompat(R.drawable.ic_profile_placeholder))
            .circleCrop()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(bidView)
    }

    private fun getYourBidColor(offer: OfferModel): Int {
        val yourBid = getYourBid(offer, viewModel.user.value)
        val highestBid = viewModel.offerDetailsModel.value?.highestBid ?: 0f
        return if (yourBid < highestBid) {
            getColorCompat(R.color.red)
        } else {
            getColorCompat(R.color.green)
        }
    }

    private fun initTagsRv(offer: OfferModel) {
        val userSelectedLanguage = viewModel.selectedLanguage
        val conditionFieldValue = CategoryFieldsValue(
            resources.getString(R.string.condition).replace("*", ""),
            ConditionTypeEnum.valueOf(offer.condition).getConditionTranslatedName(this)
        )

        val categoryTags = viewModel.categoryDetailsModel.categoryDetails
        val finalList: MutableList<CategoryFieldsValue> = offer.details as MutableList<CategoryFieldsValue>

        finalList.forEachIndexed { index, categoryFieldsValue ->
            val translatedKey =
                categoryTags.firstOrNull {
                    categoryFieldsValue.key.lowercase().equals(it.name.lowercase(), true)
                }?.label?.firstOrNull { it.language.lowercase() == userSelectedLanguage.lowercase() }?.translation
            translatedKey?.let {
                finalList[index].apply { key = it }
            }
        }
        if (!finalList.contains(conditionFieldValue)) {
            finalList.add(
                offer.details.size,
                CategoryFieldsValue(
                    resources.getString(R.string.condition).replace("*", ""),
                    ConditionTypeEnum.valueOf(offer.condition).getConditionTranslatedName(this)
                )
            )
        }
        val adapterOfferTags = AdapterOfferTags(finalList)
        binding.rvCategoryTags.apply {
            adapter = adapterOfferTags
            layoutManager = GridLayoutManager(context, 2)
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

    private fun updateOfferPhotos(photos: MutableList<OfferPhoto>?) {
        if (photos.isNullOrEmpty()) {
            binding.vpDotsIndicator.hide()
            binding.ivVpPlaceholder.show()
            return
        } else {
            photos.filter { it.isCover }
            offerImagesAdapter?.updateOfferImagesList(photos)
        }
    }

    private fun initPhotosViewPager() {
        offerImagesAdapter = DotIndicatorPager2Adapter(mutableListOf(), ::onImageClicked)
        val zoomOutPageTransformer = ZoomOutPageTransformer()

        binding.apply {
            vpOfferImages.adapter = offerImagesAdapter
            vpOfferImages.setPageTransformer { page, position ->
                zoomOutPageTransformer.transformPage(page, position)
            }
            vpDotsIndicator.attachTo(vpOfferImages)
        }
    }

    private fun onImageClicked(imagePosition: Int) {
        val intent = Intent(this, ImageViewerActivity::class.java)
        ImageViewerActivity.selectedPosition = imagePosition
        ImageViewerActivity.offerPhotos =
            viewModel.offerDetailsModel.value?.photos.orEmpty() as MutableList<OfferPhoto>
        activityResultLauncher.launch(intent)
    }

    private fun initListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
                setTransparentStatusBar()
            }
        })

        binding.btnBackArrow.setOnClickListenerWithDelay {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.inclPlaceBidView.btnPlaceBid.setOnClickListenerWithDelay {
            handleUserLoggedInAction { showPlaceBidBottomSheet() }
        }

        binding.tvReportOffer.setOnClickListenerWithDelay {
            handleUserLoggedInAction { showReportOfferBottomSheet() }
        }

        binding.cvFavorite.setOnClickListenerWithDelay {
            handleUserLoggedInAction {
                if (!isOwner()) {
                    viewModel.favoriteOfferCall()
                }
            }
        }

        binding.inclEditOfferView.btnEdit.setOnClickListenerWithDelay {
            openOfferEdit()
        }
        binding.inclEditOfferView.btnEnable.setOnClickListenerWithDelay {
            viewModel.user.value?.availableCoins?.let {
                enableConfirmationDialog.setCoinsAmount(it)
            }
            enableConfirmationDialog.show(supportFragmentManager, enableConfirmationDialog.tag)
        }

        binding.inclEditOfferView.btnDisable.setOnClickListenerWithDelay {
            disableConfirmationDialog.show(supportFragmentManager, enableConfirmationDialog.tag)
        }

        binding.inclCloseAuctionView.btnCloseAuction.setOnClickListenerWithDelay {
            showCloseAuctionDialog()
        }

        binding.inclContactSellerView.btnCallSeller.setOnClickListenerWithDelay {
            callSeller()
        }

        binding.inclContactSellerView.btnMessageSeller.setOnClickListenerWithDelay {
            messageSeller()
        }

        binding.inclCallMessageView.btnCall.setOnClickListenerWithDelay {
            callSeller()
        }

        binding.inclCallMessageView.btnMessage.setOnClickListenerWithDelay {
            messageSeller()
        }

        binding.ivPromoteIcon.setOnClickListenerWithDelay {
            viewModel.offerDetailsModel.value?.id?.let { id ->
                addBackStack(PromoteOfferFragment(offerId = id), binding.flContainer.id)
            }
        }

        binding.ivShareIcon.setOnClickListenerWithDelay {
            handleShareOffer()
        }

        binding.ivDeleteIcon.setOnClickListenerWithDelay {
            showOfferDeleteConfirmation()
        }

        binding.viewBiddersAndMore.llAuctionOthersContainer.setOnClickListenerWithDelay {
            if (viewModel.offerDetailsModel.value?.bids?.isNotEmpty() == true) {
                showBidHistory()
            }
        }

        binding.ivFacebook.setOnClickListenerWithDelay {
            redirectToBrowserLink(this, FACEBOOK_PAGE)
        }

        binding.ivInstagram.setOnClickListenerWithDelay {
            redirectToBrowserLink(this, INSTAGRAM_PAGE)
        }

        binding.ivTiktok.setOnClickListenerWithDelay {
            redirectToBrowserLink(this, TIKTOK_PAGE)
        }

        binding.tvContactUs.setOnClickListenerWithDelay {
            showContactUsInfoPopup()
        }

        binding.ivUserAvatar.setOnClickListenerWithDelay {
            openUserOffersActivity()
        }

        binding.tvUsername.setOnClickListenerWithDelay {
            openUserOffersActivity()
        }

        binding.viewRatingBarOverlay.setOnClickListenerWithDelay {
            openUserOffersActivity()
        }
    }

    private fun handleShareOffer() {
        viewModel.offerDetailsModel.value?.deepLink.orEmpty().let { link ->
            if (link.isNotEmpty()) {
                shareLink(this, link)
            } else {
                viewModel.getOfferDeepLink(offerID) { newLink ->
                    if (newLink.isNotEmpty()) {
                        shareLink(this, newLink)
                    } else {
                        AlerterUtils.showErrorAlert(this, resources.getString(R.string.something_went_wrong))
                    }
                }
            }
        }
    }

    private fun showContactUsInfoPopup() {
        contactUsInfoDialog.show(supportFragmentManager, contactUsInfoDialog.tag)
    }

    private fun showBidHistory() {
        bidHistoryBottomSheet.show(supportFragmentManager, bidHistoryBottomSheet.tag)
    }

    private fun openOfferEdit() {
        launchActivity<AddOfferActivity> {
            putExtra(IS_EDIT_MODE, true)
            putExtra(OFFER_ID, viewModel.offerDetailsModel.value?.id)
        }
    }

    private fun showOfferDeleteConfirmation() {
        deleteConfirmationDialog.show(supportFragmentManager, deleteConfirmationDialog.tag)
    }

    private fun messageSeller() {
        if (viewModel.isUserLoggedIn.value == false) {
            showGuestModePopup()
        } else {
            launchChatMessageActivity()
        }
    }

    private fun launchChatMessageActivity() {
        viewModel.offerDetailsModel.value?.let { offer ->
            launchActivity<ChatMessagesActivity> {
                putExtra(OFFER_ID, offer.id)
            }
        }
    }

    private fun callSeller() {
        if (viewModel.isUserLoggedIn.value == false) {
            showGuestModePopup()
        } else {
            showPhoneDialer(viewModel.phoneNumber, this)
        }
    }

    private fun showGuestModePopup() {
        showGuestModeBottomSheet(supportFragmentManager, getString(R.string.guest_offer_details))
    }

    private fun showCloseAuctionDialog() {
        closeAuctionDialog.show(
            supportFragmentManager,
            GenericDialog.CLOSE_AUCTION_DIALOG_TAG
        )
    }

    private fun onCloseAuctionConfirmClicked() {
        viewModel.changeOfferStatus()
        closeAuctionDialog.dismiss()
    }

    private fun showPlaceBidBottomSheet() {
        viewModel.offerDetailsModel.value?.let {
            placeBidBottomSheet.show(this.supportFragmentManager, placeBidBottomSheet.tag)
        }
    }

    private fun onPlaceBidClicked(placeBidModel: PlaceBidModel) {
        viewModel.placeBidEvent.value = placeBidModel
    }

    private fun showReportOfferBottomSheet() {
        reportOfferBottomSheet.show(this.supportFragmentManager, reportOfferBottomSheet.tag)
    }

    private fun sendOfferReport(reportOfferModel: ReportOfferModel) {
        viewModel.sendOfferReport(reportOfferModel)
    }

    private fun startStopShimmer(shouldStart: Boolean) {
        if (shouldStart) {
            binding.inclShimmerOffers.shimmerContainer.startShimmer()
        } else {
            binding.inclShimmerOffers.shimmerContainer.stopShimmer()
        }
    }

    private fun checkIfWasLightStatusBar() {
        if (viewModel.isLightStatusBar.value == true)
            StatusBarCompat.changeToLightStatusBar(this)
        else
            StatusBarCompat.cancelLightStatusBar(this)
    }

    private fun openUserOffersActivity() {
        if (isOwner()) {
            launchActivity<YourOffersActivity>()
        } else {
            launchActivity<ViewUserOffersActivity> {
                viewModel.offerDetailsModel.value?.owner?.let {
                    this@launchActivity.putExtra(USER_OFFERS, it)
                }
            }
        }
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
                    StatusBarCompat.changeToLightStatusBar(this@OfferDetailsActivity)
                    isShow = true
                    viewModel.isLightStatusBar.value = true
                } else if (isShow) {
                    StatusBarCompat.cancelLightStatusBar(this@OfferDetailsActivity)
                    isShow = false
                    viewModel.isLightStatusBar.value = false
                }
            }
        })
    }

    private fun selectCarouselImage(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val resultValue = data?.getIntExtra(LAST_CAROUSEL_INT_EXTRA, 0)
            resultValue?.let {
                lastCarouselImagePosition = it
                binding.vpOfferImages.currentItem = lastCarouselImagePosition
            }
        }
    }

    private fun handleUserLoggedInAction(action: () -> Unit) {
        if (viewModel.isUserLoggedIn.value == true) {
            action.invoke()
        } else {
            showGuestModeBottomSheet(
                supportFragmentManager,
                getString(R.string.guest_generic_message)
            )
        }
    }

    private fun onEnableOfferConfirmation() {
        enableConfirmationDialog.dismiss()
        if (viewModel.hasEnoughCoins()) {
            viewModel.changeOfferStatus()
        } else {
            showNotEnoughCoinsDialog()
        }
    }

    private fun onDisableOfferConfirmation() {
        disableConfirmationDialog.dismiss()
        viewModel.changeOfferStatus()
    }

    private fun onDeleteOfferConfirmation() {
        deleteConfirmationDialog.dismiss()
        viewModel.deleteCurrentOffer()
    }

    private fun isOwner(): Boolean {
        return viewModel.userType.value == UserTypeEnum.OWNER_AUCTION_OFFER ||
                viewModel.userType.value == UserTypeEnum.OWNER_FIX_PRICE_OFFER
    }

    companion object {
        const val LAST_CAROUSEL_INT_EXTRA = "LAST_CAROUSEL_INT_EXTRA"
        const val USER_OFFERS = "USER_OFFERS"
    }
}