package com.fivedevs.auxby.screens.dashboard.offers.categories

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityCategoriesBinding
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.offers.categories.adapters.AllCategoriesAdapter
import com.fivedevs.auxby.screens.dashboard.offers.categories.categoryOffers.CategoryOffersActivity
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllCategoriesActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private val viewModel by viewModels<AllCategoriesViewModel>()
    private var adapterCategories: AllCategoriesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initListeners()
        customizeSearchView()
        initCategoriesRv()
        viewModel.onCreate()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_categories)
        binding.apply {
            viewModel = this@AllCategoriesActivity.viewModel
            lifecycleOwner = this@AllCategoriesActivity
        }
    }

    private fun initCategoriesRv() {
        adapterCategories = AllCategoriesAdapter(this, mutableListOf(), viewModel.onCategorySelected)
        binding.rvAllCategories.apply {
            layoutManager = LinearLayoutManager(this@AllCategoriesActivity)
            isMotionEventSplittingEnabled = false
            adapter = adapterCategories
        }
    }

    private fun initObservers() {
        viewModel.categories.observe(this) { categories ->
            updateCategoriesAdapter(categories)
        }

        viewModel.filteredCategories.observe(this) { categories ->
            updateCategoriesAdapter(categories)
        }

        viewModel.categorySelected.observe(this) {
            handleCategorySelected(it)
        }

        viewModel.openOfferDetails.observe(this) { offerId ->
            launchActivity<OfferDetailsActivity> {
                putExtra(Constants.SELECTED_OFFER_ID, offerId)
            }
        }
    }

    private fun handleCategorySelected(it: CategoryModel) {
        when {
            it.noOffers.orZero() > 1 -> {
                launchActivity<CategoryOffersActivity> {
                    putExtra(SELECTED_CATEGORY_ID, it.id)
                }
            }
            it.noOffers.orZero() == 1 -> {
                viewModel.getFilteredCategory(it)
            }
            else -> {}
        }
    }

    private fun updateCategoriesAdapter(categories: List<CategoryModel>) {
        adapterCategories?.updateCategoriesList(categories)
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.searchViewCategories.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
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

    private fun customizeSearchView() {
        val montserratMediumFont = ResourcesCompat.getFont(this, R.font.montserrat_medium)
        binding.searchViewCategories.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).typeface =
            montserratMediumFont
        binding.searchViewCategories.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).setTextColor(
            ContextCompat.getColor(this, R.color.dark_text)
        )
        binding.searchViewCategories.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).setHintTextColor(
            ContextCompat.getColor(this, R.color.light_text)
        )
        binding.searchViewCategories.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon).setColorFilter(
            ContextCompat.getColor(this, R.color.dark_text), PorterDuff.Mode.SRC_IN
        )
        binding.searchViewCategories.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            .setImageResource(R.drawable.ic_close)
    }

    companion object {
        const val SELECTED_CATEGORY_ID = "Selected category id"
    }
}