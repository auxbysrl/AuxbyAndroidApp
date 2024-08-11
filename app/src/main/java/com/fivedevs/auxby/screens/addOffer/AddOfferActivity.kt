package com.fivedevs.auxby.screens.addOffer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityAddOfferBinding
import com.fivedevs.auxby.domain.models.toOfferRequestModel
import com.fivedevs.auxby.domain.utils.Constants.CATEGORY_ID
import com.fivedevs.auxby.domain.utils.Constants.IS_EDIT_MODE
import com.fivedevs.auxby.domain.utils.Constants.OFFER_ID
import com.fivedevs.auxby.domain.utils.extensions.addBackStack
import com.fivedevs.auxby.domain.utils.extensions.replace
import com.fivedevs.auxby.domain.utils.extensions.setWhiteStatusBar
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.domain.utils.views.LoaderDialog
import com.fivedevs.auxby.screens.addOffer.fragments.AddOfferFragment
import com.fivedevs.auxby.screens.addOffer.fragments.PreviewOfferFragment
import com.fivedevs.auxby.screens.addOffer.fragments.PromoteOfferFragment
import com.fivedevs.auxby.screens.addOffer.fragments.SelectCategoryFragment
import com.fivedevs.auxby.screens.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddOfferActivity : BaseActivity() {

    private lateinit var binding: ActivityAddOfferBinding

    private var loaderDialog: LoaderDialog? = null
    private val viewModel by viewModels<AddOfferViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        retrieveBundleInformation(savedInstanceState)
        viewModel.onCreate()
        initObservers()
        initListeners()
        initLoaderDialog()
    }

    private fun retrieveBundleInformation(savedInstanceState: Bundle?) {
        viewModel.isEditMode = intent.getBooleanExtra(IS_EDIT_MODE, false)
        viewModel.editOfferId = intent.getLongExtra(OFFER_ID, 0L)

        // HANDLE CATEGORY ID FROM ADS
        val categoryId = intent.getIntExtra(CATEGORY_ID, -1)
        if (categoryId != -1) {
            viewModel.selectedCategoryId.value = categoryId
        }

        initEditMode(savedInstanceState)
    }

    private fun initEditMode(savedInstanceState: Bundle?) {
        if (viewModel.isEditMode && viewModel.editOfferId != 0L) {
            viewModel.getOfferById(viewModel.editOfferId)
        } else {
            showSelectCategoryFragment(savedInstanceState)
        }
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_offer)
        binding.apply {
            viewModel = this@AddOfferActivity.viewModel
            lifecycleOwner = this@AddOfferActivity
        }
    }

    private fun initObservers() {
        viewModel.selectedCategoryId.observe(this) {
            addBackStack(AddOfferFragment(), binding.fragmentContainerView.id)
        }

        viewModel.onPreviewFragment.observe(this) {
            viewModel.shouldShowToolbar.value = false
            addBackStack(PreviewOfferFragment(), binding.fragmentContainerView.id)
        }

        viewModel.onPromoteOfferFragment.observe(this) { offerId ->
            addBackStack(PromoteOfferFragment(isFromAddOffer = true, offerId), binding.flContainer.id)
        }

        viewModel.returnToDashboard.observe(this) {
            returnAndRefreshDashboard(it)
        }

        viewModel.shouldShowLoader.observe(this) {
            if (it) loaderDialog?.showDialog()
            else loaderDialog?.dismissDialog()
        }
        viewModel.offerDetailsModel.observe(this) { offerModel ->
            viewModel.offerRequestModel = offerModel.toOfferRequestModel()
            addBackStack(AddOfferFragment(), binding.fragmentContainerView.id)
            viewModel.getCategoryDetailsForEdit(offerModel.categoryId ?: 0)
            viewModel.automaticallyPopulateDetails.call()
            viewModel.shouldShowLoader.value = false
        }

        viewModel.errorUploadingImagesEvent.observe(this) {
            showAlerterErrorMessage(resources.getString(R.string.error_uploading_images))
        }
    }

    private fun returnAndRefreshDashboard(shouldRefreshData: Boolean) {
        val resultIntent = Intent()
        resultIntent.putExtra(REFRESH_AFTER_ADD_OFFER, shouldRefreshData)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun showAlerterErrorMessage(errorMsg: String) {
        AlerterUtils.showErrorAlert(this, errorMsg)
    }

    private fun initListeners() {
        onBackPressedDispatcher.addCallback(this) {
            handleOnbackpressed()
        }

        binding.inclToolbar.materialToolbar.setOnClickListener {
            returnToDashboard()
        }
    }

    private fun handleOnbackpressed() {
        val fragments = supportFragmentManager.fragments
        val fragmentCount = supportFragmentManager.backStackEntryCount

        if (viewModel.isEditMode) {
            finish()
            return
        }

        if (fragments[fragmentCount] is PromoteOfferFragment) {
            finish()
        } else {
            viewModel.shouldShowToolbar.value = true
            if (fragmentCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
            setWhiteStatusBar()
            setToolbarTitle()
        }
    }

    private fun setToolbarTitle(title: String = resources.getString(R.string.add_offer)) {
        binding.inclToolbar.tvToolbarTitle.text = title
    }

    private fun showSelectCategoryFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            replace(SelectCategoryFragment(), binding.fragmentContainerView.id)
        }
    }

    private fun returnToDashboard() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun initLoaderDialog() {
        loaderDialog = LoaderDialog(this)
    }

    companion object {
        const val REFRESH_AFTER_ADD_OFFER = "REFRESH_AFTER_ADD_OFFER"
    }
}