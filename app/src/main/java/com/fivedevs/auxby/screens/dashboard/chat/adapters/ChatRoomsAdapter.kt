package com.fivedevs.auxby.screens.dashboard.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.databinding.ItemRoomBinding
import com.fivedevs.auxby.domain.utils.Formatters.formatDateStringToHourAndMinute
import com.fivedevs.auxby.domain.utils.extensions.htmlText
import com.fivedevs.auxby.domain.utils.extensions.loadImage
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatPagerAdapter.Companion.BUY_TAB_POSITION


class ChatRoomsAdapter(
    private val context: Context,
    private var chatRooms: MutableList<ChatRoom>,
    private val callback: (ChatRoom) -> Unit,
    private val tabPosition: Int
) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {

    var user: User = User()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRoomBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(chatRooms[position])
    }

    override fun getItemCount() = chatRooms.size

    inner class ViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoom: ChatRoom) {
            binding.apply {
                ivRoomAvatar.loadImage(chatRoom.chatImage, R.drawable.ic_placeholder, circleCrop = true)
                tvRoomName.text = chatRoom.roomName
                tvHostName.htmlText(getHostName(chatRoom))
                tvRoomLastMessageTime.text = formatDateStringToHourAndMinute(chatRoom.lastMessageTime)
                if (!chatRoom.lastSeenMessageTime.equals(chatRoom.lastMessageTime, ignoreCase = true) && chatRoom.lastSeenMessageTime.isNotEmpty()) {
                    highlightNewMessage()
                }
            }
            itemView.setOnClickListener {
                callback(chatRoom)
                removeHighlight()
            }
        }

        private fun removeHighlight() {
            val montserratMedium = ResourcesCompat.getFont(context, R.font.montserrat_medium)
            val montserratRegular = ResourcesCompat.getFont(context, R.font.montserrat_regular)
            binding.tvRoomName.typeface = montserratMedium
            binding.tvHostName.typeface = montserratRegular
            binding.tvRoomLastMessageTime.typeface = montserratMedium
        }

        private fun highlightNewMessage() {
            val montserratBold = ResourcesCompat.getFont(context, R.font.montserrat_bold)
            binding.tvRoomName.typeface = montserratBold
            binding.tvHostName.typeface = montserratBold
            binding.tvRoomLastMessageTime.typeface = montserratBold
        }

        private fun getHostName(chatRoom: ChatRoom): String =
            if (tabPosition == BUY_TAB_POSITION) chatRoom.guest
            else context.resources.getString(R.string.contacted_by_name, chatRoom.host)
    }

    inner class ChatRoomsDiffCallback(
        private var oldList: MutableList<ChatRoom>,
        private var newList: MutableList<ChatRoom>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].roomId == newList[newItemPosition].roomId
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].lastMessageTime == newList[newItemPosition].lastMessageTime
        }
    }

    fun updateChatRoomsList(chatRooms: List<ChatRoom>, user: User) {
        this.user = user
        val oldList = this.chatRooms.toMutableList()
        val diffResult = DiffUtil.calculateDiff(
            ChatRoomsDiffCallback(
                oldList,
                chatRooms.toMutableList()
            )
        )
        this.chatRooms = chatRooms.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }
}