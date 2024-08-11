package com.fivedevs.auxby.screens.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
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
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.addOffer.AddOfferActivity
import com.fivedevs.auxby.screens.addOffer.AddOfferActivity.Companion.REFRESH_AFTER_ADD_OFFER
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.buyCoins.BuyCoinsActivity
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_BIDS_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_CHAT_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_FAVOURITE_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_OFFERS_POSITION
import com.fivedevs.auxby.screens.dashboard.adapter.DashboardPagerAdapter.Companion.TAB_PLACEHOLDER_POSITION
import com.fivedevs.auxby.screens.dashboard.adsDialog.AppAdsDialog
import com.fivedevs.auxby.screens.dashboard.notifications.NotificationsActivity
import com.fivedevs.auxby.screens.dashboard.notifications.NotificationsActivity.Companion.NAVIGATE_TO_CHAT_TAB
import com.fivedevs.auxby.screens.dialogs.GenericDialog
import com.fivedevs.auxby.screens.profile.ProfileActivity
import com.fivedevs.auxby.screens.profile.userActions.UserActionsDialog
import com.fivedevs.auxby.screens.search.SearchActivity
import com.fivedevs.auxby.screens.settings.SettingsActivity
import com.fivedevs.auxby.screens.tutorials.guestMode.GuestModeDialog
import com.fivedevs.auxby.screens.yourOffers.YourOffersActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.messaging.FirebaseMessaging
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel by viewModels<DashboardViewModel>()

    private val menuSmoothScroll = false
    private var dashboardPagerAdapter: DashboardPagerAdapter? = null

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initAppUpdateManager()
        viewModel.getOneTimeData()
        checkForAppUpdates()
        initObservers()
        initBottomNavigation()
        initListeners()
        initViewPager()
        initDrawerItems()
        checkNotificationsPermission()
        viewModel.getUser()
    }

    private fun initAppUpdateManager() {
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdateLIstener)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotifications()
        resumeAppUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdateLIstener)
        }
    }

    private fun resumeAppUpdate() {
        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    val appUpdateOptions = AppUpdateOptions.newBuilder(updateType).build()
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        this,
                        appUpdateOptions,
                        IN_APP_UPDATE_RESULT_CODE
                    )
                }
            }
        }
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
        viewModel.localUser.observe(this) {
            populateUserAvatar(it)
            populateUserDetails(it)
        }

        viewModel.loginClickEvent.observe(this) {
            launchActivityWithFinish<LoginActivity>()
        }

        viewModel.addOfferClickEvent.observe(this) {
            val intent = Intent(this, AddOfferActivity::class.java)
            activityResultLauncher.launch(intent)
            closeDrawer()
        }

        viewModel.searchClickEvent.observe(this) {
            launchActivity<SearchActivity>()
        }

        viewModel.closeDrawerClickEvent.observe(this) {
            closeDrawer()
        }

        viewModel.userOptionsClickEvent.observe(this) {
            val dialog = UserActionsDialog()
            dialog.show(supportFragmentManager, GenericDialog.LOGOUT_DIALOG_TAG)
        }

        viewModel.profileClickEvent.observe(this) {
            launchActivity<ProfileActivity>()
            closeDrawer()
        }

        viewModel.buyCoinsClickEvent.observe(this) {
            launchActivity<BuyCoinsActivity>()
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
            redirectToBrowserLink(
                this@DashboardActivity,
                resources.getString(R.string.link_policies_agreements)
            )
            closeDrawer()
        }

        viewModel.termsConditionsEvent.observe(this) {
            redirectToBrowserLink(
                this@DashboardActivity,
                resources.getString(R.string.link_terms_conditions)
            )
            closeDrawer()
        }

        viewModel.shouldShowSaveGuestMode.observe(this) {
            showGuestModeBottomSheet(supportFragmentManager, getString(R.string.guest_add_offer))
        }

        viewModel.networkConnectionState.observe(this, EmptyObserver())

        viewModel.appAdsEvent.observe(this) { addModel ->
            val adsDialog = AppAdsDialog(addModel) {
                if (viewModel.isUserLoggedIn.value == false) {
                    showGuestModeBottomSheet(supportFragmentManager, getString(R.string.guest_add_offer))
                } else {
                    viewModel.saveUserAdCode(addModel.code)
                    val intent = Intent(this, AddOfferActivity::class.java)
                    intent.putExtra(Constants.CATEGORY_ID, addModel.categoryId)
                    activityResultLauncher.launch(intent)
                }
            }
            adsDialog.show(supportFragmentManager, adsDialog.tag)
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh categories after new offer added
                result.data?.takeIf { it.getBooleanExtra(REFRESH_AFTER_ADD_OFFER, false) }
                    ?.run {
                        viewModel.getCategories()
                        viewModel.getUser()
                        viewModel.refreshDataEvent.call()
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

    private fun retrieveFcmTokenForTESTING() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e(
                    this@DashboardActivity.toString(),
                    "Fetching FCM registration token failed",
                    task.exception?.message
                )
                return@OnCompleteListener
            }
            val token = task.result
            Timber.d("Token here: $token")
        })
    }

    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val appUpdateOptions = AppUpdateOptions.newBuilder(updateType).build()
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    this,
                    appUpdateOptions,
                    IN_APP_UPDATE_RESULT_CODE
                )
            }
        }.addOnFailureListener {

        }
    }

    private val installStateUpdateLIstener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            toast("Download successful. Restarting the app...")
            lifecycleScope.launch {
                delay(2.seconds)
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IN_APP_UPDATE_RESULT_CODE) {
            if (resultCode == RESULT_CANCELED) {
                if (updateType == AppUpdateType.IMMEDIATE) {
                    finish()
                }
            } else if (resultCode != RESULT_OK) {
                Timber.e("Something went wrong updating the app")
            }
        }
    }

    companion object {
        fun showGuestModeBottomSheet(manager: FragmentManager, descriptionText: String) {
            val guestModeBottomSheet = GuestModeDialog(descriptionText)
            guestModeBottomSheet.show(manager, guestModeBottomSheet.tag)
        }

        const val IN_APP_UPDATE_RESULT_CODE = 100123
    }
}
