package com.fivedevs.auxby.screens.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.BuildConfig
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.data.prefs.PreferencesService.Companion.PUSH_NOTIFICATIONS
import com.fivedevs.auxby.databinding.ActivityDashboardBinding
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Constants.EMPTY
import com.fivedevs.auxby.domain.utils.EmptyObserver
import com.fivedevs.auxby.domain.utils.Utils.redirectToBrowserLink
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.setCornerRadius
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.addOffer.AddOfferActivity
import com.fivedevs.auxby.screens.addOffer.AddOfferActivity.Companion.REFRESH_AFTER_ADD_OFFER
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_BIDS_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_CHAT_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_FAVOURITE_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_OFFERS_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_PLACEHOLDER_POSITION
import com.fivedevs.auxby.screens.dashboard.notifications.NotificationsActivity
import com.fivedevs.auxby.screens.dashboard.notifications.NotificationsActivity.Companion.NAVIGATE_TO_CHAT_TAB
import com.fivedevs.auxby.screens.profile.ProfileActivity
import com.fivedevs.auxby.screens.search.SearchActivity
import com.fivedevs.auxby.screens.settings.SettingsActivity
import com.fivedevs.auxby.screens.tutorials.guestMode.GuestModeDialog
import com.fivedevs.auxby.screens.yourOffers.YourOffersActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.firebase.messaging.FirebaseMessaging
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    private val menuSmoothScroll = false
    private var dashboardPagerAdapter: DashboardPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getData()
        initBinding()
        initObservers()
        initBottomNavigation()
        initListeners()
        initViewPager()
        initDrawerItems()
        checkNotificationsPermission()
        //retrieveFcmTokenForTESTING()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotifications()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.apply {
            viewModel = this@DashboardActivity.viewModel
            lifecycleOwner = this@DashboardActivity
        }
    }

    private fun initDrawerItems() {
        val versionCode = BuildConfig.VERSION_NAME
        binding.inclDrawerMenu.tvAppVersion.text =
            resources.getString(R.string.app_version, versionCode)
        binding.inclDrawerMenuGuest.tvAppVersionGuest.text =
            resources.getString(R.string.app_version, versionCode)
    }

    private fun initObservers() {
        viewModel.user.observe(this) {
            populateUserAvatar(it)
            populateUserDetails(it)
        }

        viewModel.loginClickEvent.observe(this) {
            launchActivityWithFinish<LoginActivity>()
        }

        viewModel.addOfferClickEvent.observe(this) {
            val intent = Intent(this, AddOfferActivity::class.java)
            activityResultLauncher.launch(intent)
        }

        viewModel.searchClickEvent.observe(this) {
            launchActivity<SearchActivity>()
        }

        viewModel.closeDrawerClickEvent.observe(this) {
            closeDrawer()
        }

        viewModel.profileClickEvent.observe(this) {
            launchActivity<ProfileActivity>()
            closeDrawer()
        }

        viewModel.notificationsClickEvent.observe(this) {
            val intent = Intent(this, NotificationsActivity::class.java)
            activityResultLauncher.launch(intent)
            closeDrawer()
        }

        viewModel.yourOffersClickEvent.observe(this) {
            launchActivity<YourOffersActivity>()
            closeDrawer()
        }

        viewModel.settingsClickEvent.observe(this) {
            launchActivity<SettingsActivity>()
            closeDrawer()
        }

        viewModel.contactUsCLickEvent.observe(this) {
            redirectToBrowserLink(this@DashboardActivity, resources.getString(R.string.auxby_ro))
            closeDrawer()
        }

        viewModel.policiesClickEvent.observe(this) {
            redirectToBrowserLink(this@DashboardActivity, resources.getString(R.string.link_policies_agreements))
            closeDrawer()
        }

        viewModel.termsConditionsEvent.observe(this) {
            redirectToBrowserLink(this@DashboardActivity, resources.getString(R.string.link_terms_conditions))
            closeDrawer()
        }

        viewModel.shouldShowSaveGuestMode.observe(this) {
            showGuestModeBottomSheet(supportFragmentManager, getString(R.string.guest_add_offer))
        }

        viewModel.networkConnectionState.observe(this, EmptyObserver())
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh categories after new offer added
                result.data?.takeIf { it.getBooleanExtra(REFRESH_AFTER_ADD_OFFER, false) }
                    ?.run {
                        viewModel.getCategories()
                        viewModel.getUser()
                    }

                // Navigate to Chat tab after user clicks on New Message notification
                result.data?.takeIf { it.getBooleanExtra(NAVIGATE_TO_CHAT_TAB, false) }
                    ?.run { navigateToChatTab() }
            }
        }

    private fun navigateToChatTab() {
        binding.viewPagerDashboard.doOnPreDraw {
            binding.viewPagerDashboard.currentItem = TAB_CHAT_POSITION
        }
        binding.bottomNavigationView.apply {
            selectedItemId = R.id.tab_chat
        }
    }

    private fun populateUserDetails(user: User) {
        binding.inclDrawerMenu.tvAvailableCoins.text =
            resources.getString(R.string.available_coins, user.availableCoins.toString())
    }

    private fun populateUserAvatar(user: User) {
        Glide
            .with(this@DashboardActivity)
            .load(user.avatar)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(getDrawableCompat(R.drawable.ic_profile_placeholder))
            .placeholder(getDrawableCompat(R.drawable.ic_profile_placeholder))
            .circleCrop()
            .override(300, 300)
            .into(binding.inclDrawerMenu.ivUserAvatar)
    }

    private fun initBottomNavigation() {
        binding.bottomNavigationView.apply {
            background = null
            itemIconTintList = null
            menu.getItem(TAB_PLACEHOLDER_POSITION).isEnabled = false
        }
        applyBottomBarRadius()
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            binding.drawerDashboard.openDrawer(GravityCompat.START)
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_offers -> {
                    viewModel.toolbarTitle.value = EMPTY
                    binding.viewPagerDashboard.setCurrentItem(TAB_OFFERS_POSITION, menuSmoothScroll)
                }
                R.id.tab_favourite -> {
                    viewModel.toolbarTitle.value = getString(R.string.favourite)
                    binding.viewPagerDashboard.setCurrentItem(
                        TAB_FAVOURITE_POSITION,
                        menuSmoothScroll
                    )
                }
                R.id.tab_chat -> {
                    viewModel.toolbarTitle.value = getString(R.string.messages)
                    binding.viewPagerDashboard.setCurrentItem(TAB_CHAT_POSITION, menuSmoothScroll)
                }
                R.id.tab_my_bids -> {
                    viewModel.toolbarTitle.value = getString(R.string.my_bids)
                    binding.viewPagerDashboard.setCurrentItem(TAB_BIDS_POSITION, menuSmoothScroll)
                }
            }
            true
        }
    }

    private fun initViewPager() {
        dashboardPagerAdapter = DashboardPagerAdapter(this.supportFragmentManager, lifecycle)
        binding.viewPagerDashboard.apply {
            isUserInputEnabled = false
            offscreenPageLimit = 4
            adapter = dashboardPagerAdapter
        }
    }

    private fun applyBottomBarRadius() {
        val radius = resources.getDimension(R.dimen.space_12dp)
        (binding.bottomAppBar.background as MaterialShapeDrawable).setCornerRadius(radius)
    }

    private fun closeDrawer() {
        if (binding.drawerDashboard.isOpen) {
            binding.drawerDashboard.closeDrawer(GravityCompat.START, true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_STORAGE, Constants.PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        AlerterUtils.showErrorAlert(
                            this,
                            resources.getString(R.string.allow_permission_manually)
                        )
                    }
                }
            }
        }
    }

    private fun checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                .permissions(Manifest.permission.POST_NOTIFICATIONS)
                .request { allGranted, _, _ ->
                    PreferencesService(this).setValue(PUSH_NOTIFICATIONS, allGranted)
                    if (allGranted) {
                        viewModel.listenForFCMToken()
                    }
                }
        }
    }

    // TODO -> REMOVE THIS AFTER NOTIFICATION API IMPLEMENTATION
    private fun retrieveFcmTokenForTESTING() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    this@DashboardActivity.toString(),
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Log.d("Token here:", token)
        })
    }

    companion object {
        fun showGuestModeBottomSheet(manager: FragmentManager, descriptionText: String) {
            val guestModeBottomSheet = GuestModeDialog(descriptionText)
            guestModeBottomSheet.show(manager, guestModeBottomSheet.tag)
        }
    }
}
