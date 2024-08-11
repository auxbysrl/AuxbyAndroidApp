package com.fivedevs.auxby.screens.addOffer.fragments

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentAddOfferBinding
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.models.enums.ConditionTypeEnum
import com.fivedevs.auxby.domain.models.enums.DialogTypes
import com.fivedevs.auxby.domain.models.enums.OfferTypeEnum
import com.fivedevs.auxby.domain.utils.Constants.OFFER_MAXIMUM_PHOTOS
import com.fivedevs.auxby.domain.utils.Currencies
import com.fivedevs.auxby.domain.utils.OfferUtils.getCurrency
import com.fivedevs.auxby.domain.utils.Utils
import com.fivedevs.auxby.domain.utils.Validator
import com.fivedevs.auxby.domain.utils.extensions.disableClick
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.hideKeyboard
import com.fivedevs.auxby.domain.utils.extensions.isResumedOrLater
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.orFalse
import com.fivedevs.auxby.domain.utils.extensions.parcelableArrayList
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.transformIntoDatePicker
import com.fivedevs.auxby.domain.utils.views.CustomArrayAdapter
import com.fivedevs.auxby.screens.addOffer.AddOfferViewModel
import com.fivedevs.auxby.screens.addOffer.adapters.OfferPhotoAdapter
import com.fivedevs.auxby.screens.addOffer.factory.DynamicViewsManager
import com.fivedevs.auxby.screens.buyCoins.BuyCoinsActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.CategoryAdapter
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import com.google.android.material.textfield.TextInputLayout
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.FishBun.Companion.INTENT_PATH
import com.sangcomz.fishbun.MimeType
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import java.time.LocalDate

class AddOfferFragment : Fragment() {

    private lateinit var binding: FragmentAddOfferBinding
    private val parentViewModel: AddOfferViewModel by activityViewModels()
    private var categoryAdapter: CategoryAdapter? = null
    private var offerPhotoAdapter: OfferPhotoAdapter? = null
    private val dynamicViewsManager: DynamicViewsManager by lazy {
        DynamicViewsManager(
            requireContext()
        )
    }

    private var noCoinsBalanceDialog: GenericDialog? = null
    private var confirmOfferPublishDialog: GenericDialog? = null
    var selectedPhotos = mutableListOf<OfferPhoto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_offer, container, false)
        binding.parentViewModel = parentViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentViewModel.getCategoryDetails()
        initObservers()
        iniListener()
        initFieldsListener()
        initSubcategoriesRv()
        initOfferPhotosRv()
        populateOfferPriceType()
        populatePhoneNumberField()
        populateAutocompleteCities()
    }

    private fun iniListener() {
        binding.inclOfferCategory.ivCategoryArrow.setOnClickListenerWithDelay {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.etOfferEndDate.transformIntoDatePicker(requireContext())

        binding.btnContinue.setOnClickListenerWithDelay {
            handleContinueAddOffer()
        }

        binding.btnConfirm.setOnClickListenerWithDelay {
            handleEditOfferConfirm()
        }

        binding.viewOfferLocation.setOnClickListener {
            binding.spinnerLocation.performClick()
        }
    }

    private fun initFieldsListener() {
        with(binding) {
            etOfferTitle.doOnTextChanged { _, _, _, _ ->
                removeErrorField(tilOfferTitle)
            }

            etOfferEndDate.doOnTextChanged { text, _, _, _ ->
                removeErrorField(tilOfferEndDate)
                parentViewModel?.offerRequestModel?.setEndDate(text.toString())
            }

            etOfferPrice.doOnTextChanged { text, _, _, _ ->
                removeErrorField(tilEditPrice)
                parentViewModel?.offerRequestModel?.setPriceValue(text.toString())
            }

            etOfferDescription.doOnTextChanged { _, _, _, _ ->
                removeErrorField(tilOfferDescription)
            }

            etOfferLocation.doOnTextChanged { _, _, _, _ ->
                removeErrorField(tilOfferLocation)
            }

            etOfferPhone.doOnTextChanged { _, _, _, _ ->
                removeErrorField(tilOfferPhone)
            }

            actvPriceCurrency.doOnTextChanged { text, _, _, _ ->
                parentViewModel?.offerRequestModel?.setCurrencyTypeValue(text.toString())
            }

            rgOfferType.setOnCheckedChangeListener { rg, _ ->
                val checked = rg.findViewById(rg.checkedRadioButtonId) as RadioButton
                parentViewModel?.offerRequestModel?.setOfferTypeValue(checked.tag.toString())

                handleOfferTypeCheck()
            }

            rgCondition.setOnCheckedChangeListener { rg, _ ->
                val checked = rg.findViewById(rg.checkedRadioButtonId) as RadioButton
                parentViewModel?.offerRequestModel?.setConditionTypeValue(checked.tag.toString())
            }

            scSelfExtension.setOnCheckedChangeListener { _, isChecked ->
                tvSelfExtDesc.text = if (isChecked) {
                    getString(R.string.auto_extends_the_offer_every_30_days)
                } else {
                    getString(R.string.the_offer_expires_in_30_days)
                }
            }

            spinnerLocation.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val position =
                        if (p2 == resources.getStringArray(R.array.cities).size) p2 else p2.inc()
                    binding.etOfferLocation.setText(
                        binding.spinnerLocation.getItemAtPosition(
                            position
                        ).toString()
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun handleOfferTypeCheck() {
        binding.etOfferEndDate.text?.clear()
        if (binding.rbAuction.isChecked) {
            binding.tvOfferEndDate.show()
            binding.tilOfferEndDate.show()
        } else {
            binding.tvOfferEndDate.hide()
            binding.tilOfferEndDate.hide()
        }
    }

    private fun initObservers() {
        parentViewModel.categoryDetailsResponse.observe(viewLifecycleOwner) {
            populateOfferCategory(it)
            handleSubcategories(it)
            handleAddCategoryDetailsViews(it)
        }

        parentViewModel.selectedSubcategory.observe(viewLifecycleOwner) {
            handleAddSubcategoryViews(it)
        }

        parentViewModel.selectedPosPhoto.observe(viewLifecycleOwner) {
            viewLifecycleOwner.isResumedOrLater {
                handleSelectedPhoto(it)
            }
        }

        parentViewModel.automaticallyPopulateDetails.observe(viewLifecycleOwner) {
            automaticallyPopulateFields()
        }

        parentViewModel.onOfferUpdateEvent.observe(viewLifecycleOwner) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        parentViewModel.multipartImages.observe(viewLifecycleOwner) {
            parentViewModel.updateOffer(it)
        }
    }

    private fun automaticallyPopulateFields() {
        with(parentViewModel.offerRequestModel) {
            initDialogs()
            binding.inclOfferCategory.viewBlockListener.show()
            binding.btnContinue.hide()
            binding.btnConfirm.show()
            if (offerType == OfferTypeEnum.FIXE_PRICE.offerType) {
                binding.rgOfferType.check(R.id.rbFixPrice)
            } else {
                binding.rgOfferType.check(R.id.rbAuction)
            }
            if (parentViewModel.isEditMode) {
                binding.rbAuction.disableClick()
                binding.rbFixPrice.disableClick()
            }
            handleOfferTypeCheck()

            binding.etOfferPrice.setText(price.toString())
            binding.actvPriceCurrency.setText(getCurrency(currencyType), false)
            if (conditionType == ConditionTypeEnum.USED.getConditionName()) {
                binding.rgCondition.check(R.id.rbConditionUsed)
            } else {
                binding.rgCondition.check(R.id.rbConditionNew)
            }
            binding.etOfferLocation.setText(contactInfo.location)
            binding.etOfferPhone.setText(parentViewModel.localUser.value?.phone.orEmpty())
            selectedPhotos = photos
            if (selectedPhotos.isNotEmpty()) {
                updateOfferPhotoAdapter()
            }
        }
    }

    private fun handleAddSubcategoryViews(it: CategoryDetailsModel) {
        binding.llSubcategory.removeAllViews()
        populateCategoryDetailsViews(
            it.getSortedCategoryDetails(),
            true,
            parentViewModel.offerRequestModel.categoryDetails
        )
    }

    private fun handleAddCategoryDetailsViews(it: CategoryDetailsModel) {
        if (it.categoryDetails.isEmpty()) {
            binding.tvCategoryDetails.hide()
            binding.llCategoryDetails.hide()
        } else {
            it.subcategories.firstOrNull()?.getSortedCategoryDetails()?.let { subcategoryFields ->
                populateCategoryDetailsViews(
                    subcategoryFields,
                    true,
                    parentViewModel.offerRequestModel.categoryDetails
                )
            }
            populateCategoryDetailsViews(
                it.getSortedCategoryDetails(),
                false,
                parentViewModel.offerRequestModel.categoryDetails
            )
        }
    }

    private fun handleSubcategories(it: CategoryDetailsModel) {
        if (it.subcategories.isNotEmpty() && !parentViewModel.isEditMode) {
            binding.tvSubcategory.show()
            binding.rvSubcategories.show()
            categoryAdapter?.updateCategoriesList(it.subcategoriesToCategoryModel())
        }
    }

    private fun handleSelectedPhoto(offerPhoto: OfferPhoto) {
        if (offerPhoto.url.isEmpty()) {
            initMultiplePhotos()
        } else {
            showDialog(offerPhoto)
        }
    }

    private fun showDialog(offerPhoto: OfferPhoto) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val arrayList = listOf(
            getString(R.string.set_as_cover), getString(R.string.remove), getString(R.string.cancel)
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), R.layout.item_dropdown, arrayList
        )
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    if (!offerPhoto.isCover) {
                        setPhotoAsCover(offerPhoto)
                    }
                }
                1 -> {
                    removeSelectedPhoto(offerPhoto)
                }
                else -> {}
            }
        }
        builder.setAdapter(adapter, listener)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val windows = dialog.window?.attributes
        windows?.gravity = Gravity.BOTTOM
        dialog.show()
    }

    private fun setPhotoAsCover(offerPhoto: OfferPhoto) {
        selectedPhotos.first { it.isCover }.isCover = false
        selectedPhotos.remove(offerPhoto)
        selectedPhotos.add(0, offerPhoto)
        selectedPhotos.first().isCover = true
        updateOfferPhotoAdapter()
    }

    private fun removeSelectedPhoto(offerPhoto: OfferPhoto) {
        selectedPhotos.remove(offerPhoto)
        updateOfferPhotoAdapter()
    }

    private fun updateOfferPhotoAdapter() {
        selectedPhotos.map { it.isCover }
        offerPhotoAdapter?.updatePhotosList(selectedPhotos)
        binding.tvOfferPhotos.text = getString(
            R.string.photos, "${selectedPhotos.size}/$OFFER_MAXIMUM_PHOTOS"
        )
    }

    private fun initMultiplePhotos() {
        FishBun.with(this)
            .setImageAdapter(GlideAdapter())
            .setMaxCount(OFFER_MAXIMUM_PHOTOS).setPickerSpanCount(4)
            .setActionBarColor(
                requireContext().getColor(R.color.white),
                requireContext().getColor(R.color.white),
                true
            )
            .setActionBarTitleColor(requireContext().getColor(R.color.dark_text))
            .setAlbumSpanCount(1, 2)
            .setButtonInAlbumActivity(true).hasCameraInPickerPage(true)
            .exceptMimeType(listOf(MimeType.GIF))
            .setSelectedImages(arrayListOf<Uri>().apply {
                clear()
                addAll(selectedPhotos.map { Uri.parse(it.url) })
            })
            .setReachLimitAutomaticClose(false)
            .setHomeAsUpIndicatorDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_arrow
                )
            )
            .setDoneButtonDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_done))
            .setAllViewTitle(getString(R.string.all_yout_photos))
            .setActionBarTitle(getString(R.string.photos_title))
            .textOnImagesSelectionLimitReached(getString(R.string.no_select_any_more))
            .textOnNothingSelected(getString(R.string.need_photo))
            .startAlbumWithActivityResultCallback(startForResult)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
            if (intent.resultCode == AppCompatActivity.RESULT_OK) {
                val data =
                    intent.data?.parcelableArrayList<Parcelable>(INTENT_PATH)
                        ?.map { OfferPhoto(it.toString()) } ?: listOf()

                selectedPhotos = data.toMutableList()
                updateOfferPhotoAdapter()
            }
        }

    private fun populateOfferPriceType() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            Currencies.currenciesList.map { it.symbol })
        binding.actvPriceCurrency.setAdapter(adapter)
        binding.actvPriceCurrency.setDropDownBackgroundResource(R.color.white)
    }

    private fun populateAutocompleteCities() {
        val cities = resources.getStringArray(R.array.cities).toMutableList()
        cities.add(0, "")

        CustomArrayAdapter(
            requireContext(), R.layout.item_dropdown,
            cities as ArrayList<String>
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLocation.adapter = adapter
        }

        if (!parentViewModel.localUser.value?.address?.city.isNullOrEmpty()) {
            cities
                .forEachIndexed { index, city ->
                    if (city.equals(parentViewModel.localUser.value?.address?.city, true)) {
                        binding.spinnerLocation.setSelection(index.dec())
                        return
                    }
                }
        }
    }

    private fun populateOfferCategory(it: CategoryDetailsModel) {
        binding.inclOfferCategory.ivCategoryArrow.setImageResource(R.drawable.ic_close)
        binding.inclOfferCategory.tvCategoryTitle.text = it.getName(requireContext())
        Glide.with(requireContext())
            .load(Utils.getFullImageUrl(it.icon))
            .error(requireContext().getDrawableCompat(R.drawable.ic_placeholder))
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.inclOfferCategory.ivCategory)
        binding.inclOfferCategory.root.setStrokeColor(
            ColorStateList.valueOf(requireContext().getColor(R.color.colorAccent))
        )
    }

    private fun initSubcategoriesRv() {
        categoryAdapter = CategoryAdapter(
            requireContext(),
            mutableListOf(),
            parentViewModel.onSubCategorySelected
        )
        binding.rvSubcategories.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun initOfferPhotosRv() {
        binding.tvOfferPhotos.text = getString(R.string.photos, "0/$OFFER_MAXIMUM_PHOTOS")
        offerPhotoAdapter = OfferPhotoAdapter(
            requireContext(), mutableListOf(OfferPhoto()), parentViewModel.onOfferPhotoSelected
        )
        binding.rvOfferPhotos.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = offerPhotoAdapter
        }
    }

    private fun populateCategoryDetailsViews(
        categoryDetails: List<CategoryField>,
        isSubcategory: Boolean = false,
        editCategoryDetails: List<CategoryFieldsValue> = listOf()
    ) {
        handleCreateViews(categoryDetails, isSubcategory, editCategoryDetails)

    }

    private fun handleCreateViews(
        categoryDetails: List<CategoryField>,
        isSubcategory: Boolean,
        detailsValue: List<CategoryFieldsValue> = listOf()
    ) {
        categoryDetails.forEach { categoryField ->
            dynamicViewsManager.getFieldByType(categoryField.type)
                ?.createDynamicView(
                    categoryField,
                    dynamicViewsManager.getValueByName(detailsValue, categoryField.name)
                )
                ?.let { categoryFieldView ->
                    if (isSubcategory) {
                        binding.llSubcategory.addView(categoryFieldView)
                    } else {
                        binding.llCategoryDetails.addView(categoryFieldView)
                    }
                }
        }
    }

    private fun handleContinueAddOffer() {
        if (areFieldsValid()) {
            updateOfferData()
            parentViewModel.onPreviewFragment.call()
        }
    }

    private fun handleEditOfferConfirm() {
        if (areFieldsValid()) {
            updateOfferData()
            if (parentViewModel.isEditMode) {
                parentViewModel.shouldShowLoader.value = true
                parentViewModel.getMultipartImages()
            } else {
                checkForCoins()
            }
        }
    }

    private fun checkForCoins() {
        val availableCoins = parentViewModel.localUser.value?.availableCoins ?: 0
        if (availableCoins == 0 || availableCoins < (parentViewModel.categoryDetailsResponse.value?.addOfferCost
                ?: 0)
        ) {
            showNotEnoughCoinsDialog()
        } else {
            showPublishOfferConfirmDialog()
        }
    }

    private fun showPublishOfferConfirmDialog() {
        confirmOfferPublishDialog?.show(
            childFragmentManager,
            GenericDialog.PUBLISH_ACCOUNT_DIALOG_TAG
        )
    }

    private fun showNotEnoughCoinsDialog() {
        noCoinsBalanceDialog?.show(
            childFragmentManager,
            GenericDialog.PUBLISH_ACCOUNT_NO_COINS_DIALOG_TAG
        )
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

    private fun onBuyMoreClicked() {
        requireActivity().launchActivity<BuyCoinsActivity>()
        noCoinsBalanceDialog?.dismiss()
    }

    private fun onConfirmClicked() {
        parentViewModel.getMultipartImages()
        confirmOfferPublishDialog?.dismiss()
    }

    private fun updateOfferData() {
        parentViewModel.offerRequestModel.apply {
            publishDate = LocalDate.now().toString()
            categoryDetails = dynamicViewsManager.getCategoryDetailsValues()
            photos =
                offerPhotoAdapter?.getListOfPhotos().orEmpty().map { it } as ArrayList<OfferPhoto>
            contactInfo.location = binding.etOfferLocation.text.toString()
        }
    }

    private fun areFieldsValid(): Boolean {
        var areValid = true
        with(binding) {
            unfocusedFields()
            if (etOfferTitle.text.toString().isEmpty()) {
                errorField(tilOfferTitle)
                areValid = false
            }

            if (rbAuction.isChecked && etOfferEndDate.text.toString().isEmpty()) {
                errorField(tilOfferEndDate)
                areValid = false
            }

            if (etOfferPrice.text.toString().isEmpty()) {
                errorField(tilEditPrice)
                areValid = false
            }

            if (etOfferDescription.text.toString().isEmpty()) {
                errorField(tilOfferDescription)
                areValid = false
            }

            if (etOfferLocation.text.toString().isEmpty()) {
                errorField(tilOfferLocation)
                areValid = false
            }

            if (etOfferPhone.text.toString().isEmpty()) {
                errorField(tilOfferPhone)
                areValid = false
            }

            val phoneValidator =
                Validator.validatePhoneField(etOfferPhone.text.toString(), requireContext())
            if (!phoneValidator.isNullOrEmpty()) {
                errorField(tilOfferPhone, phoneValidator)
                areValid = false
            }
        }

        if (!dynamicViewsManager.areDynamicFieldsValid().orFalse()) {
            areValid = false
        }

        return areValid
    }

    private fun unfocusedFields() {
        binding.root.clearFocus()
        binding.root.hideKeyboard()
    }

    private fun errorField(view: TextInputLayout, message: String = "") {
        view.error = message.ifEmpty { getString(R.string.text_field_required) }
        view.errorIconDrawable = null
    }

    private fun removeErrorField(view: TextInputLayout) {
        view.error = null
        view.isErrorEnabled = false
    }

    private fun populatePhoneNumberField() {
        parentViewModel.localUser.value?.let { user ->
            parentViewModel.offerRequestModel.contactInfo.phoneNumber = user.phone.trim()
        }
    }
}