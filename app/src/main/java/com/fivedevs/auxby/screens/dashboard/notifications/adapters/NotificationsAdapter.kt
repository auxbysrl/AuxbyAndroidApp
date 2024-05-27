package com.fivedevs.auxby.screens.dashboard.notifications.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.NotificationItem
import com.fivedevs.auxby.databinding.ItemNotificationBinding
import com.fivedevs.auxby.domain.models.enums.getNotificationDescription
import com.fivedevs.auxby.domain.models.enums.getNotificationTitle
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import io.reactivex.rxjava3.subjects.PublishSubject

class NotificationsAdapter(
    var context: Context,
    var notifications: List<NotificationItem>,
    var notificationPublishSubject: PublishSubject<NotificationItem>,
    var deleteNotificationPublishSubject: PublishSubject<NotificationItem>
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationItem) {
            binding.apply {
                tvNotificationDate.text = getNotificationsDate(notification.date)
                tvNotificationTitle.text = getNotificationTitle(notification.type, context)
                tvNotificationDescription.text = getNotificationDescription(notification.type, context)
            }
            itemView.setOnClickListenerWithDelay {
                notificationPublishSubject.onNext(notification)
            }
            binding.ivDeleteNotification.setOnClickListenerWithDelay {
                deleteNotificationPublishSubject.onNext(notification)
            }
        }

        private fun getNotificationsDate(date: String): String {
            return if (DateUtils().isToday(date)) {
                context.getString(
                    R.string.today
                )
            } else {
                DateUtils().getFormattedDateYearMonthDay(date)
            }
        }
    }
}