package com.fivedevs.auxby.screens.dashboard.chat.chatMessages.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.databinding.ItemGuestMessageBinding
import com.fivedevs.auxby.databinding.ItemHostMessageBinding
import com.fivedevs.auxby.databinding.ItemMessageDateBinding
import com.fivedevs.auxby.domain.models.ChatMessage
import com.fivedevs.auxby.domain.utils.Constants.TODAY
import com.fivedevs.auxby.domain.utils.Constants.YESTERDAY
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity.Companion.CHAT_DATE_DIVIDER

class ChatMessagesAdapter(
    private val context: Context,
    private var chatMessages: MutableList<ChatMessage>,
    private val user: User
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HOST_MESSAGE -> {
                val binding = ItemHostMessageBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                HostViewHolder(binding)
            }
            DATE_MESSAGE_DIVIDER -> {
                val binding = ItemMessageDateBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                DateDividerViewHolder(binding)
            }
            else -> {
                val binding = ItemGuestMessageBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                GuestViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            HOST_MESSAGE -> {
                (holder as HostViewHolder).bind(chatMessages[position])
            }
            GUEST_MESSAGE -> {
                (holder as GuestViewHolder).bind(chatMessages[position])
            }
            DATE_MESSAGE_DIVIDER -> {
                (holder as DateDividerViewHolder).bind(chatMessages[position])
            }
        }
    }

    override fun getItemCount() = chatMessages.size

    inner class HostViewHolder(private val binding: ItemHostMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            binding.apply {
                tvMessage.text = chatMessage.messageText
                tvMessageTime.text = DateUtils().getFormattedDateForUserActive(chatMessage.messageTime)
//                when (chatMessage.messageTime) {
//                    TODAY -> {
//                        tvMessageTime.text = context.resources.getString(R.string.today)
//                    }
//                    YESTERDAY -> {
//                        tvMessageTime.text = context.resources.getString(R.string.yesterday)
//                    }
//                    else -> {
//                        tvMessageTime.text = getFormattedDateForUserActive(chatMessage.messageTime)
//                    }
//                }
            }
        }
    }

    inner class GuestViewHolder(private val binding: ItemGuestMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            binding.apply {
                tvMessage.text = chatMessage.messageText
                tvMessageTime.text = DateUtils().getFormattedDateForUserActive(chatMessage.messageTime)
            }
        }
    }

    inner class DateDividerViewHolder(private val binding: ItemMessageDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            binding.apply {
                tvMessageDate.text = getDateMessage(chatMessage.messageTime)
            }
        }

        private fun getDateMessage(messageTime: String): String {
            return when (messageTime) {
                TODAY -> {
                    return context.resources.getString(R.string.today)
                }
                YESTERDAY -> {
                    return context.resources.getString(R.string.yesterday)
                }
                else -> {
                    DateUtils().getFormatChatDividerDate(messageTime)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].sender == user.email) {
            HOST_MESSAGE
        } else if (chatMessages[position].messageText == CHAT_DATE_DIVIDER) {
            DATE_MESSAGE_DIVIDER
        } else {
            GUEST_MESSAGE
        }
    }

    inner class ChatRoomsDiffCallback(
        private var oldList: MutableList<ChatMessage>,
        private var newList: MutableList<ChatMessage>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].messageTime == newList[newItemPosition].messageTime
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].messageText == newList[newItemPosition].messageText
        }
    }

    fun updateChatMessagesList(chatMessages: List<ChatMessage>) {
        val oldList = this.chatMessages.toMutableList()
        val diffResult = DiffUtil.calculateDiff(
            ChatRoomsDiffCallback(
                oldList,
                chatMessages.toMutableList()
            )
        )
        this.chatMessages = chatMessages.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val HOST_MESSAGE = 0
        const val GUEST_MESSAGE = 1
        const val DATE_MESSAGE_DIVIDER = 2
    }
}