package com.fivedevs.auxby.screens.filterOffers

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.DialogFilterOffersBinding
import com.fivedevs.auxby.domain.models.AdvancedFiltersModel
import com.fivedevs.auxby.domain.models.CategoryDetailsModel
import com.fivedevs.auxby.domain.models.CategoryField
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.models.enums.ConditionTypeEnum
import com.fivedevs.auxby.domain.models.enums.CurrencyEnum
import com.fivedevs.auxby.domain.models.enums.OfferTypeEnum
import com.fivedevs.auxby.domain.utils.Currencies
import com.fivedevs.auxby.domain.utils.Utils
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.views.CustomArrayAdapter
import com.fivedevs.auxby.screens.addOffer.factory.DynamicViewsManager
import com.fivedevs.auxby.screens.base.BaseBottomSheetDialog
import com.fivedevs.auxby.screens.dashboard.offers.adapters.CategoryAdapter
import com.fivedevs.auxby.screens.dashboard.offers.categories.adapters.AllCategoriesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.KFunction2


@AndroidEntryPoint
class FilterOffersDialog(
    private val currentFilters: AdvancedFiltersModel,
    private val applyCallBack: KFunction2<AdvancedFiltersModel, String, Unit>
) : BaseBottomSheetDialog(R.layout.dialog_filter_offers, true) {

    private lateinit var binding: DialogFilterOffersBinding
    private val viewModel by viewModels<FilterOffersViewModel>()

    private var categoryAdapter: CategoryAdapter? = null
    private var adapterCategories: AllCategoriesAdapter? = null
    private val dynamicViewsManager: DynamicViewsManager by lazy {
        DynamicViewsManager(
            requireContext()
        )
    }

    override fun getContentView(binding: ViewDataBinding) {
        this.binding = binding as DialogFilterOffersBinding
        this.binding.viewModel = viewModel
        viewModel.advancedFilterOffers = currentFilters.copy()
        viewModel.getCategoryDetails(currentFilters.categories.firstOrNull())
        populateOfferCategory()
        populateAutocompleteCities()
        initListeners()
        initObservers()
        initFieldsListener()
        populateOfferPriceType()
        initSubcategoriesRv()
        initSelectCategoriesRv()
        populateCurrentFilters(currentFilters)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.advancedFilterOffers.clear()
    }

    private fun initListeners() {
        binding.toolbar.tvCancel.setOnClickListenerWithDelay {
            dismiss()
        }

        binding.toolbar.tvClear.setOnClickListenerWithDelay {
            clearFilters()
        }

        binding.applyButton.setOnClickListenerWithDelay {
            applyFilters()
            dismiss()
        }

        binding.inclOfferCategory.ivCategoryArrow.setOnClickListenerWithDelay {
            clearCategorySelected()
        }

        binding.inclOfferCategory.root.setOnClickListenerWithDelay {
            with(binding) {
                rvAllCategories.show()
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right)
                rvAllCategories.startAnimation(animation)
                filtersContainer.hide()
                binding.toolbar.tvClear.hide()
            }
        }

        binding.viewFilterLocation.setOnClickListener {
            binding.spinnerLocation.performClick()
        }
    }

    private fun initObservers() {
        viewModel.categoryDetailsResponse.observe(this) {
            populateCategorySelected(it)
        }

        viewModel.selectedSubcategory.observe(this) {
            handleAddSubcategoryViews(it)
        }

        viewModel.appCategoriesResponse.observe(this) {
            adapterCategories?.updateCategoriesList(it)
            binding.rvAllCategories.smoothScrollToPosition(0)
        }

        viewModel.selectedCategory.observe(this) {
            handleSelectedCategory()
        }
    }

    private var isProgrammaticChange = false

    private fun initFieldsListener() {
        with(binding) {
            etOfferPrice.doOnTextChanged { text, _, _, _ ->
                viewModel?.advancedFilterOffers?.priceFilter?.lowestPrice =
                    text.toString().toIntOrNull() ?: 0
            }

            etOfferEndPrice.doOnTextChanged { text, _, _, _ ->
                viewModel?.advancedFilterOffers?.priceFilter?.highestPrice =
                    text.toString().toIntOrNull() ?: Int.MAX_VALUE
            }

            actvPriceType.doOnTextChanged { text, _, _, _ ->
                if (!isProgrammaticChange) {
                    isProgrammaticChange = true
                    actvEndPriceType.setText(text, false)
                    isProgrammaticChange = false
                }
            }

            actvEndPriceType.doOnTextChanged { text, _, _, _ ->
                if (!isProgrammaticChange) {
                    isProgrammaticChange = true
                    actvPriceType.setText(text, false)
                    isProgrammaticChange = false
                }
            }

            rbFixPrice.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel?.advancedFilterOffers?.offerType = OfferTypeEnum.FIXE_PRICE.offerType
                } else {
                    viewModel?.advancedFilterOffers?.offerType = null
                }
            }

            rbAuction.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel?.advancedFilterOffers?.offerType = OfferTypeEnum.AUCTION.offerType
                } else {
                    viewModel?.advancedFilterOffers?.offerType = null
                }
            }

            rbConditionUsed.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel?.advancedFilterOffers?.conditionType =
                        ConditionTypeEnum.USED.getConditionName()
                } else {
                    viewModel?.advancedFilterOffers?.conditionType = null
                }
            }

            rbConditionNew.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel?.advancedFilterOffers?.conditionType =
                        ConditionTypeEnum.NEW.getConditionName()
                } else {
                    viewModel?.advancedFilterOffers?.conditionType = null
                }
            }

            etOfferLocation.doOnTextChanged { text, _, _, _ ->
                viewModel?.advancedFilterOffers?.location = text.toString().trim()
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

    private fun clearFilters() {
        clearCategorySelected()
        with(binding) {
            etOfferPrice.text?.clear()
            etOfferEndPrice.text?.clear()
            rbFixPrice.isChecked = false
            rbAuction.isChecked = false
            rbConditionUsed.isChecked = false
            rbConditionNew.isChecked = false
            etOfferLocation.text?.clear()
        }
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

        if (viewModel.advancedFilterOffers.location.isNullOrEmpty()) {
            binding.spinnerLocation.setSelection(0, false)
        } else {
            binding.spinnerLocation.setSelection(
                cities.indexOf(viewModel.advancedFilterOffers.location.orEmpty()).dec()
            )
        }
    }

    private fun applyFilters() {
        with(viewModel) {
            val priceFilter = advancedFilterOffers.priceFilter
            if (priceFilter.lowestPrice > priceFilter.highestPrice) {
                val lowestPrice = priceFilter.lowestPrice
                priceFilter.lowestPrice = priceFilter.highestPrice
                priceFilter.highestPrice = lowestPrice
            }

            priceFilter.currencyType = getSelectedCurrency()
            advancedFilterOffers.location = binding.etOfferLocation.text.toString()

            applyCallBack(
                advancedFilterOffers.copy(),
                viewModel.categoryDetailsResponse.value?.getName(requireContext()).orEmpty()
            )
        }
    }

    private fun getSelectedCurrency(): String {
        return Currencies.currenciesList.firstOrNull { it.symbol.equals(binding.actvPriceType.text.toString(), true) }?.name ?: CurrencyEnum.RON.currencyType
    }

    private fun clearCategorySelected() {
        populateOfferCategory()
        handleSubcategories()
        handleAddCategoryDetailsViews()
        viewModel.advancedFilterOffers.clear()
    }

    private fun populateCategorySelected(it: CategoryDetailsModel) {
        populateOfferCategory(it)
        handleSubcategories(it)
        handleAddCategoryDetailsViews(it)
    }

    private fun populateOfferCategory(category: CategoryDetailsModel? = null) {
        if (category != null) {
            binding.inclOfferCategory.tvCategoryTitle.text =
                category.label.getName(requireContext())
            Glide.with(requireContext())
                .load(Utils.getFullImageUrl(category.icon))
                .error(requireContext().getDrawableCompat(R.drawable.ic_placeholder))
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.inclOfferCategory.ivCategory)
            binding.inclOfferCategory.ivCategoryArrow.setImageResource(R.drawable.ic_close)
            binding.inclOfferCategory.root.setStrokeColor(
                ColorStateList.valueOf(requireContext().getColor(R.color.colorAccent))
            )
        } else {
            binding.inclOfferCategory.tvCategoryTitle.text = getString(R.string.select_a_category)
            binding.inclOfferCategory.ivCategory.setImageResource(R.drawable.ic_placeholder)
            binding.inclOfferCategory.ivCategoryArrow.setImageResource(R.drawable.ic_arrow_right_light)
            binding.inclOfferCategory.root.setStrokeColor(
                ColorStateList.valueOf(requireContext().getColor(R.color.white))
            )

        }
    }

    private fun initSubcategoriesRv() {
        categoryAdapter = CategoryAdapter(
            requireContext(),
            mutableListOf(),
            viewModel.onSubCategorySelected
        )
        binding.rvSubcategories.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun initSelectCategoriesRv() {
        adapterCategories =
            AllCategoriesAdapter(requireContext(), mutableListOf(), viewModel.onCategorySelected)
        binding.rvAllCategories.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = adapterCategories
        }
    }

    private fun handleSubcategories(it: CategoryDetailsModel = CategoryDetailsModel()) {
        if (it.subcategories.isNotEmpty()) {
            binding.tvSubcategory.show()
            binding.rvSubcategories.show()
            categoryAdapter?.updateCategoriesList(it.subcategoriesToCategoryModel())
        } else {
            binding.tvSubcategory.hide()
            binding.rvSubcategories.hide()
        }
    }

    private fun handleSelectedCategory() {
        with(binding) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_left)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    rvAllCategories.hide()
                    filtersContainer.show()
                    binding.toolbar.tvClear.show()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            rvAllCategories.startAnimation(animation)

        }
    }

    private fun handleAddSubcategoryViews(it: CategoryDetailsModel) {
        binding.llSubcategory.removeAllViews()
        populateCategoryDetailsViews(
            it.getSortedCategoryDetails(),
            true,
            viewModel.offerRequestModel.categoryDetails
        )
    }

    private fun populateOfferPriceType() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            CurrencyEnum.entries.map { it.symbol() })
        binding.actvPriceType.setAdapter(adapter)
        binding.actvPriceType.setDropDownBackgroundResource(R.color.white)
        binding.actvEndPriceType.setAdapter(adapter)
    }

    private fun populateCurrentFilters(currentFilters: AdvancedFiltersModel) {
        with(binding) {
            currentFilters.priceFilter.let {
                if (it.lowestPrice > 0) {
                    etOfferPrice.setText(it.lowestPrice.toString())
                }

                if (it.highestPrice != Int.MAX_VALUE) {
                    etOfferEndPrice.setText(it.highestPrice.toString())
                }

                it.currencyType?.let { currency ->
                    actvPriceType.setText(getCurrencySymbol(currency))
                }
            }

            currentFilters.offerType?.let {
                rbAuction.isChecked = it == OfferTypeEnum.AUCTION.offerType
                rbFixPrice.isChecked = it == OfferTypeEnum.FIXE_PRICE.offerType
            }

            currentFilters.conditionType?.let {
                rbConditionUsed.isChecked = it == ConditionTypeEnum.USED.getConditionName()
                rbConditionNew.isChecked = it == ConditionTypeEnum.NEW.getConditionName()
            }
        }
    }

    private fun getCurrencySymbol(currency: String): String {
        return Currencies.currenciesList.firstOrNull { it.name.equals(currency, true) }?.symbol ?: CurrencyEnum.RON.symbol()
    }

    private fun handleAddCategoryDetailsViews(it: CategoryDetailsModel = CategoryDetailsModel()) {
        if (it.categoryDetails.isEmpty()) {
            binding.llCategoryDetails.removeAllViews()
            binding.llSubcategory.removeAllViews()
            binding.llCategoryDetails.hide()
            binding.llSubcategory.hide()
        } else {
            binding.llCategoryDetails.show()
            binding.llSubcategory.show()
            it.subcategories.firstOrNull()?.getSortedCategoryDetails()?.let { subcategoryFields ->
                populateCategoryDetailsViews(
                    subcategoryFields,
                    true,
                    viewModel.offerRequestModel.categoryDetails
                )
            }
            populateCategoryDetailsViews(
                it.getSortedCategoryDetails(),
                false,
                viewModel.offerRequestModel.categoryDetails
            )
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
}