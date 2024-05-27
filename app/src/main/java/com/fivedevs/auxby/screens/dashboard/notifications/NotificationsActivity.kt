package com.fivedevs.auxby.screens.dashboard.notifications

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.databinding.ActivityNotificationsBinding
import com.fivedevs.auxby.domain.models.enums.NotificationsTypeEnum
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.notifications.adapters.NotificationsAdapter
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel by viewModels<NotificationsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initObservers()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotifications()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications)
        binding.apply {
            viewModel = this@NotificationsActivity.viewModel
            lifecycleOwner = this@NotificationsActivity
        }
    }

    private fun initObservers() {
        viewModel.onNotificationClickEvent.observe(this) {
            handleNotificationClick(it)
        }

        viewModel.notificationsData.observe(this) {
            initNotificationsRV(it)
        }
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        when (notification.type) {
            NotificationsTypeEnum.NEW_CHAT_STARTED.name,
            NotificationsTypeEnum.NEW_MESSAGE_RECEIVED.name -> {
                navigateToChatTab()
            }
            NotificationsTypeEnum.AUCTION_INTERRUPTED.name -> {
                viewModel.deleteNotificationWhenOfferClosed(notification)
            }
            else -> {
                launchActivity<OfferDetailsActivity> {
                    putExtra(Constants.SELECTED_OFFER_ID, notification.offerId)
                }
            }
        }
    }

    private fun navigateToChatTab() {
        val resultIntent = Intent()
        resultIntent.putExtra(NAVIGATE_TO_CHAT_TAB, true)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun initNotificationsRV(notifications: List<NotificationItem>) {
        if (notifications.isEmpty()) {
            showEmptyStateView()
            return
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = NotificationsAdapter(
                this@NotificationsActivity,
                notifications,
                viewModel.notificationSelectorPublishSubject,
                viewModel.deleteNotificationPublishSubject
            )
            itemAnimator = null
        }
        showNotificationsRv()
    }

    private fun showNotificationsRv() {
        binding.inclEmptyNotificationsList.root.hide()
        binding.rvNotifications.show()
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showEmptyStateView() {
        binding.rvNotifications.hide()
        binding.inclEmptyNotificationsList.root.show()
    }

    companion object {
        const val NAVIGATE_TO_CHAT_TAB = "NAVIGATE_TO_CHAT_TAB"
    }
}